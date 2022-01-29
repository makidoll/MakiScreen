package cat.maki.MakiScreen;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

// https://stackoverflow.com/questions/21420252/how-to-receive-mpeg-ts-stream-over-udp-from-ffmpeg-in-java
class VideoCaptureUDPServer extends Thread {
    public boolean running = true;

    private DatagramSocket socket;

    public void onFrame(BufferedImage frame) { }

    public void run() {
        try {
            byte[] buffer = new byte[1024*1024]; // 1 mb
            socket = new DatagramSocket(1337);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            int soi = 0; // start of image / SOI
            int eoi = 0; // end of image / EOI
            while (running) {
                socket.receive(packet);

                byte[] data = packet.getData();

                int length = packet.getLength();
                for (int i = packet.getOffset(); i < length; i++) {
                    byte b = data[i];
                    switch (b) {
                        case (byte) 0xFF:
                            if (soi % 2 == 0) soi++; // find next byte
                            if (eoi == 0) eoi++;
                            break;
                        case (byte) 0xD8:
                            if (soi % 2 == 1) {
                                soi++; // first SOI found
                            }
                            if (soi == 4) {
                                // found another SOI, probably incomplete frame.
                                // discard previous data, restart with this SOI
                                output.reset();
                                output.write(0xFF);
                                soi = 2;
                            }
                            break;
                        case (byte) 0xD9:
                            if (eoi == 1) eoi++; // EOI found
                            break;
                        default:
                            // wrong byte, reset
                            if (soi == 1) soi = 0;
                            if (eoi == 1) eoi = 0;
                            if (soi == 3) soi--;
                            break;
                    }
                    output.write(b);
                    if (eoi == 2) { // image is complete
                        try {
                            ByteArrayInputStream stream = new ByteArrayInputStream(output.toByteArray());
                            BufferedImage bufferedImage = ImageIO.read(stream);
                            if (bufferedImage != null) {
                                onFrame(bufferedImage);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // reset
                         output.reset();
                        soi = 0;
                        eoi = 0;
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cleanup() {
        running = false;
        if (socket!=null) socket.disconnect();
        if (socket!=null) socket.close();
    }
}

public class VideoCapture extends Thread {
    public int width;
    public int height;
    MakiScreen plugin;
    public static BufferedImage currentFrame;

    VideoCaptureUDPServer videoCaptureUDPServer;

    public VideoCapture(MakiScreen plugin, int width, int height) {
        this.plugin = plugin;
        this.width = width;
        this.height = height;


        videoCaptureUDPServer = new VideoCaptureUDPServer() {
            @Override
            public void onFrame(BufferedImage frame) {
                currentFrame = frame;
            }
        };
        videoCaptureUDPServer.start();

    }

    public void cleanup() {
        videoCaptureUDPServer.cleanup();
    }
}

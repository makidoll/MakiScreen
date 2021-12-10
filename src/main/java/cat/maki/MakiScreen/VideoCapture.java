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

            while (running) {
                socket.receive(packet);

                byte[] data = packet.getData();

                if (data[0]==-1 && data[1]==-40) { // FF D8 (start of file)
                    if (output.size()>0) {
                        try {
                            ByteArrayInputStream stream = new ByteArrayInputStream(output.toByteArray());
                            BufferedImage bufferedImage = ImageIO.read(stream);
                            if (bufferedImage != null) {
                                onFrame(bufferedImage);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        output.reset();
                    }
                }

                output.write(data,0,packet.getLength());
                //System.out.println(String.format("%02X", data[0])+" "+String.format("%02X", data[1]));
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

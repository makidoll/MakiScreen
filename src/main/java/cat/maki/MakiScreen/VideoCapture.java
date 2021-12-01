package cat.maki.MakiScreen;

import org.bukkit.map.MapCanvas;

import javax.imageio.ImageIO;
import java.awt.*;
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
                            onFrame(ImageIO.read(stream));
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
    private static Image currentFrame;
    public int width;
    public int height;


    VideoCaptureUDPServer videoCaptureUDPServer;


    public static void renderCanvas(int id, MapCanvas mapCanvas) {
        BufferedImage frame = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = frame.createGraphics();
        switch (id) {
            case 0 -> graphics.drawImage(currentFrame, 0, 0, null);
            case 1 -> graphics.drawImage(currentFrame, -128, 0, null);
            case 2 -> graphics.drawImage(currentFrame, -256, 0, null);
            case 3 -> graphics.drawImage(currentFrame, -384, 0, null);
            case 4 -> graphics.drawImage(currentFrame, 0, -128, null);
            case 5 -> graphics.drawImage(currentFrame, -128, -128, null);
            case 6 -> graphics.drawImage(currentFrame, -256, -128, null);
            case 7 -> graphics.drawImage(currentFrame, -384, -128, null);
        }
        mapCanvas.drawImage(0,0, frame);
        graphics.dispose();

    }

    public VideoCapture(int width, int height) {
        this.width = width;
        this.height = height;

        currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

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

package cat.maki.MakiScreen;

import static cat.maki.MakiScreen.dither.DitherLookupUtil.COLOR_MAP;

import com.google.common.collect.EvictingQueue;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Queue;

import org.bukkit.scheduler.BukkitRunnable;

class FrameProcessorTask extends BukkitRunnable {
  private final Queue<byte[][]> frameBuffers = EvictingQueue.create(40);

  private byte[] frameData;
  private BufferedImage frame;

  public Queue<byte[][]> getFrameBuffers() {
    return frameBuffers;
  }


  private final int mapAmount = ConfigFile.getMapAmount();
  private final int mapWidth = ConfigFile.getMapWidth();


  private byte[] drawImage(int partId) {
    byte[] bytes = new byte[128 * 128];

    int width = frame.getWidth();
    int offset = 0;
    int startX = ((partId % mapWidth) * 128);
    int startY = ((partId / mapWidth) * 128);
    int maxY = startY + 128;
    int maxX = startX + 128;
    for (int y = startY; y < maxY; y++) {
      for (int x = startX; x < maxX; x++) {
        int pos = (y * 3 * width) + (x * 3);
        int rgb = -16777216;
        rgb += ((int) frameData[pos++] & 0xff);
        rgb += (((int) frameData[pos++] & 0xff) << 8);
        rgb += (((int) frameData[pos] & 0xff) << 16);
        bytes[offset++] = getColor(rgb);
      }
    }
    return bytes;
  }

  private byte getColor(int rgb) {
    return COLOR_MAP[(rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7
        | (rgb & 0xFF) >> 1];
  }

  @Override
  public void run() {

    frame = VideoCapture.currentFrame;
    if (frame == null) {
      return;
    }
    frameData = ((DataBufferByte) frame.getRaster().getDataBuffer()).getData();

    byte[][] buffers = new byte[mapAmount][];

    for (int i = 0; i < mapAmount; i++) {
      buffers[i] = drawImage(i);
    }
    frameBuffers.offer(buffers);
  }
}

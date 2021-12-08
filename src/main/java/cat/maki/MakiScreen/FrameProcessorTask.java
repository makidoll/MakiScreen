package cat.maki.MakiScreen;

import com.google.common.collect.EvictingQueue;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Queue;

import static cat.maki.MakiScreen.dither.DitherLookupUtil.COLOR_MAP;
import static cat.maki.MakiScreen.dither.DitherLookupUtil.FULL_COLOR_MAP;

class FrameProcessorTask extends BukkitRunnable {

  private final Object lock = new Object();
  private final Queue<byte[][]> frameBuffers = EvictingQueue.create(450);
  private final int mapSize;

  private final byte[] ditheredFrameData;
  private final int[][] ditherBuffer;
  private final byte[][] cachedMapData;
  private final int frameWidth;
  private byte[] frameData;

  FrameProcessorTask(int mapSize, int mapWidth) {
    this.mapSize = mapSize;
    this.frameWidth = mapWidth * 128;
    this.ditheredFrameData = new byte[mapSize * 128 * 128];
    this.ditherBuffer = new int[2][frameWidth << 2];
    this.cachedMapData = new byte[mapSize][];
  }

  public Queue<byte[][]> getFrameBuffers() {
    return frameBuffers;
  }

  private void ditherFrame() {
    int width = this.frameWidth;
    int height = this.ditheredFrameData.length / width;
    int widthMinus = width - 1;
    int heightMinus = height - 1;

    //   |  Y
    // X | -> -> -> ->
    //   | <- <- <- <-
    //   | -> -> -> ->
    //   | <- <- <- <-
    for (int y = 0; y < height; y++) {
      boolean hasNextY = y < heightMinus;
      int yIndex = y * width;
      if ((y & 0x1) == 0) { // Forward
        int bufferIndex = 0;
        final int[] buf1 = ditherBuffer[0];
        final int[] buf2 = ditherBuffer[1];

        for (int x = 0; x < width; ++x) {
          int pos = pos(x, y, width);
          int blue = (int) frameData[pos++] & 0xff;
          int green = ((int) frameData[pos++] & 0xff);
          int red = ((int) frameData[pos] & 0xff);

          red = Math.max(Math.min(255, red + buf1[bufferIndex++]), 0);
          green = Math.max(Math.min(255, green + buf1[bufferIndex++]), 0);
          blue = Math.max(Math.min(255, blue + buf1[bufferIndex++]), 0);

          final int closest = getBestFullColor(red, green, blue);
          final int delta_r = red - (closest >> 16 & 0xFF);
          final int delta_g = green - (closest >> 8 & 0xFF);
          final int delta_b = blue - (closest & 0xFF);

          if (x < widthMinus) {
            buf1[bufferIndex] = delta_r >> 1;
            buf1[bufferIndex + 1] = delta_g >> 1;
            buf1[bufferIndex + 2] = delta_b >> 1;
          }
          if (hasNextY) {
            if (x > 0) {
              buf2[bufferIndex - 6] = delta_r >> 2;
              buf2[bufferIndex - 5] = delta_g >> 2;
              buf2[bufferIndex - 4] = delta_b >> 2;
            }
            buf2[bufferIndex - 3] = delta_r >> 2;
            buf2[bufferIndex - 2] = delta_g >> 2;
            buf2[bufferIndex - 1] = delta_b >> 2;
          }
          ditheredFrameData[yIndex + x] = getColor(closest);
        }
      } else { // Backward
        int bufferIndex = width + (width << 1) - 1;
        final int[] buf1 = ditherBuffer[1];
        final int[] buf2 = ditherBuffer[0];
        for (int x = width - 1; x >= 0; --x) {
          int pos = pos(x, y, width);
          int blue = (int) frameData[pos++] & 0xff;
          int green = ((int) frameData[pos++] & 0xff);
          int red = ((int) frameData[pos] & 0xff);

          red = Math.max(Math.min(255, red + buf1[bufferIndex--]), 0);
          green = Math.max(Math.min(255, green + buf1[bufferIndex--]), 0);
          blue = Math.max(Math.min(255, blue + buf1[bufferIndex--]), 0);

          int closest = getBestFullColor(red, green, blue);
          int delta_r = red - (closest >> 16 & 0xFF);
          int delta_g = green - (closest >> 8 & 0xFF);
          int delta_b = blue - (closest & 0xFF);

          if (x > 0) {
            buf1[bufferIndex] = delta_b >> 1;
            buf1[bufferIndex - 1] = delta_g >> 1;
            buf1[bufferIndex - 2] = delta_r >> 1;
          }
          if (hasNextY) {
            if (x < widthMinus) {
              buf2[bufferIndex + 6] = delta_b >> 2;
              buf2[bufferIndex + 5] = delta_g >> 2;
              buf2[bufferIndex + 4] = delta_r >> 2;
            }
            buf2[bufferIndex + 3] = delta_b >> 2;
            buf2[bufferIndex + 2] = delta_g >> 2;
            buf2[bufferIndex + 1] = delta_r >> 2;
          }
          ditheredFrameData[yIndex + x] = getColor(closest);
        }
      }
    }
  }

  private static int pos(int x, int y, int width) {
    return (y * 3 * width) + (x * 3);
  }

  private static byte getColor(int rgb) {
    return COLOR_MAP[(rgb >> 16 & 0xFF) >> 1 << 14 | (rgb >> 8 & 0xFF) >> 1 << 7
        | (rgb & 0xFF) >> 1];
  }

  private static int getBestFullColor(final int red, final int green, final int blue) {
    return FULL_COLOR_MAP[red >> 1 << 14 | green >> 1 << 7 | blue >> 1];
  }

  @Override
  public void run() {
    synchronized (lock) {
      BufferedImage frame = VideoCapture.currentFrame;
      if (frame == null) {
        return;
      }
      frameData = ((DataBufferByte) frame.getRaster().getDataBuffer()).getData();
      long l = System.nanoTime();
      ditherFrame();
      long diff = System.nanoTime() - l;
//      System.out.println("DitherTime: " + diff + "ns");

      byte[][] buffers = new byte[mapSize][];

      for (int partId = 0; partId < buffers.length; partId++) {
        buffers[partId] = getMapData(partId, frameWidth);
      }

      frameBuffers.offer(buffers);
    }
  }

  private byte[] getMapData(int partId, int width) {
    int offset = 0;
    int startX = ((partId % ConfigFile.getMapWidth()) * 128);
    int startY = ((partId / ConfigFile.getMapWidth()) * 128);
    int maxY = startY + 128;
    int maxX = startX + 128;

    boolean modified = false;
    byte[] bytes = this.cachedMapData[partId];
    if (bytes == null) {
      bytes = new byte[128 * 128];
      modified = true;
    }
    for (int y = startY; y < maxY; y++) {
      int yIndex = y * width;
      for (int x = startX; x < maxX; x++) {
        byte newColor = ditheredFrameData[yIndex + x];
        if (modified) {
          bytes[offset] = newColor;
        } else {
          if (bytes[offset] != newColor) {
            bytes[offset] = newColor;
            modified = true;
          }
        }
        offset++;
      }
    }

    if (modified) {
      this.cachedMapData[partId] = bytes;
      return bytes;
    }
    return null;
  }

}

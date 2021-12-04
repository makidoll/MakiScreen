package cat.maki.MakiScreen;

public class ScreenPart {
  public static final int WIDTH = 128;
  public static final int HEIGHT = 128;

  public final int mapId;
  public final int partId;

  public ScreenPart(int mapId, int partId) {
    this.mapId = mapId;
    this.partId = partId;
  }
}

package cat.maki.MakiScreen;

public record ScreenPart(int mapId, int partId) {

  public int getMapId() {
    return mapId;
  }

  public int getPartId() {
    return partId;
  }

}

package cat.maki.MakiScreen;

import java.util.Queue;
import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.world.level.saveddata.maps.WorldMap.b;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

class FramePacketSender extends BukkitRunnable {
  private final Queue<byte[][]> frameBuffers;

  public FramePacketSender(Queue<byte[][]> frameBuffers) {
    this.frameBuffers = frameBuffers;
  }

  @Override
  public void run() {
    byte[][] buffers = frameBuffers.poll();
    if (buffers == null) {
      return;
    }
    PacketPlayOutMap[] packets = new PacketPlayOutMap[MakiScreen.screens.size()];
    for (int i = 0; i < MakiScreen.screens.size(); i++) {
      ScreenPart screen = MakiScreen.screens.get(i);
      packets[i] = getPacket(screen.getMapId(), buffers[screen.getPartId()]);
    }

    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
      for (PacketPlayOutMap packet : packets) {
        ((CraftPlayer) onlinePlayer).getHandle().networkManager.sendPacket(packet);
      }
    }
  }

  private PacketPlayOutMap getPacket(int mapId, byte[] data) {
    return new PacketPlayOutMap(
        mapId, (byte) 0, false, null,
        new b(0, 0, 128, 128, data));
  }
}

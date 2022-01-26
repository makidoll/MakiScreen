package cat.maki.MakiScreen;

import java.net.http.WebSocket.Listener;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.world.level.saveddata.maps.WorldMap.b;
import net.minecraft.server.network.PlayerConnection;

class FramePacketSender extends BukkitRunnable implements Listener, org.bukkit.event.Listener {
  private long frameNumber = 0;
  private final Queue<byte[][]> frameBuffers;
  private final MakiScreen plugin;

  public FramePacketSender(MakiScreen plugin, Queue<byte[][]> frameBuffers) {
    this.frameBuffers = frameBuffers;
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @Override
  public void run() {
    byte[][] buffers = frameBuffers.poll();
    if (buffers == null) {
      return;
    }
    List<PacketPlayOutMap> packets = new ArrayList<>(MakiScreen.screens.size());
    for (ScreenPart screenPart : MakiScreen.screens) {
      byte[] buffer = buffers[screenPart.partId];
      if (buffer != null) {
        PacketPlayOutMap packet = getPacket(screenPart.mapId, buffer);
        if (!screenPart.modified) {
          packets.add(0, packet);
        } else {
          packets.add(packet);
        }
        screenPart.modified = true;
        screenPart.lastFrameBuffer = buffer;
      } else {
        screenPart.modified = false;
      }
    }

    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
      sendToPlayer(onlinePlayer, packets);
    }

    if (frameNumber % 300 == 0) {
      byte[][] peek = frameBuffers.peek();
      if (peek != null) {
        frameBuffers.clear();
        frameBuffers.offer(peek);
      }
    }
    frameNumber++;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    new BukkitRunnable() {
      @Override
      public void run() {
        List<PacketPlayOutMap> packets = new ArrayList<>();
        for (ScreenPart screenPart : MakiScreen.screens) {
          if (screenPart.lastFrameBuffer != null) {
            packets.add(getPacket(screenPart.mapId, screenPart.lastFrameBuffer));
          }
        }
        sendToPlayer(event.getPlayer(), packets);
      }
    }.runTaskLater(plugin, 10);
  }

  private void sendToPlayer(Player player, List<PacketPlayOutMap> packets) {
    final PlayerConnection connection = ((CraftPlayer) player).getHandle().b;
    for (PacketPlayOutMap packet : packets) {
      if (packet != null) {
        connection.a(packet);
      }
    }
  }

  private PacketPlayOutMap getPacket(int mapId, byte[] data) {
    if (data == null) {
      throw new NullPointerException("data is null");
    }
    return new PacketPlayOutMap(
        mapId, (byte) 0, false, null,
        new b(0, 0, 128, 128, data));
  }
}

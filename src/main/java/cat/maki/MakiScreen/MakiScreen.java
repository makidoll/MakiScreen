package cat.maki.MakiScreen;

import net.minecraft.network.protocol.game.PacketPlayOutMap;
import net.minecraft.world.level.saveddata.maps.WorldMap.b;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public final class MakiScreen extends JavaPlugin implements Listener {

    private final Logger logger = getLogger();

    public static final List<ScreenPart> screens = new CopyOnWriteArrayList<>();
    private VideoCapture videoCapture;
    public static Queue<byte[][]> frameBuffers = new LinkedList<>();

    @Override
    public void onEnable() {
        ImageManager manager = ImageManager.getInstance();
        manager.init();

        logger.info("Hi!");
        getServer().getPluginManager().registerEvents(this, this);

        int width = 128 * 4;
        int height = 128 * 2;
        videoCapture = new VideoCapture(this, width, height);
        videoCapture.start();

        new BukkitRunnable() {

            @Override
            public void run() {
                byte[][] buffers = frameBuffers.poll();
                if (buffers == null) {
                    return;
                }
                PacketPlayOutMap[] packets = new PacketPlayOutMap[screens.size()];
                for (int i = 0; i < screens.size(); i++) {
                    ScreenPart screen = screens.get(i);
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
        }.runTaskTimerAsynchronously(this, 0, 1);

        new BukkitRunnable() {
            public byte[] drawImage(int x, int y, Image image) {
                byte[] buffer = new byte[128 * 128];
                byte[] bytes = MapPalette.imageToBytes(image);

                for(int x2 = 0; x2 < image.getWidth(null); ++x2) {
                    for(int y2 = 0; y2 < image.getHeight(null); ++y2) {
                        this.setPixel(buffer,x + x2, y + y2, bytes[y2 * image.getWidth(null) + x2]);
                    }
                }
                return buffer;
            }

            public void setPixel(byte[] buffer, int x, int y, byte color) {
                if (x >= 0 && y >= 0 && x < 128 && y < 128) {
                    if (buffer[y * 128 + x] != color) {
                        buffer[y * 128 + x] = color;
                    }
                }
            }

            @Override
            public void run() {
                BufferedImage frame = VideoCapture.currentFrame;
                if (frame == null) {
                    return;
                }

                byte[][] buffers = new byte[8][];

                for (int i =0;i < 8;i++) {
                    BufferedImage imgPart = frame.getSubimage(i % 4 * 128, (i / 4) * 128,
                            128, 128);
                    buffers[i] = drawImage(0, 0, imgPart);
                }
                frameBuffers.add(buffers);
            }
        }.runTaskTimerAsynchronously(this, 0, 1);
    }

    @Override
    public void onDisable() {
        logger.info("Bye!");
        videoCapture.cleanup();
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
        Player player = (Player) sender;
        if (command.getName().equals("maki")) {
            if (!player.isOp()) {
                player.sendMessage("You don't have permission!");
                return false;
            }

            for (int i=0; i<8; i++) {
                MapView mapView = getServer().createMap(player.getWorld());
                mapView.setScale(MapView.Scale.CLOSEST);
                mapView.setUnlimitedTracking(true);
                for (MapRenderer renderer : mapView.getRenderers()) {
                    mapView.removeRenderer(renderer);
                }

                ItemStack itemStack = new ItemStack(Material.FILLED_MAP);

                MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
                mapMeta.setMapView(mapView);

                itemStack.setItemMeta(mapMeta);
                player.getInventory().addItem(itemStack);
                screens.add(new ScreenPart(mapView.getId(), i));
                ImageManager manager = ImageManager.getInstance();
                manager.saveImage(mapView.getId(), i);
            }
        }

        return true;
    }

}
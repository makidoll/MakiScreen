package cat.maki.MakiScreen;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.logging.Logger;

public final class MakiScreen extends JavaPlugin implements Listener {

    private final Logger logger = getLogger();

    private VideoCapture videoCapture;

    private static final int maps = 8;

    @Override
    public void onEnable() {
        logger.info("Hi!");
        getServer().getPluginManager().registerEvents(this, this);

        // BufferedImage original = ImageIO.read(new URL("https://cutelab.space/u/XjZRz2.png"));
        // Graphics2D imageGraphics = image.createGraphics();
        // imageGraphics.drawImage(original,0,0,128,128,null);
        // imageGraphics.dispose();

        int width = 128 * 4;
        int height = 128 * 2;
        videoCapture = new VideoCapture(width, height);
        videoCapture.start();
    }

    @Override
    public void onDisable() {
        logger.info("Bye!");
        videoCapture.cleanup();
    }

    @EventHandler
    public void onMapInitialize(MapInitializeEvent e) {
        e.getMap().removeRenderer(e.getMap().getRenderers().get(0));

        MapView mapView = e.getMap();
        int id = mapView.getId();
        if (id>maps) return;

        mapView.setScale(MapView.Scale.FARTHEST);
        mapView.setUnlimitedTracking(true);
        mapView.getRenderers().clear();
        mapView.addRenderer(new MapRenderer(true) {
            @Override
            public void render(@NotNull MapView mapView, @NotNull MapCanvas mapCanvas, @NotNull Player player) {
                videoCapture.renderCanvas(id, mapCanvas);
            }
        });
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
        Player player = (Player) sender;
        if (command.getName().equals("maki")) {
            if (!player.isOp()) {
                player.sendMessage("You don't have permission!");
                return false;
            }

            for (int i=0; i<maps; i++) {
                getServer().createMap(player.getWorld());
                ItemStack itemStack = new ItemStack(Material.FILLED_MAP);

                MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
                //mapMeta.setDisplayName("MakiScreen "+(i+1));
                mapMeta.setMapId(i);
//                mapMeta.setMapView(getServer().createMap(player.getWorld()));
                itemStack.setItemMeta(mapMeta);

                player.getInventory().addItem(itemStack);
            }
        }

        return true;
    }
}

package cat.maki.MakiScreen;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public final class MakiScreen extends JavaPlugin implements Listener {

    private final Logger logger = getLogger();

    private VideoCapture videoCapture;

    @Override
    public void onEnable() {
        ImageManager manager = ImageManager.getInstance();
        manager.init();

        logger.info("Hi!");
        getServer().getPluginManager().registerEvents(this, this);

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
                mapView.setScale(MapView.Scale.FARTHEST);
                mapView.setUnlimitedTracking(true);
                mapView.removeRenderer(mapView.getRenderers().get(0));

                ItemStack itemStack = new ItemStack(Material.FILLED_MAP);

                MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
                mapMeta.setMapView(mapView);

                itemStack.setItemMeta(mapMeta);
                player.getInventory().addItem(itemStack);

                int finalI = i;
                mapView.addRenderer(new MapRenderer(true) {
                    @Override
                    public void render(@NotNull MapView mapView, @NotNull MapCanvas mapCanvas, @NotNull Player player) {
                        VideoCapture.renderCanvas(finalI, mapCanvas);
                    }
                });
                ImageManager manager = ImageManager.getInstance();
                manager.saveImage(mapView.getId(), i);
            }
        }

        return true;
    }

}

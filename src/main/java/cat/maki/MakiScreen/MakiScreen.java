package cat.maki.MakiScreen;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class MakiScreen extends JavaPlugin implements Listener {

    private final Logger logger = getLogger();

    public static final List<ScreenPart> screens = new CopyOnWriteArrayList<>();
    private VideoCapture videoCapture;

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

        FrameProcessorTask frameProcessorTask = new FrameProcessorTask();
        frameProcessorTask.runTaskTimerAsynchronously(this, 0, 1);
        FramePacketSender framePacketSender = new FramePacketSender(frameProcessorTask.getFrameBuffers());
        framePacketSender.runTaskTimerAsynchronously(this, 0, 1);
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

            for (int i=0; i<32; i++) {
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
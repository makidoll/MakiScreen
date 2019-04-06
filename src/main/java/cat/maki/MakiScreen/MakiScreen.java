package cat.maki.MakiScreen;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class MakiScreen extends JavaPlugin implements Listener {

    private Logger logger = getLogger();

    private int width = 128*2;
    private int height = 128*1;

    private VideoCapture videoCapture;

    @Override
    public void onEnable() {
        logger.info("Hi!");
        getServer().getPluginManager().registerEvents(this, this);

        // BufferedImage original = ImageIO.read(new URL("https://cutelab.space/u/XjZRz2.png"));
        // Graphics2D imageGraphics = image.createGraphics();
        // imageGraphics.drawImage(original,0,0,128,128,null);
        // imageGraphics.dispose();

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
        MapView mapView = e.getMap();
        int id = mapView.getId();
        if (id>6) return;

        mapView.setScale(MapView.Scale.FARTHEST);
        mapView.setUnlimitedTracking(true);
        mapView.getRenderers().clear();
        mapView.addRenderer(new MapRenderer(true) {
            @Override
            public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
                videoCapture.renderCanvas(id, mapCanvas);
            }
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        Player player = (Player) sender;

        if (command.getName().equals("maki")) {
            if (!player.isOp()) {
                player.sendMessage("You dont have permission!");
                return false;
            }

            for (int i=0; i<2; i++) {
                ItemStack itemStack = new ItemStack(Material.FILLED_MAP);

                MapMeta mapMeta = (MapMeta)itemStack.getItemMeta();
                //mapMeta.setDisplayName("MakiScreen "+(i+1));
                mapMeta.setMapId(i);
                itemStack.setItemMeta(mapMeta);

                player.getInventory().addItem(itemStack);
            }
        }

        return true;
    }
}

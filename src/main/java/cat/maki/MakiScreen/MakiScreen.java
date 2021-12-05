package cat.maki.MakiScreen;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

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

        ConfigFile configFile = new ConfigFile(this);
        configFile.run();

        manager.removeAllData();
        for (int i=0; i<ConfigFile.getMapAmount(); i++) {
            manager.saveImage(i, i);
        }


        logger.info("Config file loaded \n"+
                "Map amount: " + ConfigFile.getMapAmount() +"\n"+
                "Map Width: " + ConfigFile.getMapWidth() +"\n"+
                "VC Height: " + ConfigFile.getVCHeight() +"\n"+
                "VC Width: " + ConfigFile.getVCWidth()
        );

        videoCapture = new VideoCapture(this,
                ConfigFile.getVCWidth(),
                ConfigFile.getVCHeight()
        );
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

            for (int i=0; i<ConfigFile.getMapAmount(); i++) {
                getServer().dispatchCommand(player, "give "+player.getName()+" minecraft:filled_map{map:"+i+"}");

                screens.add(new ScreenPart(i, i));
                ImageManager manager = ImageManager.getInstance();
                manager.saveImage(i, i);
            }
        }

        return true;
    }

}
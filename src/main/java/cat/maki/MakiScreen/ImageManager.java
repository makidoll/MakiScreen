package cat.maki.MakiScreen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;

/***
 *
 * @author CodedRed
 * https://www.youtube.com/watch?v=DMNdcJyeP4k
 * Code Provided by CodedRed
 */
public class ImageManager implements Listener {

    private static ImageManager instance = null;

    public static ImageManager getInstance() {
        if (instance == null)
            instance = new ImageManager();
        return instance;
    }

    private final CustomFile dataFile = new CustomFile("data.yml");

    private final Map<Integer, Integer> savedImages = new HashMap<>();

    /***
     * Call this method in the onEnable()
     * Code:
     * ImageManager manager = ImageManger.getInstance();
     * manager.init();
     *
     * Once done, just add the below code to the end of your map command:
     * ImageManager manager = ImageManager.getInstance();
     * manager.saveImage(view.getId(), args[0]); // args[0] is the url
     *
     */
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, MakiScreen.getPlugin(MakiScreen.class));
        loadImages();
    }


    @EventHandler
    public void onMapInitEvent(MapInitializeEvent event) {
        if (hasImage(event.getMap().getId())) {
            MapView view = event.getMap();
            for (MapRenderer renderer : view.getRenderers())
                view.removeRenderer(renderer);
            view.setScale(Scale.CLOSEST);
            view.setTrackingPosition(false);
            MakiScreen.screens.add(new ScreenPart(view.getId(), getImage(view.getId())));
        }
    }


    /***
     * Whenever a new map is created, save the ID and Image to data file.
     *
     * @param id - MapView ID
     * @param partId - int partId
     */
    public void saveImage(Integer id, Integer partId) {
        getData().set("ids." + id, partId);
        saveData();
    }


    /***
     * Loads images from data file to HashMap.
     */
    private void loadImages() {
        if (getData().contains("ids"))
            Objects.requireNonNull(getData().getConfigurationSection("ids")).getKeys(false).forEach(id -> savedImages.put(Integer.parseInt(id), getData().getInt("ids." + id)));
    }


    public boolean hasImage(int id) {
        return savedImages.containsKey(id);
    }


    public Integer getImage(int id) {
        return savedImages.get(id);
    }


    public FileConfiguration getData() {
        return dataFile.getConfig();
    }


    public void saveData() {
        dataFile.saveConfig();
    }


    /***
     *
     * @author CodedRed
     *
     * CustomFile code provided by CodedRed.
     */
    static class CustomFile {

        private final MakiScreen plugin = MakiScreen.getPlugin(MakiScreen.class);
        private FileConfiguration dataConfig = null;
        private File dataConfigFile = null;
        private final String name;

        public CustomFile(String name) {
            this.name = name;
            saveDefaultConfig();
        }

        public void reloadConfig() {
            if (dataConfigFile == null)
                dataConfigFile = new File(plugin.getDataFolder(),name);

            this.dataConfig = YamlConfiguration
                    .loadConfiguration(dataConfigFile);

            InputStream defConfigStream = plugin.getResource(name);
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration
                        .loadConfiguration(new InputStreamReader(defConfigStream));
                this.dataConfig.setDefaults(defConfig);
            }
        }

        public FileConfiguration getConfig() {
            if (this.dataConfig == null)
                reloadConfig();
            return this.dataConfig;
        }

        public void saveConfig() {
            if ((dataConfig == null) || (dataConfigFile == null))
                return;
            try {
                getConfig().save(dataConfigFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config to "
                        + dataConfigFile, e);
            }
        }

        public void saveDefaultConfig() {
            if (dataConfigFile == null)
                dataConfigFile = new File(plugin.getDataFolder(), name);
            if (!dataConfigFile.exists())
                plugin.saveResource(name, false);
        }

    }
}

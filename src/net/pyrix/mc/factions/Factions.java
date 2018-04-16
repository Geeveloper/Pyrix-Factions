package net.pyrix.mc.factions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import net.pyrix.mc.factions.player.FPlayer;
import net.pyrix.mc.factions.storage.StorageManager;

public class Factions extends JavaPlugin {

	final File factionsDir = new File(this.getDataFolder() + "/factions");
	final File playersDir = new File(this.getDataFolder() + "/players");
	final File territoryDir = new File(this.getDataFolder() + "/territories");

	private static Factions factions;

	public static Factions getInstance() {
		return factions;
	}

	public void onEnable() {
		factions = this;

		if (!new File(this.getDataFolder(), "config.yml").exists()) {
			System.out.println(ChatColor.YELLOW + "No Config Found! Generating a new one...");
			this.saveDefaultConfig(); // Retrieves config.yml stored in plugin
		}

		if (!factionsDir.exists()) {
			factionsDir.mkdir();
		}

		if (!playersDir.exists()) {
			playersDir.mkdir();
		}

		if (!territoryDir.exists()) {
			territoryDir.mkdir();
		}

		Set<Class<? extends Manager>> debugClasses = initializeClasses();
		if (debugClasses != null) {
			getLogger().info("Unsuccessfully initialized classes ->: ");
			for (Class<?> c : debugClasses) {
				getLogger().info("> " + c.getName());
			}
			return;
		}
		getLogger().info("PyrixFactions successfully initialized! ^ω^");

		StorageManager.get.LanguageStorage.setup(this);

	}

	public static FPlayer[] getAllOnlineFPlayers() {
		List<FPlayer> FPlayers = new ArrayList<FPlayer>();
		for (Player player : Bukkit.getOnlinePlayers()) {
			FPlayer fplayer = StorageManager.get.FPlayerStorage.get(player);
			if (fplayer != null) {
				FPlayers.add(fplayer);
			}
		}
		return FPlayers.toArray(new FPlayer[FPlayers.size()]);
	}

	private Set<Class<? extends Manager>> initializeClasses() {
		// Initializes only classes that extend Manager first
		Reflections reflections = new Reflections("net.pyrix.mc.factions");

		Set<Class<? extends Manager>> allManagerClasses = reflections.getSubTypesOf(Manager.class);
		for (Class<?> c : allManagerClasses) {
			try {
				c.getDeclaredMethod("onEnable").invoke(c.getConstructor().newInstance());
				getLogger().info("Debug: Enabled -> " + c.getSimpleName());
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException e) {
				e.printStackTrace();
				return allManagerClasses;
			}
		}
		return null;

	}

}

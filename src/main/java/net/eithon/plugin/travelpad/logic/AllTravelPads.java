package net.eithon.plugin.travelpad.logic;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.FileContent;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class AllTravelPads {

	private HashMap<String, TravelPadInfo> _travelPadsByBlock = null;
	private HashMap<String, TravelPadInfo> _travelPadsByName = null;

	private EithonPlugin _eithonPlugin;

	public AllTravelPads(EithonPlugin eithonPlugin) {
		this._eithonPlugin = eithonPlugin;
		this._travelPadsByBlock = new HashMap<String, TravelPadInfo>();
		this._travelPadsByName = new HashMap<String, TravelPadInfo>();
	}
	
	public void add(TravelPadInfo info) {
		this._travelPadsByName.put(info.getTravelPadName(), info);
		this._travelPadsByBlock.put(info.getBlockHash(), info);
	}

	public void remove(TravelPadInfo info) {
		this._travelPadsByName.remove(info.getTravelPadName());
		this._travelPadsByBlock.remove(info.getBlockHash());
	}

	public Collection<TravelPadInfo> getAll() {
		return this._travelPadsByName.values();
	}

	TravelPadInfo getByLocation(Location location) {
		debug("AllTravelPads.getByLocation", "Enter");
		if (this._travelPadsByBlock == null) {
			debug("AllTravelPads.getByLocation", "travelPadsByBlock == null");
			return null;
		}
		String position = TravelPadInfo.toBlockHash(location);
		if (!this._travelPadsByBlock.containsKey(position)) {
			debug("AllTravelPads.getByLocation", "No travelpads at position " + position);
			for (TravelPadInfo info : this._travelPadsByBlock.values()) {
				debug("AllTravelPads.getByLocation", String.format("TravelPad by block: %s", info.toString()));
			}
			for (TravelPadInfo info : this._travelPadsByName.values()) {
				debug("AllTravelPads.getByLocation", String.format("TravelPad by name: %s", info.toString()));
			}
			return null;
		}
		TravelPadInfo info = this._travelPadsByBlock.get(position);
		debug("AllTravelPads.getByLocation", String.format("Found a travelpad: %s.", info.toString()));
		return info;
	}

	public TravelPadInfo getByName(String name) {
		if (!this._travelPadsByName.containsKey(name)) return null;
		return this._travelPadsByName.get(name);
	}

	public void delayedSave(JavaPlugin plugin, double seconds)
	{
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				save();
			}
		}, TimeMisc.secondsToTicks(seconds));		
	}

	public void delayedLoad(JavaPlugin plugin, double seconds)
	{
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				load();
			}
		}, TimeMisc.secondsToTicks(seconds));		
	}

	@SuppressWarnings("unchecked")
	public
	void save() {
		JSONArray travelPads = new JSONArray();
		for (TravelPadInfo travelPadInfo : getAll()) {
			travelPads.add(travelPadInfo.toJson());
		}
		if ((travelPads == null) || (travelPads.size() == 0)) {
			this._eithonPlugin.getEithonLogger().info("No TravelPads saved.");
			return;
		}
		this._eithonPlugin.getEithonLogger().info("Saving %d TravelPads", travelPads.size());
		File file = getTravelPadStorageFile();
		
		FileContent fileContent = new FileContent("TravelPad", 1, travelPads);
		fileContent.save(file);
	}

	private File getTravelPadStorageFile() {
		File file = this._eithonPlugin.getDataFile("travelpads.json");
		return file;
	}

	void load() {
		File file = getTravelPadStorageFile();
		FileContent fileContent = FileContent.loadFromFile(file);
		if (fileContent == null) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "File was empty.");
			return;			
		}
		JSONArray array = (JSONArray) fileContent.getPayload();
		if ((array == null) || (array.size() == 0)) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "The list of TravelPads was empty.");
			return;
		}
		this._eithonPlugin.getEithonLogger().info("Restoring %d TravelPads from loaded file.", array.size());
		this._travelPadsByBlock = new HashMap<String, TravelPadInfo>();
		this._travelPadsByName = new HashMap<String, TravelPadInfo>();
		for (int i = 0; i < array.size(); i++) {
			TravelPadInfo info = new TravelPadInfo();
			info.fromJson((JSONObject) array.get(i));
			this.add(info);
		}
		for (TravelPadInfo info : this._travelPadsByName.values()) {
			if (!info.isJumpPad()) {
				TravelPadInfo target = getByLocation(info.getTargetLocation());
				if (target == null) continue;
				info.setTarget(target);
			}
		}
	}

	void debug(String method, String message) {
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "%s: %s", method, message);
	}
}

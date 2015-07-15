package net.eithon.plugin.travelpad;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.travelpad.logic.Controller;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public final class EventListener implements Listener {
	
	private Controller _controller;
	private EithonPlugin _eithonPlugin;
	
	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
		this._eithonPlugin = eithonPlugin;
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		debug("onPlayerInteractEvent", "Enter");
		if (event.isCancelled()) {
			debug("onPlayerInteractEvent", "Event was already cancelled");
			return;
		}
		if (event.getAction() != Action.PHYSICAL) {
			debug("onPlayerInteractEvent", "Event was not Action.PHYSICAL");
			return;
		}
		Player player = event.getPlayer();
		Block pressurePlate = event.getClickedBlock();
		if (pressurePlate == null) {
			debug("onPlayerInteractEvent", "Not a clicked block");
			return;
		}
		if (pressurePlate.getType() != Material.STONE_PLATE) {
			debug("onPlayerInteractEvent", "Not a STONE_PLATE");
			return;
		}
		debug("onPlayerInteractEvent", "We are ready for teleport");
		this._controller.maybeTravel(player, pressurePlate);
	}

	private void debug(String method, String message) {
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "%s: %s", method, message);
	}
}

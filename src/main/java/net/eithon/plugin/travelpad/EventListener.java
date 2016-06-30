package net.eithon.plugin.travelpad;

import net.eithon.library.extensions.EithonPlugin;
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
		verbose("onPlayerInteractEvent", "Enter");
		if (event.isCancelled()) {
			verbose("onPlayerInteractEvent", "Event was already cancelled");
			return;
		}
		if (event.getAction() != Action.PHYSICAL) {
			verbose("onPlayerInteractEvent", "Event was not Action.PHYSICAL");
			return;
		}
		Player player = event.getPlayer();
		Block pressurePlate = event.getClickedBlock();
		if (pressurePlate == null) {
			verbose("onPlayerInteractEvent", "Not a clicked block");
			return;
		}
		if (pressurePlate.getType() != Material.STONE_PLATE) {
			verbose("onPlayerInteractEvent", "Not a STONE_PLATE");
			return;
		}
		verbose("onPlayerInteractEvent", "We are ready for teleport");
		this._controller.maybeTravel(player, pressurePlate);
	}
	
	private void verbose(String method, String format, Object... args)
	{
		this._eithonPlugin.dbgVerbose("EventListener", method, format, args);
	}
}

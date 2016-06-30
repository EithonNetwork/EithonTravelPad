package net.eithon.plugin.travelpad.logic;

import java.util.ArrayList;

import net.eithon.library.extensions.EithonLocation;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.move.IBlockMoverFollower;
import net.eithon.library.move.MoveEventHandler;
import net.eithon.library.time.CoolDown;
import net.eithon.plugin.travelpad.Config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public class Controller implements IBlockMoverFollower {

	net.eithon.library.core.PlayerCollection<TravellerInfo> _travellers = null;
	CoolDown _coolDown = null;
	private AllTravelPads _allTravelPads = null;
	private EithonPlugin _eithonPlugin = null;

	public Controller(EithonPlugin eithonPlugin){
		this._eithonPlugin = eithonPlugin;
		this._coolDown = new CoolDown("travelpad", Config.V.secondsToPauseBeforeNextTeleport);
		this._travellers = new net.eithon.library.core.PlayerCollection<TravellerInfo>();
		this._allTravelPads = new AllTravelPads(eithonPlugin);
		this._allTravelPads.delayedLoad(eithonPlugin, Config.V.secondsBeforeLoad);
	}

	public boolean createOrUpdateTravelPad(Player player, String name, double upSpeed, double forwardSpeed) {
		EithonLocation eithonLocation = new EithonLocation(player.getLocation());
		Block pressurePlate = eithonLocation.searchForFirstBlockOfMaterial(Material.STONE_PLATE, 3);
		if (pressurePlate == null) {
			player.sendMessage("No stone plate within 3 blocks.");
			return false;
		}

		Location playerLocation = player.getLocation();
		Location padLocation = pressurePlate.getLocation();
		// Remember where the player looked when the travelpad was created/updated
		padLocation.setYaw(playerLocation.getYaw());
		padLocation.setPitch(playerLocation.getPitch());

		TravelPadInfo travelPadInfo = this._allTravelPads.getByName(name);
		if ((upSpeed != 0.0) || (forwardSpeed != 0.0)) {
			if (travelPadInfo != null) travelPadInfo.setVelocity(upSpeed, forwardSpeed, playerLocation.getYaw());
			else travelPadInfo = new TravelPadInfo(name, padLocation, upSpeed, forwardSpeed, playerLocation.getYaw(), player);
		} else {
			if (travelPadInfo != null) travelPadInfo.setTarget(travelPadInfo);
			else travelPadInfo = new TravelPadInfo(name, padLocation, padLocation, player);
		}
		this._allTravelPads.add(travelPadInfo);
		if (player != null) coolDown(player);
		save();
		return true;
	}

	public void maybeTravel(Player player, Block pressurePlate) {
		debug("maybeTravel", "Enter");

		if (isAboutToTele(player)) {
			debug("maybeTravel", "Player already waiting for travel to happen");
			return;
		}

		if (isInCoolDownPeriod(player)) {
			debug("maybeTravel", "Player is in cool down period");
			return;
		}

		Location location = pressurePlate.getLocation();
		TravelPadInfo info = this._allTravelPads.getByLocation(location);
		if (info == null) return;

		debug("maybeTravel", "Travel sequence is starting");
		travelSoon(player, info);
	}

	boolean isAboutToTele(Player player) {
		TravellerInfo travellerInfo = this._travellers.get(player);
		if (travellerInfo == null) return false;
		return travellerInfo.isAboutToTele();
	}

	private void travelSoon(Player player, TravelPadInfo info) {
		debug("travelSoon", "Enter");
		TravellerInfo travellerInfo = new TravellerInfo(player, info);
		this._travellers.put(player,  travellerInfo);
		MoveEventHandler.addBlockMover(player, this);
		if (info.isJumpPad()) {
			debug("travelSoon", "No potion effects for jump.");
			debug("travelSoon", "Call delayedJump");
			delayedJump(player, info, travellerInfo);	
		} else {
			debug("travelSoon", "Add potion effects for teleport.");
			addPotionEffects(player, travellerInfo);
			delayedRemoveEffects(player, travellerInfo);
			debug("travelSoon", "Call delayedTeleport");
			delayedTeleport(player, info, travellerInfo);
		}
		debug("teleSoon", "Leave");
	}

	private void addPotionEffects(Player player, TravellerInfo travellerInfo) {
		ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
		PotionEffect nausea = null;
		if (Config.V.nauseaTicks > 0) {
			debug("addPotionEffects", "Add nausea");
			nausea = new PotionEffect(PotionEffectType.CONFUSION, (int) Config.V.nauseaTicks, 4);
			effects.add(nausea);
			travellerInfo.setNausea(true);
		}
		PotionEffect slowness = null;
		if (Config.V.slownessTicks > 0) {
			debug("addPotionEffects", "Add slowness");
			slowness = new PotionEffect(PotionEffectType.SLOW, (int) Config.V.slownessTicks, 4);
			effects.add(slowness);
			travellerInfo.setSlowness(true);
		}
		PotionEffect blindness = null;
		if (Config.V.blindnessTicks > 0) {
			debug("addPotionEffects", "Add blindness");
			blindness = new PotionEffect(PotionEffectType.BLINDNESS, (int) Config.V.blindnessTicks, 4);
			effects.add(blindness);
			travellerInfo.setBlindness(true);
		}
		player.addPotionEffects(effects);
	}

	private void delayedRemoveEffects(
			Player player,
			TravellerInfo travellerInfo) {
		Controller thisController = this;
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				TravellerInfo latestJumperInfo = thisController._travellers.get(player);
				if (!travellerInfo.isSame(latestJumperInfo)){
					debug("delayedRemoveEffects", "There seems to exist a new travel. Skip this.");
					return;
				}	
				debug("delayedRemoveEffects", "Remove effects");
				removeEffects(player, travellerInfo);
			}
		}, Config.V.disableEffectsAfterTicks);
	}

	private void delayedJump(
			Player player, 
			TravelPadInfo info,
			TravellerInfo travellerInfo) {
		debug("delayedJump", "Enter");
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				debug("delayedJump task", "Prepare");
				if (!prepareAndIsReadyToTravel(player, travellerInfo)) return;
				debug("delayedJump task", "JUMP!");
				jump(player, info);
			}
		}, Config.V.ticksBeforeJump);
		debug("delayedJump", "Leave");
	}

	private void delayedTeleport(
			Player player, 
			TravelPadInfo info,
			TravellerInfo travellerInfo) {
		debug("delayedTeleport", "Enter");
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				debug("delayedTeleport task", "Prepare");
				if (!prepareAndIsReadyToTravel(player, travellerInfo)) return;
				debug("delayedTeleport task", "TELEPORT!");
				teleport(player, info);
			}
		}, Config.V.ticksBeforeTele);
		debug("delayedTeleport", "Leave");
	}

	boolean prepareAndIsReadyToTravel(Player player, TravellerInfo travellerInfo) {
		debug("prepareForJumpOrTele", "Enter");
		TravellerInfo latestJumperInfo = this._travellers.get(player);
		if (!travellerInfo.isSame(latestJumperInfo)){
			debug("prepareForJumpOrTele", "A new travel has been initiated for this player. Skip this one.");
			return false;
		}
		debug("prepareForJumpOrTele", "Last chance to change our mind");
		if (!isAboutToTele(player)) {
			debug("delayedTeleport", "The travel seems to have been cancelled");
			return false;
		}
		travellerInfo.setAboutToTravel(false);
		if (travellerInfo.canBeRemoved()) this._travellers.remove(player);
		MoveEventHandler.removeBlockMover(player, this);
		coolDown(player);
		return true;
	}

	/*
	private float stopPlayer(Player player) {
		float walkSpeed = player.getWalkSpeed();
		player.setWalkSpeed(0.0F);
		player.setVelocity(new Vector(0.0, 0.0, 0.0));
		return walkSpeed;
	}
	 */

	void teleport(Player player, TravelPadInfo info) {
		Location targetLocation = info.getTargetLocation();
		player.teleport(targetLocation);
		final String welcomeMessage = info.getWelcomeMessage();
		if ((welcomeMessage == null) || (welcomeMessage.isEmpty())) return;
		player.sendMessage(welcomeMessage);
	}

	void jump(Player player, TravelPadInfo info) {
		Vector jumpPadVelocity = info.getVelocity();
		Vector velocity = new Vector(jumpPadVelocity.getX(), jumpPadVelocity.getY(), jumpPadVelocity.getZ());
		player.setVelocity(velocity);
	}

	public void coolDown(Player player) {
		this._coolDown.addIncident(player);
	}

	public boolean isInCoolDownPeriod(Player player) {
		return this._coolDown.isInCoolDownPeriod(player);
	}

	void removeEffects(Player player, TravellerInfo travellerInfo) {
		travellerInfo.removeEffects();
		if (travellerInfo.canBeRemoved()) this._travellers.remove(player);
	}

	void debug(String method, String message) {
		this._eithonPlugin.dbgVerbose("Controller", method, "%s", message);
	}

	@Override
	public void moveEventHandler(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		debug("TravelPad.moveEventHandler", String.format("Enter (for player %s)", player.getName()));
		if (maybeStopTravel(player)) {
			debug("TravelPad.moveEventHandler", String.format("Stop following player %s", player.getName()));
			MoveEventHandler.removeBlockMover(player, this);
		}
		debug("TravelPad.moveEventHandler", "Leave");
	}

	public boolean maybeStopTravel(Player player) {
		debug("maybeStopTravel", String.format("Enter (for player %s)", player.getName()));
		TravellerInfo travellerInfo = this._travellers.get(player);
		if ((travellerInfo == null) || !travellerInfo.isAboutToTele()) {
			debug("maybeStopTravel", "Player is not about to travel.");
			debug("maybeStopTravel", "Leave");
			return false;
		}
		Block block = player.getLocation().getBlock();
		if ((block != null) && (block.getType() == Material.STONE_PLATE)) {
			debug("maybeStopTravel", "Player is still on stone plate.");
			debug("maybeStopTravel", "Leave");
			return false;
		}
		
		if (travellerInfo.playerIsConsideredOnTravelPad()) {
			debug("maybeStopTravel", "Player is considered to be close enough to the travel pad to continue travelling.");
			debug("maybeStopTravel", "Leave");
			return false;
		}
		travellerInfo.setAboutToTravel(false);
		this._travellers.remove(player);
		removeEffects(player, travellerInfo);
		this._coolDown.removePlayer(player);
		Config.M.movedOffTravelPad.sendMessage(player);
		debug("maybeStopTravel", "Leave");
		return true;
	}

	public TravelPadInfo getByNameOrInformUser(CommandSender sender, String name) {
		TravelPadInfo info = this._allTravelPads.getByName(name);
		if (info != null) return info;
		Config.M.unknownTravelPad.sendMessage(sender, name);
		return null;
	}

	public boolean verifyNameIsNew(Player player, String name) {
		TravelPadInfo info = this._allTravelPads.getByName(name);
		if (info != null)
		{
			player.sendMessage("TravelPad already exists: " + name);
			return true;		
		}
		return true;
	}

	@Override
	public String getName() {
		return this._eithonPlugin.getName();
	}

	public void save() {
		this._allTravelPads.delayedSave(this._eithonPlugin, 0.0);	}

	public void remove(TravelPadInfo info) {
		this._allTravelPads.remove(info);	
		save();
	}

	public void addMessage(TravelPadInfo info, String message) {
		info.setWelcomeMessage(message);
	}

	public void link(TravelPadInfo info1, TravelPadInfo info2) {
		info1.setTarget(info2);
		info2.setTarget(info1);
		save();
	}

	public void gotoTravelPad(Player player, TravelPadInfo info) {	
		player.teleport(info.getSourceAsTarget());
		coolDown(player);
	}

	public void listTravelPads(Player player) {
		for (TravelPadInfo info : this._allTravelPads.getAll()) {
			player.sendMessage(info.toString());
		}
	}
}

package net.eithon.plugin.travelpad.logic;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class TravellerInfo {
	private UUID _id;
	private boolean _nausea;
	private boolean _slowness;
	private boolean _blindness;
	private Player _player;
	private boolean _aboutToTele;
	private boolean _effectsRemoved;
	private TravelPadInfo _travelPadInfo;
	private Location _enterLocation;

	public TravellerInfo(Player player, TravelPadInfo info) {
		this._player = player;
		this._aboutToTele = true;
		this._id = UUID.randomUUID();
		this._travelPadInfo = info;
		this._enterLocation = player.getLocation();
	}
	
	public boolean isSame(TravellerInfo info) {
		return (info != null) && (info._id == this._id);
	}
	
	public boolean playerIsConsideredOnTravelPad() {
		Location currentLocation = this._player.getLocation();
		if  (currentLocation.distance(this._enterLocation) < 1.0) return true;
		return currentLocation.distance(this._travelPadInfo.getSourceAsTarget()) < 1.0;
	}

	public boolean hasNausea() { return this._nausea; }

	public void setNausea(boolean nausea) { this._nausea = nausea; }

	public boolean hasSlowness() { return this._slowness; }

	public void setSlowness(boolean slowness) { this._slowness = slowness; }

	public boolean hasBlindness() { return this._blindness; }

	public void setBlindness(boolean blindness) { this._blindness = blindness; }

	public boolean isAboutToTele() { return this._aboutToTele; }

	public void setAboutToTravel(boolean aboutToTele) { this._aboutToTele = aboutToTele; }

	public boolean canBeRemoved() { return !this._aboutToTele && this._effectsRemoved; }

	public void removeEffects() { 
		if (this._nausea) {
			this._player.removePotionEffect(PotionEffectType.CONFUSION);
			this._nausea = false;
		}
		if (this._slowness) {
			this._player.removePotionEffect(PotionEffectType.SLOW);
			this._slowness = false;
		}
		if (this._blindness) {
			this._player.removePotionEffect(PotionEffectType.BLINDNESS);
			this._blindness = false;
		}
		this._effectsRemoved = true;
	}
}

package net.eithon.plugin.travelpad.logic;

import java.util.HashMap;

import net.eithon.library.extensions.EithonLocation;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.json.IJson;
import net.eithon.plugin.travelpad.Config;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.json.simple.JSONObject;

public class TravelPadInfo implements IJson<TravelPadInfo> {
	private EithonLocation _sourceLocation;
	private EithonLocation _targetLocation;
	private TravelPadInfo _targetTravelPad;
	private double _upSpeed;
	private double _forwardSpeed;
	private float _yaw;
	private Vector _velocity;
	private String _travelPadName;
	private EithonPlayer _creator;
	private boolean _hasVelocity;

	private TravelPadInfo(String name, Location sourceLocation, Player creator)
	{
		this._travelPadName = name;
		this._sourceLocation = new EithonLocation(sourceLocation);
		if (creator != null)
		{
			this._creator = new EithonPlayer(creator);
		} else {
			this._creator = null;
		}
	}

	public TravelPadInfo(String name, Location sourceLocation, Location targetLocation, Player creator)
	{
		this(name, sourceLocation, creator);
		this._targetLocation = new EithonLocation(targetLocation);
		this._hasVelocity = false;
	}

	public TravelPadInfo(String name, Location sourceLocation, double upSpeed, double forwardSpeed, float yaw, Player creator)
	{
		this(name, sourceLocation, creator);
		setVelocity(upSpeed, forwardSpeed, yaw);
	}

	void setVelocity(double upSpeed, double forwardSpeed, float yaw) {
		this._upSpeed = upSpeed;
		this._forwardSpeed = forwardSpeed;
		this._yaw = yaw;
		this._velocity = convertToVelocityVector(upSpeed, forwardSpeed, yaw);
		this._hasVelocity = true;
	}

	TravelPadInfo() {
	}

	public boolean hasVelocity() { return this._hasVelocity; }

	public boolean isJumpPad() { return hasVelocity(); }

	@Override
	public TravelPadInfo factory() {
		return new TravelPadInfo();
	}

	@Override
	public TravelPadInfo fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._travelPadName = (String) jsonObject.get("name");
		this._sourceLocation = EithonLocation.getFromJson(jsonObject.get("sourceLocation"));
		this._hasVelocity = (boolean) jsonObject.get("hasVelocity");
		if (this._hasVelocity) {
			velocityFromJson(jsonObject.get("velocity"));
		} else {
			this._targetLocation = EithonLocation.getFromJson(jsonObject.get("targetLocation"));
		}
		this._creator = EithonPlayer.getFromJSon(jsonObject.get("creator"));
		return this;
	}

	private void velocityFromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._upSpeed = (double) jsonObject.get("upSpeed");
		this._forwardSpeed = (double) jsonObject.get("forwardSpeed");
		this._yaw = (float) (double) jsonObject.get("yaw");
		this._velocity = convertToVelocityVector(this._upSpeed, this._forwardSpeed, this._yaw);
	}

	public static TravelPadInfo createFromJson(Object json) {
		TravelPadInfo info = new TravelPadInfo();
		return info.fromJson(json);
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("name", this._travelPadName);
		json.put("sourceLocation", this._sourceLocation.toJson());
		json.put("hasVelocity", this._hasVelocity);
		if (this._hasVelocity) {
			json.put("velocity", velocityToJson());
		} else {
			json.put("targetLocation", this._targetLocation.toJson());
		}
		json.put("creator", this._creator.toJson());
		return json;
	}

	@SuppressWarnings("unchecked")
	private Object velocityToJson() {
		JSONObject json = new JSONObject();
		json.put("upSpeed", this._upSpeed);
		json.put("forwardSpeed", this._forwardSpeed);
		json.put("yaw", this._yaw);
		return json;
	}

	Location getTargetLocation() {
		return this._targetLocation.getLocation();
	}

	Vector getVelocity() {
		return this._velocity;
	}

	public void setTarget(TravelPadInfo target) {
		this._targetTravelPad = target;
		this._targetLocation = new EithonLocation(target.getSourceAsTarget());
		this._hasVelocity = false;
	}

	String getTravelPadName() {
		return this._travelPadName;
	}

	Location getSource() {
		return this._sourceLocation.getLocation();
	}

	public Location getSourceAsTarget() {
		Location location = this._sourceLocation.getLocation().clone();
		location.setX(location.getX() + 0.5);
		location.setZ(location.getZ() + 0.5);
		return location;
	}

	String getBlockHash() {
		return TravelPadInfo.toBlockHash(this._sourceLocation);
	}

	private static String toBlockHash(EithonLocation location)
	{
		if (location == null) return null;
		return toBlockHash(location.getLocation());
	}

	static String toBlockHash(Location location)
	{
		if (location == null) return null;
		return toBlockHash(location.getBlock());
	}

	static String toBlockHash(Block block)
	{
		return String.format("%d;%d;%d", block.getX(), block.getY(), block.getZ());
	}

	Player getCreator()
	{
		return this._creator.getPlayer();
	}

	public String getPlayerName() {
		return this._creator.getName();
	}

	public String toString() {
		HashMap<String,String> namedArguments = getNamedArguments();
		if (isJumpPad()) {
			return Config.M.jumpInfo.getMessage(namedArguments);
		} else {
			return Config.M.teleportInfo.getMessage(namedArguments);			
		}
	}	

	private HashMap<String,String> getNamedArguments() {
		HashMap<String,String> namedArguments = new HashMap<String, String>();
		namedArguments.put("NAME", getTravelPadName());
		if (isJumpPad()) {
			namedArguments.put("LINKED_TO", "-");
			namedArguments.put("UP_SPEED", Double.toString(this._upSpeed));
			namedArguments.put("FORWARD_SPEED", Double.toString(this._forwardSpeed));
			namedArguments.put("VELOCITY", Double.toString(this._velocity.length()));
		} else {
			namedArguments.put("UP_SPEED", "-");
			namedArguments.put("FORWARD_SPEED", "-");
			namedArguments.put("VELOCITY", "-");
			if (this._targetTravelPad == null) {
				namedArguments.put("LINKED_TO", getTargetLocation().toString());
			} else {
				namedArguments.put("LINKED_TO", this._targetTravelPad.getTravelPadName());				
			}
		}

		return namedArguments;
	}

	private Vector convertToVelocityVector(double upSpeed, double forwardSpeed, double yaw) {
		double rad = yaw*Math.PI/180.0;
		double vectorX = -Math.sin(rad)*forwardSpeed;
		double vectorY = upSpeed;
		double vectorZ = Math.cos(rad)*forwardSpeed;
		Vector velocityVector = new Vector(vectorX, vectorY, vectorZ);
		return velocityVector;
	}
}

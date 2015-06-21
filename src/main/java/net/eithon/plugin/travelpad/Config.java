package net.eithon.plugin.travelpad;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;

public class Config {
	public static void load(EithonPlugin plugin)
	{
		Configuration config = plugin.getConfiguration();
		V.load(config);
		C.load(config);
		M.load(config);

	}
	public static class V {
		public static long ticksBeforeTele;
		public static long ticksBeforeJump;
		public static long nauseaTicks;
		public static long slownessTicks;
		public static long blindnessTicks;
		public static long disableEffectsAfterTicks;
		public static int secondsToPauseBeforeNextTeleport;
		
		static void load(Configuration config) {
			ticksBeforeTele = config.getInt("TeleportAfterTicks", 100);
			ticksBeforeJump = config.getInt("JumpAfterTicks", 0);
			nauseaTicks = config.getInt("NauseaTicks", 200);
			slownessTicks = config.getInt("SlownessTicks", 0);
			blindnessTicks = config.getInt("BlindnessTicks", 0);
			disableEffectsAfterTicks = config.getInt("DisableEffectsAfterTicks", 120);
			secondsToPauseBeforeNextTeleport = config.getInt("SecondsToPauseBeforeNextTeleport", 5);
		}
	}
	public static class C {

		static void load(Configuration config) {
		}

	}
	public static class M {
		public static ConfigurableMessage travelPadAdded;	
		public static ConfigurableMessage nextStepAfterAdd;	
		public static ConfigurableMessage travelPadRemoved;
		public static ConfigurableMessage travelPadsLinked;
		public static ConfigurableMessage gotoTravelPad;
		public static ConfigurableMessage movedOffTravelPad;
		public static ConfigurableMessage unknownTravelPad;
		public static ConfigurableMessage jumpInfo;
		public static ConfigurableMessage teleportInfo;

		static void load(Configuration config) {
			travelPadAdded = config.getConfigurableMessage("messages.TravelPadAdded", 1,
					"TravelPad %s has been added.");
			nextStepAfterAdd = config.getConfigurableMessage("messages.NextStepAfterAdd", 1,
					"Now link this travelpad with command /travelpad link %s <other travelpad name>.");
			travelPadRemoved = config.getConfigurableMessage("messages.TravelPadRemoved", 1,
					"TravelPad %s has been removed.");
			travelPadsLinked = config.getConfigurableMessage("messages.TravelPadsLinked", 2,
					"TravelPad %s and %s has been linked.");
			gotoTravelPad = config.getConfigurableMessage("messages.GotoTravelPad", 1,
					"You have been teleported to TravelPad %s.");
			movedOffTravelPad = config.getConfigurableMessage("messages.MovedOffTravelPad_0", 0,
					"You moved away from the travelpad, cancelling teleport.");
			String[] parameterNames = {"NAME", "VELOCITY", "UP_SPEED", "FORWARD_SPEED", "LINKED_TO"};
			jumpInfo = config.getConfigurableMessage("messages.JumpInfo", 0,
					"%NAME%: Up %UP_SPEED%, forward %FORWARD_SPEED%", parameterNames);
			teleportInfo = config.getConfigurableMessage("messages.TeleportInfo", 0,
					"%NAME%: Linked to %LINKED_TO%", parameterNames);
		}		
	}

}

# TravelPadPlugin

A TravelPad plugin for Minecraft.
A TravelPad is a block that will throw away a user that steps on it in a certain direction.

## Prerequisits

* There must be a directory under CraftBukkit/plugins, named "TravelPad". This is where this plugin will save information about all existing TravelPads.

## Functionality

The plugin provides administrative commands for handling TravelPads, a way of saving and loading the created TravelPads to/from file storage and implementing the actual launching of players that step on a TravelPad.

### Administrative commands

* add: Add a new named TravelPad located to the STONE_PLATE block that is closest to the player
* target: Sets the target destination
* remove: Remove an existing TravelPad
* goto: Teleport to an existing TravelPad, temporarily stops the TravelPad from working for this player so that he/she is not thrown away immediately
* list: Show a list with all existing TravelPads

### The actual Jump

When a user steps on a TravelPad, he/she will first be thrown up in the air (according to the parameters for that TravelPad). When the user has reached the maximum height, he/she is shot away (according to the parameters for that TravelPad) in the direction that the creator of the TravelPad was looking when the TravelPad was created.

### Restrictions

A player must have the "TravelPad.jump" permission to use a TravelPad. If he/she doesn't, then he/she will be informed that she needs to read the server rules first.

## Release history

### 1.3 (2015-08-10)

* CHANGE: All time span configuration values are now in the general TimeSpan format instead of hard coded to seconds or minutes or hours.

### 1.2 (2015-07-14)

* NEW: TravelPads of type teleport now can have a welcome message.

### 1.1.1 (2015-07-08)

* BUG: Now shows subcommands if no subcommand was given.

### 1.0 (2015-04-18)

* NEW: First Eithon release

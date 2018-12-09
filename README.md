# Minecraft Hue
A Minecraft mod that syncs the game with your Hue smart lights.

## Description
This is my first Minecraft mod. It will sync your Philips Hue smart lights completely with Minecraft, making them react to changes in light, color, and biome in real-time. The lights will also respond to lightning and player damage events. 

## How To Use
The mod will work with Minecraft Forge running MC 1.12.2 (this is the only version I’ve tested on so far). However, it’s not doing anything crazy, so I suspect it will work on any modern Minecraft version.

The plugin adds two commands:

**/registerLightIP [Hue Hub Local IP] [Light Group Name]**

**/deregisterLightIP**

You can find the local IP of your Hue Bridge/Hub easily using their mobile app. You do not need to provide a light group, but the mod will connect to all of your Hue lights by default if you don’t. The group name can have spaces but it currently only accepts one group.

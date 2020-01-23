package io.github.derangedmango.minecrafthuemod;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Config(modid = MinecraftHueMod.MODID)
public class ModConfig extends Configuration {
	
	@Config.Comment("Mod run and transition rate settings")
	public static final Map<String, Integer> rate_settings = new HashMap<>();
	
	@Config.Comment("Network settings")
	public static final Map<String, String> network_settings = new HashMap<>();

	static {
		rate_settings.put("Color Rate", 7);
		rate_settings.put("Normal Rate", 3);
		rate_settings.put("Run Rate", 4);
		network_settings.put("Light Group Name", "");
		network_settings.put("Bridge IP", "");
		network_settings.put("Authorization", "null");
	}
	
	public ModConfig(File f) {
		super(f);
	}
	
	public void setNetwork(String ip, String group) {
		ModConfig.network_settings.put("Bridge IP", ip);
		ModConfig.network_settings.put("Light Group Name", group);
		ConfigManager.sync(MinecraftHueMod.MODID, Config.Type.INSTANCE);
	}
	
	public void setAuth(String auth) {
		ModConfig.network_settings.put("Authorization", auth);
		ConfigManager.sync(MinecraftHueMod.MODID, Config.Type.INSTANCE);
	}
	
	public String[] getNetwork() {
		return new String[] {
				ModConfig.network_settings.get("Bridge IP"),
				ModConfig.network_settings.get("Light Group Name")
		};
	}
	
	public String getAuth() {
		return ModConfig.network_settings.get("Authorization");
	}
	
	@Mod.EventBusSubscriber(modid = MinecraftHueMod.MODID)
	private static class EventHandler {
		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
			if(event.getModID().equals(MinecraftHueMod.MODID)) {
				ConfigManager.sync(MinecraftHueMod.MODID, Config.Type.INSTANCE);
				
				MinecraftHueMod.setConfigMetadata();
				
				if(MinecraftHueMod.task != null) {
					MinecraftHueMod.task.setColorRate(ModConfig.rate_settings.get("Color Rate"));
					MinecraftHueMod.task.setNormalRate(ModConfig.rate_settings.get("Normal Rate"));
					MinecraftHueMod.task.resetConInfo();
					MinecraftHueMod.task.resume();
				}
			}
		}
	}
}

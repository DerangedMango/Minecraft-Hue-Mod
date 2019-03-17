package io.github.derangedmango.minecrafthuemod;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Scanner;

@Mod(modid = MinecraftHueMod.MODID, name = MinecraftHueMod.NAME, version = MinecraftHueMod.VERSION)
public class MinecraftHueMod {
    public static final String MODID = "minecraft-hue";
    public static final String NAME = "Minecraft Hue";
    public static final String VERSION = "1.0";

    private static Logger logger;
    private static File configDir;
    public static ModConfig config;
    public static DimLights task;
    
    private int runRate = 4;
    private int colorRate = 7;
    private int normalRate = 3;
	private int[] alphaArr = new int[26];
	private BlockConfig[] blockConfigArr;
	private String sessionUser = "";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	logger = event.getModLog();
        configDir = new File(event.getModConfigurationDirectory().getAbsolutePath());
        config = new ModConfig(new File(configDir + "/minecraft-hue.cfg"));
        sessionUser = Minecraft.getMinecraft().getSession().getUsername();
        
        int[] rateArr = setConfigMetadata();
        
        if(rateArr != null) {
        	colorRate = Math.min(Math.max(rateArr[0], 0), 10);
        	normalRate = Math.min(Math.max(rateArr[1], 0), 10);
        	runRate = Math.max(rateArr[2], 2);
        }
                
        task = new DimLights(null, colorRate, normalRate, blockConfigArr, alphaArr);
        task.pause();
        
        ClientCommandHandler.instance.registerCommand(new RegisterLightIPCommand(sessionUser));
        ClientCommandHandler.instance.registerCommand(new DeregisterLightIPCommand(sessionUser));
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("Minecraft Hue is enabled");
        
        generateDataFiles();
		
		File blockConfig = new File(configDir, "minecraft-hue-blocks.txt");
		int entryCounter = 0;

		try(Scanner scanner = new Scanner(blockConfig)) {
			while(scanner.hasNextLine()) {
				scanner.nextLine();
				entryCounter++;
			}

			scanner.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}

		blockConfigArr = new BlockConfig[entryCounter];
		
		for(int i = 0; i < alphaArr.length; i++) {
			alphaArr[i] = blockConfigArr.length;
		}

		try(Scanner scanner = new Scanner(blockConfig)) {
			int index = 0;
			int lastChar = -1;
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				String name = line.substring(0, line.indexOf("#")).toUpperCase();
				String biome = line.substring(line.indexOf("#") + 1, line.indexOf(":")).toUpperCase();
				int hue = Integer.valueOf(line.substring(line.indexOf(":") + 1, line.indexOf(",")));
				int sat = Integer.valueOf(line.substring(line.indexOf(",") + 1));

				int currChar = line.toUpperCase().charAt(0);
				currChar -= 65;

				if(currChar != lastChar) alphaArr[currChar] = index;

				blockConfigArr[index++] = new BlockConfig(name, biome, hue, sat);
				lastChar = currChar;
			}
			
			scanner.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
		
		MinecraftForge.EVENT_BUS.register(
				new EventListener(runRate, colorRate, normalRate, alphaArr, blockConfigArr, sessionUser));
	}
    
    public static int[] setConfigMetadata() {
    	int[] returnArr = null;
    	
    	try {
            config.load();
            
            Iterator<ConfigCategory> it = config.getCategory(Configuration.CATEGORY_GENERAL).getChildren().iterator();
            
            Property colorRateProp = null, normalRateProp = null, runRateProp = null;
            Property ipProp = null, groupProp = null, authProp = null;
            
            while(it.hasNext()) {
            	ConfigCategory subcat = it.next();
            	String subcatName = subcat.getName();
            	
            	if(subcatName.equalsIgnoreCase("rate_settings")) {
            		subcat.setComment("Mod run and transition rate settings");
            		
            		colorRateProp = subcat.get("Color Rate");
            		normalRateProp = subcat.get("Normal Rate");
            		runRateProp = subcat.get("Run Rate");
            	} else if(subcatName.equalsIgnoreCase("network_settings")) {
            		subcat.setComment("Network Settings");
            		
            		ipProp = subcat.get("Bridge IP");
            		groupProp = subcat.get("Light Group Name");
            		authProp = subcat.get("Authorization");
            	}
            }
            
            colorRateProp.setComment("Transition rate on color change (in 100ms).\nDefault = 7 (700ms); Min = 0, Max = 10 (1s).");
            normalRateProp.setComment("Transition rate on light level change (in 100ms).\nDefault = 3 (300ms); Min = 0, Max = 10 (1s).");
            runRateProp.setComment("Rate at which mod rechecks surroundings (in ticks).\nDefault = 4 (200ms); Min = 2 (100ms).");
            
            ipProp.setComment("Local IP address of Philips Hue Bridge.");
            groupProp.setComment("Name of light group affected by mod (defaults to \"ALL\" on registration).");
            authProp.setComment("Authorization string for network communication (you probably don't want to change this).");
            
            runRateProp.setRequiresMcRestart(true);
            
            returnArr = new int[] {
            		colorRateProp.getInt(),
            		normalRateProp.getInt(),
            		runRateProp.getInt()
            };
        } catch(Exception e) {
        	System.out.println("Error loading config, returning to default variables.");
        	e.printStackTrace();
        } finally {
            config.save();
        }
    	
    	return returnArr;
    }

    private void generateDataFiles() {
		File blockConfig = new File(configDir, "minecraft-hue-blocks.txt");

		if(!blockConfig.exists()) {
			try {
				blockConfig.createNewFile();

				try(FileWriter fw = new FileWriter(blockConfig.getPath(), false);
					BufferedWriter bw = new BufferedWriter(fw);
			    	PrintWriter out = new PrintWriter(bw)) {

					out.println("BLUE_WOOL#DEFAULT:46014,254");
					out.println("BROWN_WOOL#DEFAULT:7194,231");
					out.println("CYAN_WOOL#DEFAULT:41385,233");
					out.println("DIRT#BADLANDS:558,69");
					out.println("DIRT#MESA:558,69");
					out.println("DIRT#DEFAULT:5224,56");
					out.println("GRASS#EXTREME_HILLS:35017,124");
					out.println("GRASS#ROOFED_FOREST:26986,103");
					out.println("GRASS#TAIGA_HILLS:37580,149");
					out.println("GRASS#SWAMPLAND:15762,253");
					out.println("GRASS#JUNGLE:26191,164");
					out.println("GRASS#BADLANDS:33664,32");
					out.println("GRASS#MESA:33664,32");
					out.println("GRASS#DEFAULT:30539,85");
					out.println("GREEN_WOOL#DEFAULT:20252,202");
					out.println("HARDENED_CLAY#BADLANDS:558,69");
					out.println("HARDENED_CLAY#MESA:558,69");
					out.println("HARDENED_CLAY#DEFAULT:44379,71");
					out.println("LAVA#DEFAULT:2732,217");
					out.println("LIGHTBLUE_WOOL#DEFAULT:42733,223");
					out.println("LIME_WOOL#DEFAULT:16993,246");
					out.println("MAGENTA_WOOL#DEFAULT:50685,223");
					out.println("MYCEL#DEFAULT:46711,143");
					out.println("NETHERRACK#DEFAULT:65295,254");
					out.println("OBSIDIAN#DEFAULT:47416,214");
					out.println("ORANGE_WOOL#DEFAULT:6375,254");
					out.println("PINK_WOOL#DEFAULT:53899,149");
					out.println("PURPLE_WOOL#DEFAULT:47509,214");
					out.println("RED_WOOL#DEFAULT:452,203");
					out.println("SAND#BADLANDS:558,69");
					out.println("SAND#MESA:558,69");
					out.println("SAND#DEFAULT:43307,33");
					out.println("SNOW#DEFAULT:41513,108");
					out.println("STAINED_CLAY#BADLANDS:558,69");
					out.println("STAINED_CLAY#MESA:558,69");
					out.println("STAINED_CLAY#DEFAULT:44379,71");
					out.println("STAINED_HARDENED_CLAY#BADLANDS:558,69");
					out.println("STAINED_HARDENED_CLAY#MESA:558,69");
					out.println("STAINED_HARDENED_CLAY#DEFAULT:44379,71");
					out.println("STONE#DEFAULT:44559,129");
					out.println("WATER#DEFAULT:45016,253");
					out.println("WHITE_WOOL#DEFAULT:41200,96");
					out.println("YELLOW_WOOL#DEFAULT:10782,254");

				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}

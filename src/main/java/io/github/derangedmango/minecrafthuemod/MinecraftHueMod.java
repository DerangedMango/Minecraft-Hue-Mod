package io.github.derangedmango.minecrafthuemod;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
import java.util.Scanner;

@Mod(modid = MinecraftHueMod.MODID, name = MinecraftHueMod.NAME, version = MinecraftHueMod.VERSION)
public class MinecraftHueMod {
    public static final String MODID = "minecraft-hue";
    public static final String NAME = "Minecraft Hue";
    public static final String VERSION = "1.0";

    private static Logger logger;
    private static File configDir;
    
    private int runRate = 4;
    private int colorRate = 7;
    private int normalRate = 3;
	private int[] alphaArr = new int[26];
	private BlockConfig[] blockConfigArr;
	private DimLights task;
	private String sessionUser = "";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	logger = event.getModLog();
        configDir = new File(event.getModConfigurationDirectory().getAbsolutePath() + "/Minecraft-Hue");
        sessionUser = Minecraft.getMinecraft().getSession().getUsername();
        
        task = new DimLights(configDir, null, colorRate, normalRate, blockConfigArr, alphaArr);
        task.pause();
        
        ClientCommandHandler.instance.registerCommand(new RegisterLightIPCommand(configDir, task, sessionUser));
        ClientCommandHandler.instance.registerCommand(new DeregisterLightIPCommand(configDir, task, sessionUser));
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("Minecraft Hue is enabled");
        
        generateDataFiles();
		
		File config = new File(configDir, "config.txt");
		int entryCounter = 0;

		try(Scanner scanner = new Scanner(config)) {
			while(scanner.hasNextLine()) {
				scanner.nextLine();
				entryCounter++;
			}

			entryCounter -= 3;
			scanner.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}

		blockConfigArr = new BlockConfig[entryCounter];
		
		for(int i = 0; i < alphaArr.length; i++) {
			alphaArr[i] = blockConfigArr.length;
		}

		try(Scanner scanner = new Scanner(config)) {
			int counter = 0;
			int index = 0;
			int lastChar = -1;
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				if(counter > 2) {
					String name = line.substring(0, line.indexOf("#")).toUpperCase();
					String biome = line.substring(line.indexOf("#") + 1, line.indexOf(":")).toUpperCase();
					int hue = Integer.valueOf(line.substring(line.indexOf(":") + 1, line.indexOf(",")));
					int sat = Integer.valueOf(line.substring(line.indexOf(",") + 1));

					int currChar = line.toUpperCase().charAt(0);
					currChar -= 65;

					if(currChar != lastChar) alphaArr[currChar] = index;

					blockConfigArr[index++] = new BlockConfig(name, biome, hue, sat);
					lastChar = currChar;
				} else if(counter == 0) {
					runRate = Math.max(Integer.valueOf(line.substring(line.indexOf(":") + 1)), 2);
					counter++;
				} else if(counter == 1) {
					colorRate = Math.max(Integer.valueOf(line.substring(line.indexOf(":") + 1)), 0);
					counter++;
				} else if(counter == 2) {
					normalRate = Math.max(Integer.valueOf(line.substring(line.indexOf(":") + 1)), 0);
					counter++;
				}
			}
			
			scanner.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
		
		FMLCommonHandler.instance().bus().register(
				new EventListener(task, runRate, colorRate, normalRate, alphaArr, blockConfigArr, sessionUser));
	}

    private void generateDataFiles() {
		File df = configDir;
		File file = new File(df, "player_data.txt");
		File config = new File(df, "config.txt");
		
		if (!df.exists()) { df.mkdir(); }
		
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		if(!config.exists()) {
			try {
				config.createNewFile();

				try(FileWriter fw = new FileWriter(config.getPath(), false);
					BufferedWriter bw = new BufferedWriter(fw);
			    	PrintWriter out = new PrintWriter(bw)) {

					out.println("Run Rate:4");
					out.println("Color Transition Rate:7");
					out.println("Normal Transition Rate:3");
					out.println("BLUE_WOOL#DEFAULT:46014,254");
					out.println("BROWN_WOOL#DEFAULT:7194,231");
					out.println("CYAN_WOOL#DEFAULT:41385,233");
					out.println("DIRT#BADLANDS:558,69");
					out.println("DIRT#MESA:558,69");
					out.println("DIRT#DEFAULT:5763,111");
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

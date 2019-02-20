package io.github.derangedmango.minecrafthuemod;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class DimLights {
	private final File configDir;
	private EntityPlayer player;
	private LocalConnection con;
	private int lastLightLevel;
	private int lastHue;
	private int lastSat;
	private int normalRate;
	private int colorRate;
	private boolean paused;
	private String[] conInfo;
	private boolean inTheEnd;
	private boolean nearFire;
	private FireThread fireThread;
	private int buttonPromptCounter;
	private BlockConfig[] blockConfigArr;
	private int[] alphaArr;

    public DimLights(File dir, EntityPlayer player, int c, int n, BlockConfig[] bc, int[] a) {
        configDir = dir;
    	this.player = player;
        con = null;
        lastLightLevel = -1;
        lastHue = -1;
        lastSat = -1;
        paused = false;
        conInfo = null;
        normalRate = n;
        colorRate = c;
        inTheEnd = false;
        nearFire = false;
        fireThread = null;
        buttonPromptCounter = 0;
        blockConfigArr = bc;
        alphaArr = a;
    }

    public void run() {
    	if(!paused) {
			if(conInfo == null) conInfo = getLightIP(player.getName());
	    	
	    	if(conInfo[0] != null) {
	    		setLightLevel(getPlayerLightLevel(), conInfo[0], conInfo[1], conInfo[2]);
	    	} else {
	    		this.pause();
	    	}
    	}
    }
    
    public void pause() { paused = true; }
    
    public void resume() {
        lastHue = -1;
    	paused = false;
    }
    
    public String getPlayerName() {
    	return player.getName();
    }
    
    public LocalConnection getCon() {
    	return con;
    }
    
    public void deactivateFire() {
    	if(fireThread != null) {
    		fireThread.deactivate();
    		fireThread = null;
    		lastHue = -1;
    	}
    }
    
    public void setPlayer(EntityPlayer p) { player = p; }
    public void setColorRate(int r) { colorRate = r; }
    public void setNormalRate(int r) { normalRate = r; }
    public void setBlockConfigArr(BlockConfig[] arr) { blockConfigArr = arr; }
    public void setAlphaArr(int[] arr) { alphaArr = arr; }
    public void resetConInfo() { conInfo = null; }
    
    private int getPlayerLightLevel() {
    	BlockPos pos = player.getPosition();
    	World world = player.getEntityWorld();
    	
    	// System.out.println("PLAYER POSITION: " + pos.toString());
    	// System.out.println("LIGHT LEVEL: " + world.getLightFromNeighbors(pos));
    	return world.getLightFromNeighbors(pos);
    }

    private void setLightLevel(int level, String ip, String group, String usr) {
    	int lightLevel = level;
    	String lightIP = ip;
    	String lightGroup = group;
    	String username = usr;
    	
    	int[] hueLevels = new int[] {22,37,52,67,82,97,112,127,142,157,172,187,202,217,232,247};
    	
    	if(con != null && !con.toString().equalsIgnoreCase("Not Connected")) {
    		if(con.toString().equalsIgnoreCase("Press Button")) {
    			if(buttonPromptCounter == 0 || buttonPromptCounter == 300) {
    				player.sendMessage(new TextComponentString("Please press the link button on your Hue hub."));
    			} else if(buttonPromptCounter > 600) {
    				player.sendMessage(new TextComponentString("Link button not pressed - registration attempt aborted!"));
    				player.sendMessage(new TextComponentString("Disconnect and reconnect to the server to re-initiate attempt."));
    				this.pause();
    			}
    			
    			if(con.registerUser()) {
    				conInfo = getLightIP(player.getName());
    				player.sendMessage(new TextComponentString("Connection Registered!"));
    			} else {
    				buttonPromptCounter++;
    			}
    		} else if(con.toString().equalsIgnoreCase("Ready")) {
    			int[] color = getDomColor();
    			
    			if(inTheEnd) lightLevel = 15;
    			
    			if(nearFire) {
    				if(fireThread == null) {
    					fireThread = new FireThread(con, true);
    					fireThread.start();
    				}
    			} else {
    				this.deactivateFire();
    				
	    			if(lightLevel != lastLightLevel || color[0] != lastHue || color[1] != lastSat) {
	    				if(color[0] != lastHue || color[1] != lastSat) {
	    					con.dim(hueLevels[lightLevel], color[0], color[1], colorRate);
	    				} else {
	    					con.dim(hueLevels[lightLevel], color[0], color[1], normalRate);
	    				}
	    				
	    				lastLightLevel = lightLevel;
	    				lastHue = color[0];
	    				lastSat = color[1];
	    			}
    			}
    		} else if(con.toString().equalsIgnoreCase("Invalid IP")) {
    			player.sendMessage(new TextComponentString("Error: The IP you registered is not valid."));
    			this.pause();
    		}
    	} else {
    		con = new LocalConnection(configDir, player, lightIP, lightGroup, username);
    	}
    }
    
    private String[] getLightIP(String name) {
    	String[] result = new String[3];
    	boolean match = false;
    	
    	try(Scanner scanner = new Scanner(new File(configDir, "player_data.txt"))) {
					
			while (scanner.hasNextLine()  && !match) {
				String line = scanner.nextLine();
				
				if(line.substring(0, line.indexOf(":")).equalsIgnoreCase(name)) {
					match = true;
					
					String[] arr = line.substring(line.indexOf(":") + 1).split(",");
					result[0] = arr[0];
					result[1] = arr[1];
					result[2] = arr[2];
				}
			}
			
			scanner.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    	
    	return result;
    }
    
    private int[] getDomColor() {
    	String[] blockTypes = new String[75];
		int[] blockCounts = new int[75];
		
		int px = (int) Math.floor(player.posX);
		int py = (int) player.posY;
		int pz = (int) player.posZ;
		
		int lastIndex = -1;
		boolean match = false;
		
		BlockPos loc;
		String blockName;
		World world = player.world;
		String biome = world.getBiome(player.getPosition()).getBiomeName().toUpperCase();
		
		if(biome.equalsIgnoreCase("THE END")) {
    		inTheEnd = true;
    		return new int[] {47416,214};
    	} else {
			for(int x = px - 2; x <= px + 2; x++) {
				for(int y = py - 1; y <= py + 1; y++) {
					for(int z = pz - 2; z <= pz + 2; z++) {						
						loc = new BlockPos(x, y, z);
						blockName = consolidateBlockNames(world.getBlockState(loc).getBlock(), world, loc);
						
						if(blockName.equalsIgnoreCase("FIRE")) {
							nearFire = true;
							return new int[] {3332, 254};
						}
						
						nearFire = false;
	
						if(!blockName.equalsIgnoreCase("AIR")) {
							if(lastIndex > -1) {
								int i;
		
								for(i = 0; i <= lastIndex; i++) {
									if(blockName.equals(blockTypes[i])) {
										match = true;
										break;
									}
								}
		
								if(match) {
									blockCounts[i]++;
									match = false;
								} else {
									blockTypes[++lastIndex] = blockName;
									blockCounts[lastIndex] = 1;
								}
							} else {
								blockTypes[++lastIndex] = blockName;
								blockCounts[lastIndex] = 1;
							}
						}
					}
				}
			}
		}

		int currMax = 0, grassCount = 0;
		String domBlock = "VOID";

		for(int i = 0; i <= lastIndex; i++) {
			if(blockTypes[i].equals("GRASS")) grassCount = blockCounts[i];
			
			if(blockCounts[i] > currMax) {
				currMax = blockCounts[i];
				domBlock = blockTypes[i];
			}
		}
		
		if(domBlock.equals("DIRT") && grassCount > 9) domBlock = "GRASS";
		
		inTheEnd = false;
		
		// System.out.println("DOMINANT BLOCK: " + domBlock);
		// System.out.println("DOMINANT HUE: " + getColorFromBlock(domBlock, blockTypes, blockCounts, lastIndex)[0]);
		// System.out.println("DOMINANT SAT: " + getColorFromBlock(domBlock, blockTypes, blockCounts, lastIndex)[1]);
		return getColorFromBlock(domBlock, blockTypes, blockCounts, lastIndex);
	}
    
    private String consolidateBlockNames(Block block, World world, BlockPos pos) {
    	String name = block.toString();
    	name = name.substring(name.indexOf(":") + 1, name.indexOf("}")).toUpperCase();
    	
    	if(name.contains("WATER")) {
			return "WATER";
		} else if(name.equalsIgnoreCase("DOUBLE_PLANT") || name.equalsIgnoreCase("TALLGRASS") || name.equalsIgnoreCase("LEAVES") 
				|| name.equalsIgnoreCase("LEAVES_2")) {
			return "GRASS";
		} else if((name.contains("STONE") && !name.contains("SAND") && !name.contains("RED")
				&& !name.contains("END")) || name.equalsIgnoreCase("GRAVEL")  || name.contains("ORE")) {
			return "STONE";
		} else if(name.contains("LAVA")) {
			return "LAVA";
		} else if(name.contains("SAND")) {
			return "SAND";
		} else if(name.equalsIgnoreCase("WOOL")) {
			return world.getBlockState(pos).getValue(block.getBlockState().getProperty("color"))
				.toString().toUpperCase() + "_WOOL";
		} else {
			return name;
		}
    }
    
    private int[] getColorFromBlock(String domBlock, String[] blockTypes, int[] blockCounts, int lastIndex) {
    	int alphaIndex = domBlock.toUpperCase().charAt(0);
    	alphaIndex -= 65;

    	String biome = player.world.getBiome(player.getPosition()).getBiomeName().toUpperCase();
    	if(biome.contains("MESA")) biome = "MESA";
    	// System.out.println("BIOME: " + biome);

    	for(int i = alphaArr[alphaIndex]; i < blockConfigArr.length; i++) {
    		if(domBlock.equalsIgnoreCase(blockConfigArr[i].getName())) {
    			if(blockConfigArr[i].getBiome().equalsIgnoreCase(biome)) {
    				return new int[] {blockConfigArr[i].getHue(), blockConfigArr[i].getSat()};
    			} else if(blockConfigArr[i].getBiome().equalsIgnoreCase("DEFAULT")) {
    				return new int[] {blockConfigArr[i].getHue(), blockConfigArr[i].getSat()};
    			}
    		}
    	}

    	return new int[] {44379, 71};
    }
}

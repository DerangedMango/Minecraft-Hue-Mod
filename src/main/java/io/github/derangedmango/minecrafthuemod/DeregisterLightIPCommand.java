package io.github.derangedmango.minecrafthuemod;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class DeregisterLightIPCommand extends CommandBase {
	private final File configDir;
	private final String sessionUser;
	private DimLights task;
	
	public DeregisterLightIPCommand(File f, DimLights t, String u) {
		configDir = f;
		task = t;
		sessionUser = u;
	}
	
	@Override
	public String getName() {
		return "deregisterLightIP";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender ics, String[] args) throws CommandException{
		EntityPlayer player = (EntityPlayer) ics.getCommandSenderEntity();
		
		if(player.getName().equals(sessionUser)) {
			if(!removeIP(player.getName())) {
				player.sendMessage(new TextComponentString("Error: Your username does not have a registered IP."));
			} else {
				task.resetConInfo();
				player.sendMessage(new TextComponentString("Your Hue Bridge's local IP has been deregistered."));
			}
		}
	}
	
	@Override
	public String getUsage(ICommandSender ics) {
		return "/deregisterLightIP";
	}
	
	private boolean removeIP(String name) {
		File file = new File(configDir, "player_data.txt");
		boolean match = false;
		String outString = "";
		
		try(Scanner scanner = new Scanner(file)) {
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				if(line.substring(0, line.indexOf(":")).equalsIgnoreCase(name)) {
					match = true;
				} else {
					outString = outString + line + "~";
				}
			}
			
			scanner.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    		return false;
    	}
		
		if(match) {
			task.pause();
			
			try(FileWriter fw = new FileWriter(file.getPath(), false);
				BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw)) {
				
				String seg = "";
				
				for(int x = 0; x < outString.length(); x++) {
					if(String.valueOf(outString.charAt(x)).equals("~")) {
						out.println(seg);
						seg = "";
					} else {
						seg = seg + outString.charAt(x);
					}
				}
			    
			    return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
}

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

public class RegisterLightIPCommand extends CommandBase {
	private final File configDir;
	private final String sessionUser;
	private DimLights task;
	
	public RegisterLightIPCommand(File f, DimLights t, String u) {
		configDir = f;
		task = t;
		sessionUser = u;
	}
	
	@Override
	public String getName() {
		return "registerLightIP";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender ics, String[] args) throws CommandException {
		EntityPlayer player = (EntityPlayer) ics.getCommandSenderEntity();
		
		if(player.getName().equals(sessionUser)) {
			if(args.length == 0) {
				player.sendMessage(new TextComponentString("Error: user must at least provide IP parameter"));
				player.sendMessage(new TextComponentString("Usage: " + this.getUsage(ics)));
			} else if(args.length == 1) {
				if(!addIP(player.getName(), args[0], "ALL")) {
					player.sendMessage(new TextComponentString("Error: you already have an IP registered, "
							+ "use /deregisterLightIP to remove it"));
				} else {
					task.resetConInfo();
					task.resume();
				}
			} else {
				String lightGroup = "";
				
				for(int i = 1; i < args.length; i++) {
					lightGroup = lightGroup + args[i] + " ";
				}
				
				if(!addIP(player.getName(), args[0], lightGroup.trim())) {
					player.sendMessage(new TextComponentString("Error: you already have an IP registered, "
							+ "use /deregisterLightIP to remove it"));
				} else {
					task.resetConInfo();
					task.resume();
				}
			}
		}
	}
	
	@Override
	public String getUsage(ICommandSender ics) {
		return "/registerLightIP <Hue Hub Local IP> [Light Group Name]";
	}
	
	private boolean addIP(String name, String ip, String group) {
		File file = new File(configDir, "player_data.txt");
		boolean match = false;
		
		try(Scanner scanner = new Scanner(file)) {
			
			while (scanner.hasNextLine()  && !match) {
				String line = scanner.nextLine();
				
				if(line.substring(0, line.indexOf(":")).equalsIgnoreCase(name)) {
					match = true;
				}
			}
			
			scanner.close();
    	} catch(IOException e) {
    		e.printStackTrace();
    		return false;
    	}
		
		if(!match) {
			try(FileWriter fw = new FileWriter(file.getPath(), true);
				BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw)) {
			
			    out.println(name + ":" + ip + "," + group + ",$");
			    
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

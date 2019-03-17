package io.github.derangedmango.minecrafthuemod;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class RegisterLightIPCommand extends CommandBase {
	private final String sessionUser;
	
	public RegisterLightIPCommand(String u) {
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
				if(!addIP(args[0], "ALL")) {
					player.sendMessage(new TextComponentString("Error: you already have an IP registered, "
							+ "use /deregisterLightIP to remove it"));
				} else {
					MinecraftHueMod.task.resetConInfo();
					MinecraftHueMod.task.resume();
				}
			} else {
				String lightGroup = "";
				
				for(int i = 1; i < args.length; i++) {
					lightGroup = lightGroup + args[i] + " ";
				}
				
				if(!addIP(args[0], lightGroup.trim())) {
					player.sendMessage(new TextComponentString("Error: you already have an IP registered, "
							+ "use /deregisterLightIP to remove it"));
				} else {
					MinecraftHueMod.task.resetConInfo();
					MinecraftHueMod.task.resume();
				}
			}
		}
	}
	
	@Override
	public String getUsage(ICommandSender ics) {
		return "/registerLightIP <Hue Hub Local IP> [Light Group Name]";
	}
	
	private boolean addIP(String ip, String group) {
	
		if(MinecraftHueMod.config.getNetwork()[0].equals("")) {
			MinecraftHueMod.config.setNetwork(ip, group);
			return true;
		}
	
		return false;
	}
}

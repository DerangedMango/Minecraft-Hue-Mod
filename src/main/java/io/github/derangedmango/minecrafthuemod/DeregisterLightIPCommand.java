package io.github.derangedmango.minecrafthuemod;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class DeregisterLightIPCommand extends CommandBase {
	private final String sessionUser;
	
	public DeregisterLightIPCommand(String u) {
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
				MinecraftHueMod.task.resetConInfo();
				player.sendMessage(new TextComponentString("Your Hue Bridge's local IP has been deregistered."));
			}
		}
	}
	
	@Override
	public String getUsage(ICommandSender ics) {
		return "/deregisterLightIP";
	}
	
	private boolean removeIP(String name) {
		
		if(!MinecraftHueMod.config.getNetwork()[0].equals("")) {
			MinecraftHueMod.config.setNetwork("", "");
			return true;
		}
		
		return false;
	}
}

package io.github.derangedmango.minecrafthuemod;

import java.util.concurrent.TimeUnit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventListener {
	private final int runRate;
	private final int colorRate;
	private final int normalRate;
	private final int[] alphaArr;
	private final BlockConfig[] blockConfigArr;
	private final String sessionUser;
	private int tick;
	
	public EventListener(int r, int c, int n, int[] a, BlockConfig[] bc, String u) {
		runRate = r * 2;
		colorRate = c;
		normalRate = n;
		alphaArr = a;
		blockConfigArr = bc;
		tick = 0;
		sessionUser = u;
	}
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event) {
		if(event.player.getName().equals(sessionUser)) {
			MinecraftHueMod.task.setPlayer(event.player);
			MinecraftHueMod.task.setColorRate(colorRate);
			MinecraftHueMod.task.setNormalRate(normalRate);
			MinecraftHueMod.task.setBlockConfigArr(blockConfigArr);
			MinecraftHueMod.task.setAlphaArr(alphaArr);
			MinecraftHueMod.task.resume();
		}
	}
	
	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent event) {
		if(event.player.getName().equals(sessionUser)) {
			MinecraftHueMod.task.deactivateFire();
			MinecraftHueMod.task.pause();
		}
	}
	
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if(++tick / runRate == 1) {
			tick = 0;
			MinecraftHueMod.task.run();
		}
	}
	
	@SubscribeEvent
	public void onLightning(PlaySoundEvent event) {
		if(event.getName().equalsIgnoreCase("entity.lightning.thunder")) {
			LocalConnection con = MinecraftHueMod.task.getCon();
			
	        if(con != null && con.toString().equalsIgnoreCase("Ready")) {
	        	LightningThread thread = new LightningThread(MinecraftHueMod.task, con);
	        	thread.start();
        	}
		}
	}
	
	@SubscribeEvent
	public void onDamage(LivingHurtEvent event) {
		if(event.getEntityLiving() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			
			if(player.getName().equals(sessionUser)) {
				LocalConnection con = MinecraftHueMod.task.getCon();
				
				if(con != null && con.toString().equalsIgnoreCase("Ready")) {
					MinecraftHueMod.task.pause();
	            	
	            	con.dim(254, 60088, 123, 0);
	    			
	    			try {
	    				TimeUnit.MILLISECONDS.sleep(Long.valueOf(125));
	    			} catch (InterruptedException e) {
	    				e.printStackTrace();
	    			}
	            	
	    			MinecraftHueMod.task.resume();
	        	}
			}
		}
	}
}
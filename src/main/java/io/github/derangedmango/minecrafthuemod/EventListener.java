package io.github.derangedmango.minecrafthuemod;

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
	private DimLights task;
	
	public EventListener(DimLights t, int r, int c, int n, int[] a, BlockConfig[] bc, String u) {
		runRate = r * 2;
		colorRate = c;
		normalRate = n;
		alphaArr = a;
		blockConfigArr = bc;
		tick = 0;
		task = t;
		sessionUser = u;
	}
	
	@SubscribeEvent
	public void onPlayerLogin(PlayerLoggedInEvent event) {
		if(event.player.getName().equals(sessionUser)) {
			task.setPlayer(event.player);
			task.setColorRate(colorRate);
			task.setNormalRate(normalRate);
			task.setBlockConfigArr(blockConfigArr);
			task.setAlphaArr(alphaArr);
			task.resume();
		}
	}
	
	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent event) {
		if(event.player.getName().equals(sessionUser)) task.pause();
	}
	
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if(++tick / runRate == 1) {
			tick = 0;
			task.run();
		}
	}
}

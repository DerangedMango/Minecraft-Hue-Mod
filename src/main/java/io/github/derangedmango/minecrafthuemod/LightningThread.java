package io.github.derangedmango.minecrafthuemod;

import java.util.concurrent.TimeUnit;

public class LightningThread extends Thread {
	private DimLights task;
	private LocalConnection con;
	
	public LightningThread(DimLights t, LocalConnection c) {
		task = t;
		con = c;
	}
	
	public void run() {
    	task.pause();
    	
		con.dim(254, 44773, 9, 0);
		
		try {
			TimeUnit.MILLISECONDS.sleep(Long.valueOf(20));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		con.dim(0, 44773, 9, 0);
		
		try {
			TimeUnit.MILLISECONDS.sleep(Long.valueOf(20));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		con.dim(254, 44773, 9, 0);
		con.dim(0, 44773, 9, 1);
		
		task.resume();
	}
}

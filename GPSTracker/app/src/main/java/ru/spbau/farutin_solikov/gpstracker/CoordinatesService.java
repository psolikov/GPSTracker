package ru.spbau.farutin_solikov.gpstracker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import java.util.Random;

public class CoordinatesService extends JobIntentService {
	public static final int JOB_ID = 1000;
	private static boolean isActive;
	private int x, y;
	
	public static void enqueueWork(Context context, Intent work) {
		isActive = true;
		enqueueWork(context, CoordinatesService.class, JOB_ID, work);
	}
	
	public static void stop() {
		isActive = false;
	}
	
	@Override
	protected void onHandleWork(@NonNull Intent intent) {
		Random random = new Random();
		int newX, newY;
		
		while (isActive) {
			newX = random.nextInt();
			newY = random.nextInt();
			
			if (newX != x || newY != y) {
				x = newX;
				y = newY;
				broadcastCoordinates();
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void broadcastCoordinates(){
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction("BroadcastCoordinatesAction");
		broadcastIntent.putExtra("xCoordinate", x);
		broadcastIntent.putExtra("yCoordinate", y);
		sendBroadcast(broadcastIntent);
	}
}

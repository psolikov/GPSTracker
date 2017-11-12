package ru.spbau.farutin_solikov.gpstracker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import java.util.Random;

import static java.lang.Math.abs;

public class CoordinatesService extends JobIntentService {
	public static final int JOB_ID = 1000;
	private static final double EPS = 1.0E-06;
	private static boolean isActive;
	private double x, y;
	
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
		double newX, newY;
		x = 0;
		y = 0;
		
		while (isActive) {
			newX = (double)(random.nextInt() % 1000) / 10000 + 51;
			newY = (double)(random.nextInt() % 1000) / 10000 - 0.24;
			
			if (abs(newX - x) > EPS || abs(newY - y) > EPS) {
				x = newX;
				y = newY;
				broadcastCoordinates();
			}
			
			try {
				Thread.sleep(2000);
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

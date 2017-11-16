package ru.spbau.farutin_solikov.gpstracker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import java.util.ArrayList;

import static java.lang.Math.abs;

public class CoordinatesService extends JobIntentService {
	public static final int JOB_ID = 1000;
	private static final double EPS = 1.0E-06;
	private static final int SLEEP = 1000;
	private static boolean isActive;
	
	private ArrayList<Controller.Coordinate> coordinates;
	private double lat, lng;
	private int id;
	
	public static void enqueueWork(Context context, Intent work) {
		isActive = true;
		enqueueWork(context, CoordinatesService.class, JOB_ID, work);
	}
	
	public static void stop() {
		isActive = false;
	}
	
	@Override
	protected void onHandleWork(@NonNull Intent intent) {
		boolean positionChanged;
		
		lat = 0;
		lng = 0;
		id = -1;
		
		while (isActive) {
			coordinates = Controller.fetchCoordinates(id);
			positionChanged = false;
			
			for (Controller.Coordinate pos : coordinates) {
				if (abs(pos.getLat() - lat) > EPS || abs(pos.getLng() - lng) > EPS) {
					positionChanged = true;
					break;
				}
			}
				
			if (positionChanged) {
				if (coordinates.size() != 0) {
					Controller.Coordinate pos = coordinates.get(coordinates.size() - 1);
					lat = pos.getLat();
					lng = pos.getLng();
					id = pos.getId();
				}
				
				broadcastCoordinates();
			}
			
			try {
				Thread.sleep(SLEEP);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void broadcastCoordinates(){
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction("ru.spbau.farutin_solikov.gpstracker.BroadcastCoordinatesAction");
		broadcastIntent.putParcelableArrayListExtra("ru.spbau.farutin_solikov.gpstracker.coordinates", coordinates);
		sendBroadcast(broadcastIntent);
	}
}

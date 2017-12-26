package ru.spbau.farutin_solikov.gpstracker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import java.util.ArrayList;

import static java.lang.Math.abs;

/**
 * This class checks periodically whether position of the vehicle has been changed or not.
 */
public class CoordinatesService extends JobIntentService {
	public static final int JOB_ID = 1000;
	private static final double EPS = 1.0E-06;
	private static final int SLEEP = 1000;
	private static boolean isActive;
	
	private ArrayList<Coordinate> coordinates;
	
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
		
		double lat = 0;
		double lng = 0;
		int id = -1;

		Controller.clearTable();

		while (isActive) {
			coordinates = Controller.fetchCoordinates(id);
			positionChanged = false;
			
			for (Coordinate pos : coordinates) {
				if (abs(pos.getLat() - lat) > EPS || abs(pos.getLng() - lng) > EPS) {
					positionChanged = true;
					break;
				}
			}
				
			if (positionChanged) {
				if (coordinates.size() != 0) {
					Coordinate pos = coordinates.get(coordinates.size() - 1);
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
		broadcastIntent.setAction(getString(R.string.broadcast_content_coordinates));
		broadcastIntent.putParcelableArrayListExtra(getString(R.string.extra_coordinates), coordinates);
		sendBroadcast(broadcastIntent);
	}
}

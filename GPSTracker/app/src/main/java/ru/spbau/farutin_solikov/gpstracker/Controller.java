package ru.spbau.farutin_solikov.gpstracker;

import android.content.Context;
import android.content.Intent;

public class Controller {
	public static void startCoordinatesService(Context context) {
		CoordinatesService.enqueueWork(context, new Intent());
	}
	
	public static void stopCoordinatesService() {
		CoordinatesService.stop();
	}
}

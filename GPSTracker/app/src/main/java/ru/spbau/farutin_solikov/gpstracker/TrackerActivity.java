package ru.spbau.farutin_solikov.gpstracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class TrackerActivity extends DrawerActivity implements OnMapReadyCallback {
	private CoordinatesReceiver receiver;
	private GoogleMap map;
	private LatLng lastPosition;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_tracker);
		
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
		
		Button start = findViewById(R.id.start_tracker);
		start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				map.clear();
				lastPosition = null;
				
				Controller.startCoordinatesService(TrackerActivity.this);
				
				receiver = new CoordinatesReceiver();
				IntentFilter intentSFilter = new IntentFilter("ru.spbau.farutin_solikov.gpstracker.BroadcastCoordinatesAction");
				registerReceiver(receiver, intentSFilter);
			}
		});
		
		Button stop = findViewById(R.id.stop_tracker);
		stop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Controller.stopCoordinatesService();
				try {
					unregisterReceiver(receiver);
				} catch (IllegalArgumentException ignored) {
					// no API methods to tell if it is registered at the moment
				}
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unregisterReceiver(receiver);
		} catch (IllegalArgumentException ignored) {
			// no API methods to tell if it is registered at the moment
		}
	}
	
	@Override
	public void onMapReady(GoogleMap googleMap) {
		map = googleMap;
		map.moveCamera(CameraUpdateFactory.zoomBy(15));
	}
	
	public class CoordinatesReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle notificationData = intent.getExtras();
			ArrayList<Controller.Coordinate> coordinates =
					notificationData.getParcelableArrayList("ru.spbau.farutin_solikov.gpstracker.coordinates");
			
			PolylineOptions polylineOptions = new PolylineOptions().geodesic(true);
			
			if (lastPosition == null) {
				Controller.Coordinate position = coordinates.get(0);
				lastPosition = new LatLng(position.getLat(), position.getLng());
			}

			polylineOptions.add(lastPosition);
			for (Controller.Coordinate position : coordinates) {
				polylineOptions.add(new LatLng(position.getLat(), position.getLng()));
			}
			
			Controller.Coordinate position = coordinates.get(coordinates.size() - 1);
			lastPosition = new LatLng(position.getLat(), position.getLng());
			
			map.moveCamera(CameraUpdateFactory.newLatLng(lastPosition));
			map.addPolyline(polylineOptions);
		}
	}
}

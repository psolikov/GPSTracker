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
import com.google.android.gms.maps.model.MarkerOptions;

public class TrackerActivity extends DrawerActivity implements OnMapReadyCallback {
	private CoordinatesReceiver receiver;
	private GoogleMap map;
	
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
				Controller.startCoordinatesService(TrackerActivity.this);
				
				receiver = new CoordinatesReceiver();
				IntentFilter intentSFilter = new IntentFilter("BroadcastCoordinatesAction");
				registerReceiver(receiver, intentSFilter);
			}
		});
		
		Button stop = findViewById(R.id.stop_tracker);
		stop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Controller.stopCoordinatesService();
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}
	
	@Override
	public void onMapReady(GoogleMap googleMap) {
		map = googleMap;
		map.moveCamera(CameraUpdateFactory.zoomBy(7));
	}
	
	public class CoordinatesReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle notificationData = intent.getExtras();
			double x = notificationData.getDouble("xCoordinate");
			double y = notificationData.getDouble("yCoordinate");
			
			LatLng position = new LatLng(x, y);
			map.addMarker(new MarkerOptions().position(position).title("Marker"));
			map.moveCamera(CameraUpdateFactory.newLatLng(position));
		}
	}
}

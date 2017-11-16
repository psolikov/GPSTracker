package ru.spbau.farutin_solikov.gpstracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class TrackerActivity extends DrawerActivity implements OnMapReadyCallback {
	private static final int ZOOM = 15;
	
	private CoordinatesReceiver receiver;
	private GoogleMap map;
	private LatLng lastPosition;
	private ArrayList<Controller.Coordinate> route;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_tracker);
		
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
		
		createMenu();
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
	}
	
	private void createMenu() {
		final FloatingActionsMenu menu = findViewById(R.id.menu_tracker);
		
		FloatingActionButton start = new FloatingActionButton(this);
		start.setTitle("Start trip");
		start.setColorNormalResId(R.color.colorPrimaryDark);
		start.setColorPressedResId(R.color.colorPrimary);
		start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				menu.collapse();
				
				map.clear();
				lastPosition = null;
				route = new ArrayList<Controller.Coordinate>();
				
				Controller.startCoordinatesService(TrackerActivity.this);
				
				receiver = new CoordinatesReceiver();
				IntentFilter intentSFilter = new IntentFilter("ru.spbau.farutin_solikov.gpstracker.BroadcastCoordinatesAction");
				registerReceiver(receiver, intentSFilter);
			}
		});
		
		FloatingActionButton stop = new FloatingActionButton(this);
		stop.setTitle("Stop");
		stop.setColorNormalResId(R.color.colorPrimaryDark);
		stop.setColorPressedResId(R.color.colorPrimary);
		stop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				menu.collapse();
				
				Controller.stopCoordinatesService();
				try {
					unregisterReceiver(receiver);
				} catch (IllegalArgumentException ignored) {
					// no API methods to tell if it is registered at the moment
				}
			}
		});
		
		FloatingActionButton save = new FloatingActionButton(this);
		save.setTitle("Save route");
		save.setColorNormalResId(R.color.colorPrimaryDark);
		save.setColorPressedResId(R.color.colorPrimary);
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				menu.collapse();
				
				saveRoute();
			}
		});
		
		menu.addButton(start);
		menu.addButton(stop);
		menu.addButton(save);
		
		FloatingActionButton find = findViewById(R.id.find_tracker);
		find.setColorNormalResId(R.color.colorPrimaryDark);
		find.setColorPressedResId(R.color.colorPrimary);
		find.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (lastPosition != null) {
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, ZOOM));
				}
			}
		});
	}
	
	private void saveRoute() {
		final Animation slide_in = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in);
		final Animation slide_out = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out);
		
		// Replace with Dialog
		final TextInputLayout inputLayout = findViewById(R.id.text_input2);
		inputLayout.setVisibility(View.VISIBLE);
		inputLayout.startAnimation(slide_in);
		
		final EditText nameInput = findViewById(R.id.route_name2);
		nameInput.setText("");
		
		Button save = findViewById(R.id.save2);
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String name = nameInput.getText().toString();
				if (name.length() != 0) {
					Controller.sendCoordinates(route, name);
					inputLayout.startAnimation(slide_out);
					inputLayout.setVisibility(View.GONE);
				}
			}
		});
		
		Button cancel = findViewById(R.id.cancel2);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				inputLayout.startAnimation(slide_out);
				inputLayout.setVisibility(View.GONE);
			}
		});
	}
	
	private class CoordinatesReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle notificationData = intent.getExtras();
			ArrayList<Controller.Coordinate> coordinates =
					notificationData.getParcelableArrayList("ru.spbau.farutin_solikov.gpstracker.coordinates");
			
			PolylineOptions polylineOptions = new PolylineOptions().geodesic(true);
			
			if (lastPosition == null) {
				Controller.Coordinate position = coordinates.get(0);
				lastPosition = new LatLng(position.getLat(), position.getLng());
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, ZOOM));
			}

			polylineOptions.add(lastPosition);
			for (Controller.Coordinate position : coordinates) {
				polylineOptions.add(new LatLng(position.getLat(), position.getLng()));
				route.add(position);
			}
			
			Controller.Coordinate position = coordinates.get(coordinates.size() - 1);
			lastPosition = new LatLng(position.getLat(), position.getLng());
			
			map.addPolyline(polylineOptions);
		}
	}
}

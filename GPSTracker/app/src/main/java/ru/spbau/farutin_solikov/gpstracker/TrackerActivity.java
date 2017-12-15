package ru.spbau.farutin_solikov.gpstracker;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;


public class TrackerActivity extends DrawerActivity implements OnMapReadyCallback {
	private static final int ZOOM = 15;
	
	private CoordinatesReceiver receiver;
	private GoogleMap map;
	private LatLng lastPosition;
	private Controller.Coordinate startPosition;
	private boolean startImmediately = false;
	private static ArrayList<Controller.Coordinate> route;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_tracker);
		
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
		
		createMenu();
		
		Intent intent = getIntent();
		if (intent.hasExtra("ru.spbau.farutin_solikov.gpstracker.position")) {
			startPosition = intent.getParcelableExtra("ru.spbau.farutin_solikov.gpstracker.position");
			startImmediately = true;
		}
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
		
		if (startImmediately) {
			startTracking(startPosition);
		}
	}
	
	private void createMenu() {
		final FloatingActionsMenu menu = findViewById(R.id.menu_tracker);
		
		FloatingActionButton start = new FloatingActionButton(this);
		start.setTitle("Start trip");
		start.setIcon(R.drawable.ic_play_arrow_white_24dp);
		start.setColorNormalResId(R.color.primaryColor);
		start.setColorPressedResId(R.color.primaryLightColor);
		start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				menu.collapse();
				
				if (map != null) {
					startTracking(null);
				}
			}
		});
		
		FloatingActionButton stop = new FloatingActionButton(this);
		stop.setTitle("Stop");
		stop.setIcon(R.drawable.ic_stop_white_24dp);
		stop.setColorNormalResId(R.color.primaryColor);
		stop.setColorPressedResId(R.color.primaryLightColor);
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
		save.setIcon(R.drawable.ic_save_white_24dp);
		save.setColorNormalResId(R.color.primaryColor);
		save.setColorPressedResId(R.color.primaryLightColor);
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
		find.setIcon(R.drawable.ic_my_location_white_24dp);
		find.setColorNormalResId(R.color.primaryColor);
		find.setColorPressedResId(R.color.primaryLightColor);
		find.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (lastPosition != null) {
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, ZOOM));
				}
			}
		});
	}
	
	private void startTracking(Controller.Coordinate startPosition) {
		map.clear();
		route = new ArrayList<>();
		
		if (startPosition == null) {
			lastPosition = null;
		} else {
			lastPosition = new LatLng(startPosition.getLat(), startPosition.getLng());
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, ZOOM));
			map.addMarker(new MarkerOptions().position(lastPosition));
			route.add(startPosition);
			
			lastPosition = null;
		}
		
		Controller.startCoordinatesService(TrackerActivity.this);
		
		receiver = new CoordinatesReceiver();
		IntentFilter intentSFilter = new IntentFilter("ru.spbau.farutin_solikov.gpstracker.BroadcastCoordinatesAction");
		registerReceiver(receiver, intentSFilter);
	}
	
	private void saveRoute() {
		DialogFragment saveDialogFragment = new SaveDialogFragment();
		saveDialogFragment.show(getSupportFragmentManager(), "Save route");
	}
	
	public static class SaveDialogFragment extends DialogFragment {
		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			final LayoutInflater inflater = getActivity().getLayoutInflater();
			final View view = inflater.inflate(R.layout.dialog_save, null);
			
			builder.setView(view)
					.setTitle("Save route")
					.setPositiveButton("Save", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							EditText nameInput = view.findViewById(R.id.route_name);
							nameInput.setText("");
							String name = nameInput.getText().toString();
							if (name.length() != 0) {
								Controller.sendCoordinates(route, name);
							}
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							SaveDialogFragment.this.getDialog().cancel();
						}
					});
			
			return builder.create();
		}
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
				route.add(position);
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

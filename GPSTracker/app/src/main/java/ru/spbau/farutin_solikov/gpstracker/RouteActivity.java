package ru.spbau.farutin_solikov.gpstracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class RouteActivity extends DrawerActivity implements OnMapReadyCallback {
	private static final int ZOOM = 15;
	
	private GoogleMap map;
	private ArrayList<Controller.Coordinate> route;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_route);
		
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
		
		Intent intent = getIntent();
		route = intent.getParcelableArrayListExtra("ru.spbau.farutin_solikov.gpstracker.coordinates");
	}
	
	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			finish();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onMapReady(GoogleMap googleMap) {
		map = googleMap;
		drawRoute();
	}
	
	private void drawRoute() {
		PolylineOptions polylineOptions = new PolylineOptions().geodesic(true);
		
		for (Controller.Coordinate position : route) {
			polylineOptions.add(new LatLng(position.getLat(), position.getLng()));
		}
		
		map.addPolyline(polylineOptions);
		
		if (route.size() != 0) {
			Controller.Coordinate position = route.get(route.size() - 1);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(position.getLat(), position.getLng()), ZOOM));
		}
	}
}

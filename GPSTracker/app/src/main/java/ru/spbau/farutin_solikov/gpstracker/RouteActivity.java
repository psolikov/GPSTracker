package ru.spbau.farutin_solikov.gpstracker;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Locale;

import static android.view.View.TRANSLATION_Y;
import static java.lang.Thread.sleep;

public class RouteActivity extends DrawerActivity implements OnMapReadyCallback {
	private static final int ZOOM = 15;
	
	private GoogleMap map;
	private ArrayList<LatLng> route = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_route);
		
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
		
		Intent intent = getIntent();
		ArrayList<Controller.Coordinate> coordinates = intent.getParcelableArrayListExtra("ru.spbau.farutin_solikov.gpstracker.coordinates");
		
		for (Controller.Coordinate coordinate : coordinates) {
			route.add(new LatLng(coordinate.getLat(), coordinate.getLng()));
		}
		
		final RelativeLayout infoLayout = findViewById(R.id.route_info);
		infoLayout.post(new Runnable() {
			@Override
			public void run() {
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) infoLayout.getLayoutParams();
				params.setMargins(0, 0, 0, -1 * infoLayout.getHeight());
				infoLayout.setLayoutParams(params);	}
		});
		
		final FloatingActionButton find = findViewById(R.id.route_info_button);
		find.setIcon(R.drawable.ic_info_outline_white_24dp);
		find.setColorNormalResId(R.color.primaryColor);
		find.setColorPressedResId(R.color.primaryLightColor);
		find.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ObjectAnimator animX = ObjectAnimator.ofFloat(find, View.TRANSLATION_X, 0, find.getWidth());
				animX.setDuration(700);
				animX.start();
				
				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				ObjectAnimator animY = ObjectAnimator.ofFloat(infoLayout, View.TRANSLATION_Y, 0, -1 * infoLayout.getHeight());
				animY.setDuration(1000);
				animY.start();
			}
		});
		
		ImageView hide = findViewById(R.id.hide_info);
		hide.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ObjectAnimator animY = ObjectAnimator.ofFloat(infoLayout, View.TRANSLATION_Y, -1 * infoLayout.getHeight(), 0);
				animY.setDuration(700);
				animY.start();
				
				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				ObjectAnimator animX = ObjectAnimator.ofFloat(find, View.TRANSLATION_X, find.getWidth(), 0);
				animX.setDuration(1000);
				animX.start();
			}
		});
		
		double length = SphericalUtil.computeLength(route);
		TextView routeLength = findViewById(R.id.route_length);
		routeLength.setText(String.format("Route length: %1$,.2fm", length));
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
		
		for (LatLng position : route) {
			polylineOptions.add(position);
		}
		
		map.addPolyline(polylineOptions);
		
		if (route.size() != 0) {
			LatLng position = route.get(route.size() - 1);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, ZOOM));
		}
	}
}

package ru.spbau.farutin_solikov.gpstracker;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
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
import static java.lang.Thread.sleep;

/**
 * Activity to display saved route on the map.
 */
public class RouteActivity extends DrawerActivity implements OnMapReadyCallback {
	private static final String TAG = "RouteActivity";
	private static final int ZOOM = 15;
	private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
	
	private GoogleMap map;
	private String routeName;
	private final ArrayList<LatLng> route = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_route);
		
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
		
		Intent intent = getIntent();
		routeName = intent.getStringExtra(getString(R.string.extra_route_name));
		ArrayList<Coordinate> coordinates = intent.getParcelableArrayListExtra(getString(R.string.extra_coordinates));
		
		for (Coordinate coordinate : coordinates) {
			route.add(new LatLng(coordinate.getLat(), coordinate.getLng()));
		}
		
		setUpUIElements();
	}

	// не должно ли это поведение общим для всех активити?
	//
	// Для основных трех активити (к которым есть доступ через меню слева в NavigationView)
	// в этом месте хочется выходить из приложения (как и написано в соответствующем методе
	// в DrawerActivity). Пользователь может какое-то количество раз между ними переключаться
	// из меню, но по смыслу они не являются предками друг друга в плане иерархии активити.
	@Override
	public void onBackPressed() {
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			finish();
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
		switch (requestCode) {
			case WRITE_EXTERNAL_STORAGE_REQUEST_CODE: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Controller.sendSnapshot(this, map, routeName);
				}
			}
		}
	}
		
	@Override
	public void onMapReady(GoogleMap googleMap) {
		map = googleMap;
		drawRoute();
	}
	
	private void setUpUIElements() {
		Toolbar toolbar = findViewById(R.id.toolbar);
		ImageView share = new ImageView(this);
		share.setImageResource(R.drawable.ic_share_white_24dp);
		Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.END;
		share.setLayoutParams(params);
		toolbar.addView(share);
		
		share.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (ContextCompat.checkSelfPermission(RouteActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
						!= PackageManager.PERMISSION_GRANTED) {
					
					ActivityCompat.requestPermissions(RouteActivity.this,
							new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
							WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
				} else {
					Controller.sendSnapshot(RouteActivity.this, map, routeName);
				}
			}
		});
		
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
					Log.w(TAG, e.getMessage());
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
					Log.w(TAG, e.getMessage());
				}
				
				ObjectAnimator animX = ObjectAnimator.ofFloat(find, View.TRANSLATION_X, find.getWidth(), 0);
				animX.setDuration(1000);
				animX.start();
			}
		});
		
		double length = SphericalUtil.computeLength(route);
		TextView routeLength = findViewById(R.id.route_length);
		routeLength.setText(getString(R.string.route_length_format, length));
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

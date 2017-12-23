package ru.spbau.farutin_solikov.gpstracker;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import static android.R.attr.bitmap;
import static android.R.attr.button;
import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStorageDirectory;
import static java.lang.Thread.sleep;

public class RouteActivity extends DrawerActivity implements OnMapReadyCallback {
	private static final int ZOOM = 15;
	private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
	
	private GoogleMap map;
	private String routeName;
	private ArrayList<LatLng> route = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_route);
		
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
		
		Intent intent = getIntent();
		routeName = intent.getStringExtra("ru.spbau.farutin_solikov.gpstracker.route_name");
		ArrayList<Controller.Coordinate> coordinates = intent.getParcelableArrayListExtra("ru.spbau.farutin_solikov.gpstracker.coordinates");
		
		for (Controller.Coordinate coordinate : coordinates) {
			route.add(new LatLng(coordinate.getLat(), coordinate.getLng()));
		}
		
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
					sendSnapshot();
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
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case WRITE_EXTERNAL_STORAGE_REQUEST_CODE: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					sendSnapshot();
				}
			}
		}
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
	
	private void sendSnapshot() {
		GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
			Bitmap bitmap;
			
			@Override
			public void onSnapshotReady(Bitmap snapshot) {
				try {
					bitmap = snapshot;
					
					File outputDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "GPSTracker");
					if (!outputDir.exists()) {
						outputDir.mkdir();
					}
					File outputFile = new File(outputDir, routeName + ".png");
					FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
					
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
					MediaScannerConnection.scanFile(RouteActivity.this, new String[] { outputFile.getPath() }, new String[] { "image/png" }, null);
					
					Uri attachment = Uri.fromFile(outputFile);
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_SEND);
					intent.setType("image/*");
					intent.putExtra(Intent.EXTRA_STREAM, attachment);
					
					startActivity(Intent.createChooser(intent, "Choose app:"));
				} catch (IOException e) {
					Toast.makeText(RouteActivity.this, "failed making screenshot: " + e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		};
		
		map.snapshot(callback);
	}
}

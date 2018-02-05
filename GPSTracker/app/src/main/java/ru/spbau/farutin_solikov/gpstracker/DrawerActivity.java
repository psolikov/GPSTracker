package ru.spbau.farutin_solikov.gpstracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Basic DrawerActivity with navigation.
 */
// @SuppressLint("Registered")
// Все же, видимо, правильнее сделать так как предлагает IDE (в сплывающем сообщении можно нажать "more" и получить больше информации)
public abstract class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
	
	@Override
	@SuppressLint("InflateParams")
	public void setContentView(final int layoutResID) {
		DrawerLayout drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_main, null);
		FrameLayout contentFrame = drawerLayout.findViewById(R.id.content_frame);
		getLayoutInflater().inflate(layoutResID, contentFrame, true);
		super.setContentView(drawerLayout);
				
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();
		
		NavigationView navigationView = findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
		
		ImageView settings = findViewById(R.id.settings);
		settings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(DrawerActivity.this, SettingsActivity.class);
				startActivity(intent);
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			Intent exitApp = new Intent(Intent.ACTION_MAIN);
			exitApp.addCategory(Intent.CATEGORY_HOME);
			exitApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(exitApp);
		}
	}
	
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		int id = item.getItemId();
		Class classToStart = TrackerActivity.class;
		
		if (id == R.id.nav_tracker) {
			classToStart = TrackerActivity.class;
		} else if (id == R.id.nav_alarm) {
			classToStart = AlarmActivity.class;
		} else if (id == R.id.nav_history) {
			classToStart = HistoryActivity.class;
		}
		
		Intent intent = new Intent(DrawerActivity.this, classToStart);
		startActivity(intent);
		
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}
}

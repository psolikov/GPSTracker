package ru.spbau.farutin_solikov.gpstracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

public class DrawerActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener {
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void setContentView(final int layoutResID) {
		DrawerLayout drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_main, null);
		FrameLayout contentFrame = (FrameLayout) drawerLayout.findViewById(R.id.content_frame);
		getLayoutInflater().inflate(layoutResID, contentFrame, true);
		super.setContentView(drawerLayout);
				
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();
		
		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);
	}
	
	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		int id = item.getItemId();
		Class classToStart = TrackerActivity.class;
		
		if (id == R.id.nav_tracker) {
			classToStart = TrackerActivity.class;
		} else if (id == R.id.nav_alarm) {
			classToStart = AlarmActivity.class;
		} else if (id == R.id.nav_history) {
			classToStart = HistoryActivity.class;
		} else if (id == R.id.nav_share) {
			
		}
		
		Intent intent = new Intent(DrawerActivity.this, classToStart);
		startActivity(intent);
		
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}
}

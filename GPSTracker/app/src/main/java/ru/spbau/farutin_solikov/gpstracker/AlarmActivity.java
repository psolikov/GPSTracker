package ru.spbau.farutin_solikov.gpstracker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;

public class AlarmActivity extends DrawerActivity {
	private static final int ALARM_NOTIFICATION_ID = 1;
	
	private Controller.AlarmCoordinatesReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_alarm);
		setUpButtons();
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
		
	public void positionChanged(final Coordinate position) {
		notifyUser();
		setContentView(R.layout.content_alarm_changed);
		
		Button track = findViewById(R.id.track_position);
		track.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				closeNotification();
				
				Intent intent = new Intent(AlarmActivity.this, TrackerActivity.class);
				intent.putExtra(getString(R.string.extra_position), position);
				startActivity(intent);
			}
		});
	}
	
	private void notifyUser() {
		if (Controller.notificationsOn(this)) {
			NotificationCompat.Builder builder =
					new NotificationCompat.Builder(AlarmActivity.this)
							.setSmallIcon(R.mipmap.ic_launcher)
							.setContentTitle(getString(R.string.title_alarm_notification));
			
			Intent resultIntent = new Intent(AlarmActivity.this, AlarmActivity.class);
			PendingIntent resultPendingIntent =
					PendingIntent.getActivity(
							AlarmActivity.this,
							0,
							resultIntent,
							PendingIntent.FLAG_UPDATE_CURRENT
					);
			builder.setContentIntent(resultPendingIntent);
			builder.setAutoCancel(true);
			
			NotificationManager notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notifyManager.notify(ALARM_NOTIFICATION_ID, builder.build());
		}
	}
	
	private void closeNotification() {
		NotificationManager notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notifyManager.cancel(ALARM_NOTIFICATION_ID);
	}
	
	private void setUpButtons() {
		Button alarmOn = findViewById(R.id.alarm_on);
		alarmOn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				setContentView(R.layout.content_alarm_active);
				
				Controller.startCoordinatesService(AlarmActivity.this);
				
				Button alarmOff = findViewById(R.id.alarm_off);
				alarmOff.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						closeNotification();
						setContentView(R.layout.content_alarm);
						setUpButtons();
						
						Controller.stopCoordinatesService();
						
						try {
							unregisterReceiver(receiver);
						} catch (IllegalArgumentException ignored) {
							// no API methods to tell if it is registered at the moment
						}
					}
				});
				
				receiver = new Controller.AlarmCoordinatesReceiver(AlarmActivity.this);
				IntentFilter intentSFilter = new IntentFilter(getString(R.string.broadcast_content_coordinates));
				registerReceiver(receiver, intentSFilter);
			}
		});
	}
}

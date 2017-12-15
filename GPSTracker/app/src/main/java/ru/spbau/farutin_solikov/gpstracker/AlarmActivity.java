package ru.spbau.farutin_solikov.gpstracker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


public class AlarmActivity extends DrawerActivity {
	private static final int ALARM_NOTIFICATION_ID = 1;
	
	private CoordinatesReceiver receiver;
	private Button alarmOn;
	private Button alarmOff;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_alarm);
		setUpButtons();
	}
	
	private class CoordinatesReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Controller.stopCoordinatesService();
			try {
				unregisterReceiver(this);
			} catch (IllegalArgumentException ignored) {
				// no API methods to tell if it is registered at the moment
			}
			
			Bundle notificationData = intent.getExtras();
			final ArrayList<Controller.Coordinate> coordinates =
					notificationData.getParcelableArrayList("ru.spbau.farutin_solikov.gpstracker.coordinates");
			
			notifyUser();
			
			setContentView(R.layout.content_alarm_changed);
			
			Button track = findViewById(R.id.track_position);
			track.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					closeNotification();
					
					Intent intent = new Intent(AlarmActivity.this, TrackerActivity.class);
					intent.putExtra("ru.spbau.farutin_solikov.gpstracker.position", coordinates.get(coordinates.size() - 1));
					startActivity(intent);
				}
			});
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

	private void notifyUser() {
		if (Controller.notificationsOn(this)) {
			NotificationCompat.Builder builder =
					new NotificationCompat.Builder(AlarmActivity.this)
							.setSmallIcon(R.mipmap.ic_launcher)
							.setContentTitle("Position has changed!");
			
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
		alarmOn = findViewById(R.id.alarm_on);
		alarmOn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				setContentView(R.layout.content_alarm_active);
				
				Controller.startCoordinatesService(AlarmActivity.this);
				
				alarmOff = findViewById(R.id.alarm_off);
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
								
				receiver = new CoordinatesReceiver();
				IntentFilter intentSFilter = new IntentFilter("ru.spbau.farutin_solikov.gpstracker.BroadcastCoordinatesAction");
				registerReceiver(receiver, intentSFilter);
			}
		});
	}
}

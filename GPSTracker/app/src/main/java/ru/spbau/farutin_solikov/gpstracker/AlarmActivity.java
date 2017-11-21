package ru.spbau.farutin_solikov.gpstracker;

import android.app.Notification;
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

import static android.R.attr.x;
import static android.R.attr.y;

public class AlarmActivity extends DrawerActivity {
	private CoordinatesReceiver receiver;
	private Button alarmOn;
	private Button alarmOff;
	private TextView textView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_alarm);
		
		setUpButtons();
	}
	
	private class CoordinatesReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle notificationData = intent.getExtras();
			ArrayList<Controller.Coordinate> coordinates =
					notificationData.getParcelableArrayList("ru.spbau.farutin_solikov.gpstracker.coordinates");
			
			Controller.Coordinate pos = coordinates.get(coordinates.size() - 1);
			
			NotificationCompat.Builder builder =
					new NotificationCompat.Builder(AlarmActivity.this)
							.setSmallIcon(R.mipmap.ic_launcher)
							.setContentTitle("Position has changed!")
							.setContentText("Current coordinates: " + "(" + pos.getLat() + ", " + pos.getLng() + ")");
			
			Intent resultIntent = new Intent(AlarmActivity.this, AlarmActivity.class);
			PendingIntent resultPendingIntent =
					PendingIntent.getActivity(
							AlarmActivity.this,
							0,
							resultIntent,
							PendingIntent.FLAG_UPDATE_CURRENT
					);
			builder.setContentIntent(resultPendingIntent);
			
			int notificationId = 1;
			NotificationManager notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notifyManager.notify(notificationId, builder.build());
			
			// debug
			textView.setText("(" + pos.getLat() + ", " + pos.getLng() + ")");
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
				
				textView = findViewById(R.id.tmp);
				
				receiver = new CoordinatesReceiver();
				IntentFilter intentSFilter = new IntentFilter("ru.spbau.farutin_solikov.gpstracker.BroadcastCoordinatesAction");
				registerReceiver(receiver, intentSFilter);
			}
		});
	}
}

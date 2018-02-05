package ru.spbau.farutin_solikov.gpstracker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Alarm mode - notifying user once vehicle has changed position.
 */
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
	
	/**
	 * Notifies user and changes layout content.
	 *
	 * @param position position where vehicle was moved to
	 */
	public void positionChanged(final Coordinate position) {
		notifyUser();
		
		Button track = findViewById(R.id.alarm_button);
		track.setText(R.string.title_button_track_position);
		track.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				closeNotification();
				
				Intent intent = new Intent(AlarmActivity.this, TrackerActivity.class);
				intent.putExtra(getString(R.string.extra_position), position);
				startActivity(intent);
			}
		});
		
		TextView message = findViewById(R.id.alarm_message);
		message.setVisibility(View.VISIBLE);
	}
	
	private void notifyUser() {
		if (Controller.notificationsOn(this)) {
			NotificationCompat.Builder builder =
					new NotificationCompat.Builder(AlarmActivity.this, getString(R.string.alarm_channel_id))
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
			assert notifyManager != null;
			// IDE тут "жаловался" потому что теоретически `notifyManager` может содержать `null`,
			// один из способов его успокоить это написать `assert`.
			// Лучше, конечно, как-то обрабобтать эту ситуацию, но это не всегда возможно.
			notifyManager.notify(ALARM_NOTIFICATION_ID, builder.build());
		}
	}
	
	private void closeNotification() {
		NotificationManager notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		assert notifyManager != null;
		notifyManager.cancel(ALARM_NOTIFICATION_ID);
	}
	
	private void setUpButtons() {
		final Button alarmButton = findViewById(R.id.alarm_button);
		alarmButton.setText(R.string.title_button_turn_on);
		alarmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Controller.startCoordinatesService(AlarmActivity.this);
				
				alarmButton.setText(R.string.title_button_turn_off);
				alarmButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						closeNotification();
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

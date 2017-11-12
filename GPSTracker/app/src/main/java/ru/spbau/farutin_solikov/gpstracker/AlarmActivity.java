package ru.spbau.farutin_solikov.gpstracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
	
	public class CoordinatesReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle notificationData = intent.getExtras();
			double x = notificationData.getDouble("xCoordinate");
			double y = notificationData.getDouble("yCoordinate");
			
			textView.setText("(" + x + ", " + y + ")");
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
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
					}
				});
				
				textView = findViewById(R.id.tmp);
				
				receiver = new CoordinatesReceiver();
				IntentFilter intentSFilter = new IntentFilter("BroadcastCoordinatesAction");
				registerReceiver(receiver, intentSFilter);
			}
		});
	}
}

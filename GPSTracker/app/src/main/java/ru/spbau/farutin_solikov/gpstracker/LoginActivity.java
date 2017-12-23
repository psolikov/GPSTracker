package ru.spbau.farutin_solikov.gpstracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity to log in.
 */
public class LoginActivity extends AppCompatActivity {
	private EditText deviceId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Controller.userLoggedIn(LoginActivity.this)) {
			loginSuccess();
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		deviceId = findViewById(R.id.device_id);
		deviceId.addTextChangedListener(new FourLetterFormatWatcher());
		
		Button login = findViewById(R.id.login_button);
		login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String sDeviceId = deviceId.getText().toString();
				
				if (Controller.checkDeviceId(sDeviceId)) {
					Controller.saveUserDeviceId(LoginActivity.this, sDeviceId);
					loginSuccess();
				} else {
					Toast.makeText(LoginActivity.this, getString(R.string.toast_login), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	private void loginSuccess() {
		Intent intent = new Intent(LoginActivity.this, TrackerActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Changes input id so that it corresponds following format:
	 * XXXX XXXX XXXX ...
	 */
	private class FourLetterFormatWatcher implements TextWatcher {
		private static final char SPACE = ' ';
		private boolean lock;
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			if (lock) {
				return;
			}
			
			lock = true;
			
			for (int i = 0; i < s.length(); i++) {
				if (s.charAt(i) == SPACE) {
					s.delete(i, i + 1);
				}
			}
			
			for (int i = 0; i < s.length(); i++) {
				if (Character.isLowerCase(s.charAt(i))) {
					s.replace(i, i + 1, String.valueOf(Character.toUpperCase(s.charAt(i))));
				}
			}
			
			for (int i = 4; i < s.length(); i += 5) {
				s.insert(i, String.valueOf(SPACE));
			}
			
			lock = false;
		}
	}
}

package ua.com.anyapps.makemoneyfast;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "debapp";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // переход на активити авторизации
        Intent intent = new Intent(this, EmailPasswordActivity.class);
        startActivity(intent);
        finish();
    }
}

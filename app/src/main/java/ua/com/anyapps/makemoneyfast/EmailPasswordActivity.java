package ua.com.anyapps.makemoneyfast;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailPasswordActivity extends AppCompatActivity {
    private static final String TAG = "debapp";

    // https://github.com/firebase/quickstart-android/blob/master/auth/app/src/main/java/com/google/firebase/quickstart/auth/java/EmailPasswordActivity.java

    EditText etEmail;
    EditText etPassword;
    TextView tvStatusString;

    private FirebaseAuth mAuth;

    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_password);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvStatusString = findViewById(R.id.tvStatusString);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // пользователь уже вошел - переход на главное активити
            Intent intent = new Intent(EmailPasswordActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void btnSignInClick(View v) {
        if (!validateForm()) {
            return;
        }
        tvStatusString.setText(getResources().getString(R.string.email_password_status_string_message_0));
        mProgressDialog = MyFunctions.showProgressDialog(this, mProgressDialog, getResources().getString(R.string.progress_dialogLoading_text));
        mAuth.signInWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            // пользователь подошел - вход
                            Intent intent = new Intent(EmailPasswordActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            tvStatusString.setText(getResources().getString(R.string.email_password_status_string_message_1));
                        }
                        MyFunctions.hideProgressDialog(mProgressDialog);
                    }
                });
    }

    // нажата кнопка регистрации
    public void btnRegistrationClick(View v) {
        if (!validateForm()) {
            return;
        }
        tvStatusString.setText(getResources().getString(R.string.email_password_status_string_message_0));
        mProgressDialog = MyFunctions.showProgressDialog(this, mProgressDialog, getResources().getString(R.string.progress_dialogLoading_text));
        mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            // пользователь зарегистрировался - переход на главную активити
                            Intent intent = new Intent(EmailPasswordActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            tvStatusString.setText(getResources().getString(R.string.email_password_status_string_message_1));
                        }
                        MyFunctions.hideProgressDialog(mProgressDialog);
                        // ...
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = etEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Required.");
            valid = false;
        } else {
            etEmail.setError(null);
        }

        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Required.");
            valid = false;
        } else {
            etPassword.setError(null);
        }

        return valid;
    }
}

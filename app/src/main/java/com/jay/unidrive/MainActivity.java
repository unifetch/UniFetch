package com.jay.unidrive;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity {

    EditText usernameEditText;
    EditText passwordEditText;
    Button loginButton;
    TextView signupTextView;
    boolean isLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Save the current Installation to Back4App
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signupTextView = findViewById(R.id.signupTextView);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i != EditorInfo.IME_ACTION_NONE){
                    attemptLogin(loginButton);
                }
                return false;
            }
        });

        if (ParseUser.getCurrentUser() != null){
            redirectActivity();
        }
    }

    public void attemptLogin(View view) {
        if (isLogin) {
            ParseUser.logInInBackground(usernameEditText.getText().toString(), passwordEditText.getText().toString(), new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e == null) {
                        usernameEditText.getText().clear();
                        passwordEditText.getText().clear();
                        redirectActivity();
                    } else {
                        Log.i("Info", "Login Failed");
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            ParseUser user = new ParseUser();
            user.setUsername(usernameEditText.getText().toString());
            user.setPassword(passwordEditText.getText().toString());
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        usernameEditText.getText().clear();
                        passwordEditText.getText().clear();
                        Toast.makeText(MainActivity.this, "Sign Up Successfully", Toast.LENGTH_SHORT).show();
                        switchSignUp(loginButton);
                    } else {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void redirectActivity() {
        Intent intent = new Intent(MainActivity.this, RidersActivity.class);
        startActivity(intent);
    }

    public void switchSignUp(View view){
        if (isLogin) {
            isLogin = false;
            loginButton.setText("SignUp");
            signupTextView.setText("Back to Login");
        } else {
            isLogin = true;
            loginButton.setText("Login");
            signupTextView.setText("Don't have an account? Sign Up Here.");
        }
    }


}

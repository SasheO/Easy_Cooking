package com.example.groceriesmanager.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceriesmanager.Models.User;
import com.example.groceriesmanager.R;
import com.parse.ParseException;
import com.parse.SignUpCallback;



public class RegisterActivity extends AppCompatActivity {

    private TextView tvLogin;
    private EditText etRegisterEmail;
    private EditText etRegisterUsername;
    private EditText etRegisterPassword;
    private Button btnRegister;
    private static final String TAG = "RegisterActivity";
    private String username;
    private String password;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tvLogin = findViewById(R.id.tvLogin);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etRegisterUsername = findViewById(R.id.etRegisterUsername);
        btnRegister = findViewById(R.id.btnRegister);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);


        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // what will happen when the user clicks the sign up button
                username = etRegisterUsername.getText().toString();
                password = etRegisterPassword.getText().toString();
                email = etRegisterEmail.getText().toString();

                // check if username is valid i.e. no spaces
                if (username.contains(" ")) {
                    Toast.makeText(RegisterActivity.this, "username cannot have spaces", Toast.LENGTH_LONG).show();
                } else if (username.length() == 0) {
                    Toast.makeText(RegisterActivity.this, "please choose a username", Toast.LENGTH_LONG).show();
                }
                else if (password.length() == 0){ // todo: change to checks for sufficient password complexity
                    Toast.makeText(RegisterActivity.this, "please type a password", Toast.LENGTH_LONG).show();
                }
                else if (!isValidEmail(email)){
                    Toast.makeText(RegisterActivity.this, "please type a valid email address", Toast.LENGTH_LONG).show();
                }
                else {
                    // create grocery and pantry list for the new user

                    createNewUser(username, password, email);

                }
            }});
    }

    private void goToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        // line below ensures user cannot go back to register page by repeatedly pressing back
        finish();
    }

    private void createNewUser(String username, String password, String email) {
        // create new user
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setEmail(email);

        // sign them up in server
        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(RegisterActivity.this, "error encountered signing up", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "error encountered signing up new user: " + e.toString());
                } else {
                    Toast.makeText(RegisterActivity.this, "Welcome to " + getResources().getString(R.string.app_name), Toast.LENGTH_LONG).show();
                    goToMainActivity();
                }
            }
        });
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
package com.example.sweethouse;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class sign_in extends AppCompatActivity {

    ImageView seen, hide;
    TextView sign_up;
    EditText number, password;
    Button button, send;
    SQLiteDatabase db;
    SharedPreferences sp;
    private static final int SMS_PERMISSION_CODE = 1;
    Random r = new Random();
    AlertDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        connect sp
        sp = getSharedPreferences(Session.pref, MODE_PRIVATE);

//        database connection
        db = openOrCreateDatabase("Sweethouse.db", MODE_PRIVATE, null);

        number = findViewById(R.id.sign_in_number);
        password = findViewById(R.id.sign_in_password);
        button = findViewById(R.id.sign_in_button);
        send = findViewById(R.id.sign_in_send);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (number.getText().toString().trim().equals("")){
                    number.setError("number required");
                }
                else if (!(number.getText().toString().trim().length() == 10)){
                    number.setError("invalid number");
                }
                else {
                    password.setVisibility(View.VISIBLE);
                    send.setVisibility(View.GONE);
                    button.setVisibility(View.VISIBLE);
                    AlertDialog.Builder builder = new AlertDialog.Builder(sign_in.this);
                    View progress = LayoutInflater.from(sign_in.this).inflate(R.layout.custom_progress, null);
                    builder.setView(progress);
                    builder.setCancelable(false);
                    dialog = builder.create();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                    checknumber();
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (number.getText().toString().trim().equals("")){
                    number.setError("number required");
                }
                else if (!(number.getText().toString().trim().length() == 10)){
                    number.setError("invalid number");
                }
                else if (password.getText().toString().trim().equals("")) {
                    password.setError("OTP required");
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(sign_in.this);
                    View progress = LayoutInflater.from(sign_in.this).inflate(R.layout.custom_progress, null);
                    builder.setView(progress);
                    builder.setCancelable(false);
                    dialog = builder.create();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                    checkOTP();
                }
            }
        });

        sign_up = findViewById(R.id.sign_in_up);

//        registration
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sign_in.this, com.example.sweethouse.sign_up.class);
                startActivity(intent);
            }
        });
    }

    private void checkOTP() {
        if (password.getText().toString().equals(sp.getString(Session.OTP, ""))){
            new CommonMethod(sign_in.this, "Login successfully");
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("details");
            Query check = myRef.orderByChild("number").equalTo(number.getText().toString());

            check.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.exists()){
                        dialog.dismiss();
                        sp.edit().putString(Session.name, snapshot.child(number.getText().toString()).child("name").getValue(String.class)).commit();
                        sp.edit().putString(Session.number, snapshot.child(number.getText().toString()).child("number").getValue(String.class)).commit();
                        sp.edit().putString(Session.email, snapshot.child(number.getText().toString()).child("email").getValue(String.class)).commit();
                        sp.edit().putString(Session.pass, snapshot.child(number.getText().toString()).child("password").getValue(String.class)).commit();

                        sp.edit().putString(Session.OTP, "").commit();

                        Intent intent = new Intent(sign_in.this, MainActivity.class);
                        dialog.dismiss();
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    dialog.dismiss();
                }
            });
        }
        else {
            dialog.dismiss();
            new CommonMethod(sign_in.this, "Wrong OTP");
        }
    }

    private void checknumber() {


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("details");
        Query check = myRef.orderByChild("number").equalTo(number.getText().toString());

        check.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    String phone = number.getText().toString();
                    int i1 = r.nextInt(9999 - 1111);
                    sp.edit().putString(Session.OTP, String.valueOf(i1)).commit();
                    String message = i1 + " is your Login OTP for Sweet House app. Do not share it with anyone";

                    if (ContextCompat.checkSelfPermission(sign_in.this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(sign_in.this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
                        checknumber();
                    } else {
                        SmsManager smsManager = SmsManager.getDefault();
                        try {
                            smsManager.sendTextMessage(phone, null, message, null, null);
                            Toast.makeText(sign_in.this, "OTP sent!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(sign_in.this, "Failed to send message.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    dialog.dismiss();
                }
                else {
                    dialog.dismiss();
                    new CommonMethod(sign_in.this, "number don't exists");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
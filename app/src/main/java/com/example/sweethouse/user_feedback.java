package com.example.sweethouse;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class user_feedback extends AppCompatActivity {

    EditText user_feedback;
    Button send;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_feedback);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sp = getSharedPreferences(Session.pref, MODE_PRIVATE);

        user_feedback = findViewById(R.id.user_feedback);
        send = findViewById(R.id.send_button);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user_feedback.getText().toString().trim().equals("")){
                    user_feedback.setError("required");
                }
                else {
                    FirebaseDatabase firebase = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = firebase.getReference("user_Feedback");

                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
                    DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("hh:mm a");
                    LocalDateTime now = LocalDateTime.now();

                    HashMap<String, String> val = new HashMap<>();
                    val.put("name", sp.getString(Session.name, ""));
                    val.put("email", sp.getString(Session.email, ""));
                    val.put("feedback", user_feedback.getText().toString());
                    val.put("date", dtf.format(now));
                    val.put("time", dtf2.format(now));

                    String key = myRef.push().getKey();

                    myRef.child(key).setValue(val).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            user_feedback.setText("");
                            new CommonMethod(user_feedback.this, "Feedback sent");
                        }
                    });
                }
            }
        });
    }
}
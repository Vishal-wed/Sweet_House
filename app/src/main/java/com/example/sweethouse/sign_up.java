package com.example.sweethouse;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class sign_up extends AppCompatActivity {

    String patten = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]+";
    ProgressDialog pd;
    TextView sign_in;
    EditText name, number, email, pass, cpass;
    Button button;
    SQLiteDatabase db;
    SharedPreferences sp;
    ImageView seen,seen2, hide, hide2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        connent sp
        sp = getSharedPreferences(Session.pref, MODE_PRIVATE);

//        connect database
        db = openOrCreateDatabase("Sweethouse.db", MODE_PRIVATE, null);

        name = findViewById(R.id.sign_up_name);
        number = findViewById(R.id.sign_up_number);
        email = findViewById(R.id.sign_up_email);
        pass = findViewById(R.id.sign_up_password);
        cpass = findViewById(R.id.sign_up_cpassword);
        button = findViewById(R.id.sign_up_button);

//        seen password

        seen = findViewById(R.id.seen);
        hide = findViewById(R.id.hide);

        seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seen.setVisibility(View.GONE);
                hide.setVisibility(View.VISIBLE);
                pass.setTransformationMethod(null);
            }
        });

        hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seen.setVisibility(View.VISIBLE);
                hide.setVisibility(View.GONE);
                pass.setTransformationMethod(new PasswordTransformationMethod());
            }
        });

        seen2 = findViewById(R.id.seen2);
        hide2 = findViewById(R.id.hide2);

        seen2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seen2.setVisibility(View.GONE);
                hide2.setVisibility(View.VISIBLE);
                cpass.setTransformationMethod(null);
            }
        });

        hide2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seen2.setVisibility(View.VISIBLE);
                hide2.setVisibility(View.GONE);
                cpass.setTransformationMethod(new PasswordTransformationMethod());
            }
        });

//        input condition

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (name.getText().toString().trim().equals("")){
                    name.setError("name required");
                }
                else if (number.getText().toString().trim().equals("")){
                    number.setError("number required");
                }
                else if (!(number.getText().toString().trim().length() == 10)){
                    number.setError("invalid number");
                }
                else if (email.getText().toString().trim().equals("")){
                    email.setError("Email required");
                }
                else if (!email.getText().toString().trim().matches(patten)){
                    email.setError("invalid email");
                }
                else if (pass.getText().toString().trim().equals("")){
                    pass.setError("Password required");
                }
                else if (pass.getText().toString().trim().length() < 6){
                    pass.setError("Password have minimum 6 char");
                }
                else if (cpass.getText().toString().trim().equals("")){
                    cpass.setError("Password required");
                }
                else if (!cpass.getText().toString().trim().equals(pass.getText().toString().trim())){
                    cpass.setError("Password don't match");
                }
                    else {
                       if (new ConnectionDetector(sign_up.this).networkConnected()){
//                           new CommonMethod(sign_up.this, "connect");
//                           new doSignup().execute();
                               pd = new ProgressDialog(sign_up.this);
                               pd.setMessage("Please Wait...");
                               pd.setCancelable(false);
                               pd.show();
                             firesign();

                             Intent i = new Intent(sign_up.this, sign_in.class);
                             startActivity(i);
                       }
                       else {
                           new ConnectionDetector(sign_up.this).networkDisconnected();
                       }
                    }
                }
        });

//        for login
        sign_in = findViewById(R.id.sign_up_in);

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sign_up.this, com.example.sweethouse.sign_in.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }


    public void firesign () {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("details");
        Query check = myRef.orderByChild("number").equalTo(number.getText().toString());

        check.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    pd.dismiss();
                    new CommonMethod(sign_up.this, "Number is already in use");
                }
                else {
                    HashMap<String, String> values = new HashMap<>();
                    values.put("name", name.getText().toString());
                    values.put("number", number.getText().toString());
                    values.put("email", email.getText().toString());
                    values.put("password", pass.getText().toString());

                    myRef.child(values.get("number")).setValue(values).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            pd.dismiss();
                            new CommonMethod(sign_up.this, "Sign in successfully");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
package com.example.sweethouse;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

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

import java.util.HashMap;

public class editProfile extends AppCompatActivity {

    EditText name, number, email, password, cpassword;
    Button submit, edit;
    ImageView seen,seen2,hide,hide2;
    SharedPreferences sp;
    FrameLayout cpass_layout;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sp = getSharedPreferences(Session.pref, MODE_PRIVATE);

        name = findViewById(R.id.edit_name);
        number = findViewById(R.id.edit_number);
        email = findViewById(R.id.edit_email);
        password = findViewById(R.id.edit_password);
        cpassword = findViewById(R.id.edit_cpassword);
        submit = findViewById(R.id.submit_button);
        edit = findViewById(R.id.edit_button);
        seen = findViewById(R.id.seen);
        seen2 = findViewById(R.id.seen2);
        hide = findViewById(R.id.hide);
        hide2 = findViewById(R.id.hide2);
        cpass_layout = findViewById(R.id.cpass_layout);

        seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seen.setVisibility(View.GONE);
                hide.setVisibility(View.VISIBLE);
                password.setTransformationMethod(null);
            }
        });

        hide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide.setVisibility(View.GONE);
                seen.setVisibility(View.VISIBLE);
                password.setTransformationMethod(new PasswordTransformationMethod());
            }
        });

        seen2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seen2.setVisibility(View.GONE);
                hide2.setVisibility(View.VISIBLE);
                cpassword.setTransformationMethod(null);
            }
        });

        hide2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide2.setVisibility(View.GONE);
                seen2.setVisibility(View.VISIBLE);
                cpassword.setTransformationMethod(new PasswordTransformationMethod());
            }
        });

        block(false);

        name.setText(sp.getString(Session.name, ""));
        number.setText(sp.getString(Session.number, ""));
        email.setText(sp.getString(Session.email, ""));
        password.setText(sp.getString(Session.pass, ""));
        cpassword.setText(sp.getString(Session.pass, ""));

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                block(true);
                edit.setVisibility(View.GONE);
                submit.setVisibility(View.VISIBLE);
                cpass_layout.setVisibility(View.VISIBLE);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd = new ProgressDialog(editProfile.this);
                pd.setMessage("Please Wait...");
                pd.setCancelable(false);
                pd.show();

                firesign();
            }
        });

    }

    private void block(boolean condition) {
        number.setEnabled(false);
        name.setEnabled(condition);
        email.setEnabled(condition);
        password.setEnabled(condition);
        cpassword.setEnabled(condition);
    }

    public void firesign () {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("details");
        Query check = myRef.orderByChild("number").equalTo(number.getText().toString());

        check.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("details").child(sp.getString(Session.number, ""));

                myRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        add();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void add () {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("details");
        Query check = myRef.orderByChild("number").equalTo(number.getText().toString());

        check.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, String> values = new HashMap<>();
                values.put("name", name.getText().toString());
                values.put("number", number.getText().toString());
                values.put("email", email.getText().toString());
                values.put("password", password.getText().toString());

                myRef.child(values.get("number")).setValue(values).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.dismiss();
                        new CommonMethod(editProfile.this, "Update successfully");

                        sp.edit().putString(Session.name, name.getText().toString()).commit();
                        sp.edit().putString(Session.number, number.getText().toString()).commit();
                        sp.edit().putString(Session.email, email.getText().toString()).commit();
                        sp.edit().putString(Session.pass, password.getText().toString()).commit();

                        sp.edit().putString(Session.goProfile, "profile");
                        Intent intent = new Intent(editProfile.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

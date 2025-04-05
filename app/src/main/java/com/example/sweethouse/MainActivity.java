package com.example.sweethouse;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    View home_line, category_line, cart_line, profile_line;
    int total;
//    sp
    SharedPreferences sp;

//    nav item
    ImageView home, category, cart, profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        connect sp
        sp = getSharedPreferences(Session.pref, MODE_PRIVATE);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("cart");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String db_email = dataSnapshot.child("email").getValue(String.class);
                        if (Objects.equals(db_email, sp.getString(Session.email, ""))) {
                            total += Integer.parseInt(dataSnapshot.child("total_amount").getValue(String.class));
                            sp.edit().putString(Session.total_amount, String.valueOf(total)).commit();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        fragment set and condition

        home_line = findViewById(R.id.home_line);
        category_line = findViewById(R.id.category_line);
        cart_line = findViewById(R.id.cart_line);
        profile_line = findViewById(R.id.profile_line);

        if (sp.getString(Session.goProfile,"").equals("profile")){
//            clearColor();
            profile_line.setBackgroundColor(Color.parseColor("#f37b2d"));
            FragmentManager manager4 = getSupportFragmentManager();
            manager4.beginTransaction().replace(R.id.fragment_place, new profileFragment()).commit();
        } else if (sp.getString(Session.goProfile, "").equals("cart")) {
            cart_line.setBackgroundColor(Color.parseColor("#f37b2d"));
            FragmentManager manager4 = getSupportFragmentManager();
            manager4.beginTransaction().replace(R.id.fragment_place, new CartFragment()).commit();
        } else {
//            clearColor();
            home_line.setBackgroundColor(Color.parseColor("#f37b2d"));
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.fragment_place, new HomeFragment()).commit();
        }

//        fragment changer

        home = findViewById(R.id.home_item);
        cart = findViewById(R.id.cart);
        category = findViewById(R.id.category);
        profile = findViewById(R.id.profile);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearColor();
                home_line.setBackgroundColor(Color.parseColor("#f37b2d"));
                FragmentManager manager = getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.fragment_place, new HomeFragment()).commit();
            }
        });

        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearColor();
                category_line.setBackgroundColor(Color.parseColor("#f37b2d"));
                FragmentManager manager2 = getSupportFragmentManager();
                manager2.beginTransaction().replace(R.id.fragment_place, new CategoryFragment()).commit();
            }
        });

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearColor();
                cart_line.setBackgroundColor(Color.parseColor("#f37b2d"));
                FragmentManager manager3 = getSupportFragmentManager();
                manager3.beginTransaction().replace(R.id.fragment_place, new CartFragment()).commit();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearColor();
                profile_line.setBackgroundColor(Color.parseColor("#f37b2d"));
                FragmentManager manager4 = getSupportFragmentManager();
                manager4.beginTransaction().replace(R.id.fragment_place, new profileFragment()).commit();
            }
        });

    }
    public void clearColor () {
        home_line.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        category_line.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        cart_line.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        profile_line.setBackgroundColor(Color.parseColor("#00FFFFFF"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
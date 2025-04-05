package com.example.sweethouse;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class orderHistory extends AppCompatActivity {

    RecyclerView recycle;
    ArrayList<orderList> arrayList;
    SharedPreferences sp;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sp = getSharedPreferences(Session.pref, MODE_PRIVATE);

        recycle = findViewById(R.id.order_area);

        recycle.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        recycle.setItemAnimator(new DefaultItemAnimator());

        arrayList = new ArrayList<orderList>();

        selectOrder();


//        order_adopter adopter = new order_adopter(orderHistory.this, );
//        recycle.setAdapter(adopter);
    }

    private void selectOrder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(orderHistory.this);
        View progress = LayoutInflater.from(orderHistory.this).inflate(R.layout.custom_progress, null);
        builder.setView(progress);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Transaction");

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String db_email = dataSnapshot.child("email").getValue(String.class);
                    if (Objects.equals(db_email, sp.getString(Session.email, ""))) {
                        orderList values = dataSnapshot.getValue(orderList.class);
                        arrayList.add(values);
                    }
                }
                Collections.reverse(arrayList);
                order_adopter adopter = new order_adopter(orderHistory.this, arrayList);
                dialog.dismiss();
                recycle.setAdapter(adopter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sp.edit().putString(Session.goProfile, "profile").commit();
        Intent intent = new Intent(orderHistory.this, MainActivity.class);
        startActivity(intent);
    }
}
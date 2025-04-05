package com.example.sweethouse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.ims.ImsMmTelManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class order_item extends AppCompatActivity {
    
    TextView time, status, order, address, payment_type, amount, date;
    ImageView status_img;
    Button cancel;
    SharedPreferences sp;
    ArrayList<itemList> arrayList;
    RecyclerView recycle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sp = getSharedPreferences(Session.pref, MODE_PRIVATE);

        order = findViewById(R.id.order);
        address = findViewById(R.id.address);
        payment_type = findViewById(R.id.payment_type);
        amount = findViewById(R.id.amount);
        time = findViewById(R.id.time);
        status = findViewById(R.id.status_text);
        status_img = findViewById(R.id.status_img);
        date = findViewById(R.id.date);
        recycle = findViewById(R.id.item_recycle);
        cancel = findViewById(R.id.cancel_order);

        date.setText(sp.getString(Session.date, ""));
        order.setText(sp.getString(Session.order_id, ""));
        address.setText("Address: "+sp.getString(Session.address, ""));
        payment_type.setText(sp.getString(Session.payment_type, ""));
        amount.setText("₹"+sp.getString(Session.amount, ""));
        time.setText(sp.getString(Session.time, ""));

        if (Objects.equals(sp.getString(Session.status_text, ""), "pending")){
            cancel.setVisibility(View.VISIBLE);
        }else {
            cancel.setVisibility(View.GONE);
        }

        if (Objects.equals(sp.getString(Session.status_text, ""), "pending")){
            status.setText(sp.getString(Session.status_text, ""));
            status_img.setImageResource(R.drawable.pending);
        }
        else if (Objects.equals(sp.getString(Session.status_text, ""), "success")){
            status.setText(sp.getString(Session.status_text, ""));
            status_img.setImageResource(R.drawable.success);
        }
        else {
            status.setText(sp.getString(Session.status_text, ""));
            status_img.setImageResource(R.drawable.fail);
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove_order();
            }
        });

        recycle.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        recycle.setItemAnimator(new DefaultItemAnimator());

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("OrderItem");
        arrayList = new ArrayList<itemList>();

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String db_order_id = dataSnapshot.child("order_id").getValue(String.class);
                    if (Objects.equals(db_order_id, sp.getString(Session.order_id, ""))) {
                        itemList values = dataSnapshot.getValue(itemList.class);
                        arrayList.add(values);
                    }
                }
                order_item_adopter adopter = new order_item_adopter(order_item.this, arrayList);
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
        Intent i = new Intent(order_item.this, orderHistory.class);
        startActivity(i);
    }

    public void remove_order () {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Transaction").child(sp.getString(Session.transaction_id, ""));

        myRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
//                remove_item();
                new CommonMethod(order_item.this, "canceled successfully");
                Intent i = new Intent(order_item.this, orderHistory.class);
                startActivity(i);
            }
        });
    }

    public void remove_item () {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("OrderItem");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String key = snapshot.child("order_id").getValue(String.class);
                if (sp.getString(Session.order_id, "").equals(key)){
                    DatabaseReference myRef2 = database.getReference("OrderItem").child(snapshot.getKey());
                    myRef2.removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        myRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }
}
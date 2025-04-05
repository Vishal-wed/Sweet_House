package com.example.sweethouse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.telephony.SmsManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.StartupTime;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class payout_details extends AppCompatActivity {

    String patten = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]+";
    SharedPreferences sp;
    Spinner payment_type;
    String [] types = {"Pay Online", "Cash On Delivery"};
    EditText name, number, email, address;
    static Button order;
    int index;
    SQLiteDatabase db;
    ArrayList<cart_list> arrayList;
    AlertDialog dialog;
    String all_item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payout_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = openOrCreateDatabase("Sweethouse.db", MODE_PRIVATE, null);
        sp = getSharedPreferences(Session.pref, MODE_PRIVATE);
        payment_type = findViewById(R.id.type);
        name = findViewById(R.id.name);
        number = findViewById(R.id.number);
        email = findViewById(R.id.email);
        address = findViewById(R.id.address);
        order = findViewById(R.id.checkOut);

        name.setText(sp.getString(Session.name, ""));
        number.setText(sp.getString(Session.number, ""));
        email.setText(sp.getString(Session.email, ""));
        sp.edit().putString(Session.payment, "no").commit();

        ArrayAdapter<String>  adopter = new ArrayAdapter<String>(payout_details.this, android.R.layout.simple_spinner_dropdown_item, types);
        adopter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        payment_type.setAdapter(adopter);

        payment_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                index = (int) adapterView.getItemIdAtPosition(i);
                if (index == 0){
                    order.setText("Complete payment");
                }else {
                    order.setText("Check Out");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        order.setOnClickListener(new View.OnClickListener() {
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
                else if (address.getText().toString().trim().equals("")){
                    address.setError("address required");
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(payout_details.this);
                    View progress = LayoutInflater.from(payout_details.this).inflate(R.layout.custom_progress, null);
                    builder.setView(progress);
                    builder.setCancelable(false);
                    dialog = builder.create();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                    if (index == 1) {
                        addTransaction();
                        send_message();
                    }
                    else if (index == 0){
                        checkPayment();
                    }
                    dialog.dismiss();
                }
            }
        });

        if (sp.getString(Session.payment, "").equals("yes")){
            addTransaction();
            sp.edit().putString(Session.payment, "no").commit();
        }
    }

    public void checkPayment() {
        if (index == 0){
            if (sp.getString(Session.payment, "").equals("no")){
                Intent intent = new Intent(payout_details.this, onlinePayment.class);
                startActivity(intent);
                new CommonMethod(payout_details.this, "Complete your payment");
            }
            else if (sp.getString(Session.payment, "").equals("yes")){
                addTransaction();
                sp.edit().putString(Session.payment, "no").commit();
                send_message();
            }
        }
    }

    public void addTransaction() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Transaction");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("hh:mm a");

        LocalDateTime now = LocalDateTime.now();
        String key = myRef.push().getKey();

        sp.edit().putString(Session.total_size, key).commit();

        HashMap<String, String> val = new HashMap<>();
        val.put("transation_id", key);
        val.put("order_id", key);
        val.put("name", name.getText().toString());
        val.put("number", number.getText().toString());
        val.put("email", email.getText().toString());
        val.put("address", address.getText().toString());
        val.put("payment_type", types[index]);
        if (sp.getString(Session.product, "").equals("single")) {
            val.put("amount", sp.getString(Session.product_price, ""));
            all_item = sp.getString(Session.product_price, "");
        }else {
            val.put("amount", sp.getString(Session.total_amount, ""));
            all_item = sp.getString(Session.total_amount, "");
        }
        val.put("status", "pending");
        val.put("date", dtf.format(now));
        val.put("time", dtf2.format(now));



        myRef.child(key).setValue(val).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (sp.getString(Session.product, "").equals("single")) {
                    addItem();
                }else {
                    countItem();
                }
                new CommonMethod(payout_details.this, "order Successfully");

                sp.edit().putString(Session.goProfile, "cart").commit();
                dialog.dismiss();
                Intent intent = new Intent(payout_details.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public void countItem() {

        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
        DatabaseReference myRef2 = database2.getReference("cart");

        arrayList = new ArrayList<>();
        myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String db_email = dataSnapshot.child("email").getValue(String.class);
                        if (Objects.equals(db_email, sp.getString(Session.email, ""))) {
                            cart_list list = new cart_list();
                            list.setItem_id(dataSnapshot.child("item_id").getValue(String.class));
                            list.setProduct_quantity(dataSnapshot.child("product_quantity").getValue(String.class));
                            list.setTotal_amount(dataSnapshot.child("total_amount").getValue(String.class));
                            arrayList.add(list);
                        }
                    }
                    addItem();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void addItem () {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("OrderItem");
        HashMap<String, String> val = new HashMap<>();

        if (sp.getString(Session.product, "").equals("single")){
            val.put("order_id", sp.getString(Session.total_size, ""));
            val.put("item_id", sp.getString(Session.product_id, ""));
            val.put("quantity", sp.getString(Session.quantity, ""));
            val.put("amount", sp.getString(Session.product_price, ""));
            String key = myRef.push().getKey();

            myRef.child(key).setValue(val).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
            sp.edit().putString(Session.product, "").commit();
        }else {
            for (int i = 0; i < arrayList.size(); i++) {
                val.put("order_id", sp.getString(Session.total_size, ""));
                val.put("item_id", String.valueOf(arrayList.get(i).getItem_id()));
                val.put("quantity", String.valueOf(arrayList.get(i).getProduct_quantity()));
                val.put("amount", String.valueOf(arrayList.get(i).getTotal_amount()));
                String key = myRef.push().getKey();

                myRef.child(key).setValue(val).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
            }
        }
    }

    public void send_message () {
        String message = "Your order of Rupees "+all_item+" is added successfully in Queue. Your order reach within 30 min.";
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number.getText().toString(), null, message, null, null);
    }

    public static void order_payment_done() {
        order.setText("Check Out");
    }
}
package com.example.sweethouse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class product_display extends AppCompatActivity {

    ImageView img, back, cart, addCart, cart_add, cart_remove;
    TextView product_name, product_quantity, product_price, product_desc, category_display, cart_quantity;
    SharedPreferences sp;
    SQLiteDatabase db;
    LinearLayout cart_box;
    Button checkOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_display);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = openOrCreateDatabase("Sweethouse.db", MODE_PRIVATE, null);
        sp = getSharedPreferences(Session.pref, MODE_PRIVATE);

        img = findViewById(R.id.product_img);
        category_display = findViewById(R.id.category_display);
        product_name = findViewById(R.id.product_name);
        product_price = findViewById(R.id.product_price);
        product_quantity = findViewById(R.id.product_quantity);
        product_desc = findViewById(R.id.product_desc);
        back = findViewById(R.id.item_back);
        cart = findViewById(R.id.itemToCart);
        addCart = findViewById(R.id.product_add_cart);
        cart_box = findViewById(R.id.cart_box);
        cart_add = findViewById(R.id.cart_add);
        cart_remove = findViewById(R.id.cart_minus);
        cart_quantity = findViewById(R.id.cart_quantity);


        checkProduct();

        addCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckStock("first");
            }
        });

        cart_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckStock("add");
            }
        });

        cart_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update_item("remove");
            }
        });


        category_display.setText(sp.getString(Session.categoryName, ""));
        Glide.with(product_display.this).load(sp.getString(Session.product_img, "")).into(img);
        product_name.setText(sp.getString(Session.product_name, ""));
        product_price.setText("₹" + sp.getString(Session.product_price, ""));
        product_quantity.setText(sp.getString(Session.product_quantity, ""));
        product_desc.setText(sp.getString(Session.product_desc, ""));

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sp.edit().putString(Session.goProfile, "cart").commit();
                Intent intent = new Intent(product_display.this, MainActivity.class);
                startActivity(intent);
            }
        });

        checkOut = findViewById(R.id.checkOut);

        if (sp.getString(Session.stock_available_display, "").equals("no")){
            checkOut.setText("Out Of Stock");
        }else {
            checkOut.setText("Check Out");
        }

        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sp.getString(Session.stock_available_display, "").equals("no")) {
                    new CommonMethod(product_display.this, "Product is not in Stock");
                }else {
                    sp.edit().putString(Session.quantity, cart_quantity.getText().toString()).commit();
                    sp.edit().putString(Session.product, "single").commit();
                    Intent intent = new Intent(product_display.this, payout_details.class);
                    startActivity(intent);
                }
            }
        });
    }

    public void CheckStock (String condition) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("sub_category_stock");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String id = dataSnapshot.child("item_id").getValue(String.class);
                    if (Objects.equals(sp.getString(Session.product_id, ""), id)) {
                        int stock = Integer.parseInt(dataSnapshot.child("stock_number").getValue(String.class));
                        sp.edit().putString(Session.stock_available, String.valueOf(stock)).commit();

                        if (Objects.equals(condition, "first")){
                            if (stock >= 1) {
                                addItemCart();
                                if (sp.getString(Session.total_amount, "").equals("")) {
                                    sp.edit().putString(Session.total_amount, "0").commit();
                                }
                                int total = Integer.parseInt(sp.getString(Session.total_amount, "")) + Integer.parseInt(sp.getString(Session.product_id, ""));
                                sp.edit().putString(Session.total_amount, String.valueOf(total)).commit();
                                new CommonMethod(product_display.this, "New product added to cart");
                            }
                            else {
                                new CommonMethod(product_display.this, "This item is not in stock");
                            }
                        }
                        else {
                            update_item(condition);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void update_item(String condition) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("cart");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String db_email = dataSnapshot.child("email").getValue(String.class);
                    String db_item_id = dataSnapshot.child("item_id").getValue(String.class);
                    if (Objects.equals(db_email, sp.getString(Session.email, "")) && Objects.equals(db_item_id, sp.getString(Session.product_id, ""))) {
                        int total_quantity;
                        if (Objects.equals(condition, "add")) {
                            total_quantity = (Integer.parseInt(dataSnapshot.child("product_quantity").getValue(String.class)) + 1);
                        }else {
                            total_quantity = (Integer.parseInt(dataSnapshot.child("product_quantity").getValue(String.class)) - 1);
                        }

                        if (total_quantity == 0){
                            delete_item(dataSnapshot.getKey());
                            int displayTotal = Integer.parseInt(sp.getString(Session.total_amount, "")) - Integer.parseInt(sp.getString(Session.product_price, ""));
                            sp.edit().putString(Session.total_amount, String.valueOf(displayTotal)).commit();
                            addCart.setVisibility(View.VISIBLE);
                            cart_box.setVisibility(View.GONE);
                            break;
                        }else {
                            int stock = Integer.parseInt(sp.getString(Session.stock_available, ""));
                            if (stock >= total_quantity) {
                                int total_amount = (Integer.parseInt(sp.getString(Session.product_price, "")) * total_quantity);
                                HashMap<String, String> val = new HashMap<>();
                                val.put("email", sp.getString(Session.email, ""));
                                val.put("item_id", sp.getString(Session.product_id, ""));
                                val.put("total", sp.getString(Session.product_price, ""));
                                val.put("product_quantity", String.valueOf(total_quantity));
                                val.put("total_amount", String.valueOf(total_amount));

                                myRef.child(dataSnapshot.getKey()).setValue(val);
                                cart_quantity.setText(String.valueOf(total_quantity));

                                if (Objects.equals(condition, "add")) {
                                    int displayTotal = Integer.parseInt(sp.getString(Session.total_amount, "")) + Integer.parseInt(sp.getString(Session.product_price, ""));
                                    sp.edit().putString(Session.total_amount, String.valueOf(displayTotal)).commit();
                                } else {
                                    int displayTotal = Integer.parseInt(sp.getString(Session.total_amount, "")) - Integer.parseInt(sp.getString(Session.product_price, ""));
                                    sp.edit().putString(Session.total_amount, String.valueOf(displayTotal)).commit();
                                }
                            }
                            else {
                                new CommonMethod(product_display.this, "No more stock available");
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addItemCart() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("cart");

        HashMap<String, String> val = new HashMap<>();
        val.put("email", sp.getString(Session.email, ""));
        val.put("item_id", sp.getString(Session.product_id,""));
        val.put("product_quantity", "1");
        val.put("total", sp.getString(Session.product_price, ""));
        val.put("total_amount", sp.getString(Session.product_price, ""));

        String key = myRef.push().getKey();

        myRef.child(key).setValue(val).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                addCart.setVisibility(View.GONE);
                cart_box.setVisibility(View.VISIBLE);
                cart_quantity.setText("1");
            }
        });
    }

    public void checkProduct () {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("cart");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String db_email = dataSnapshot.child("email").getValue(String.class);
                    String db_item_id = dataSnapshot.child("item_id").getValue(String.class);
                    if (Objects.equals(db_email, sp.getString(Session.email, "")) && Objects.equals(db_item_id, sp.getString(Session.product_id, ""))) {
                        addCart.setVisibility(View.GONE);
                        cart_box.setVisibility(View.VISIBLE);
                        cart_quantity.setText(dataSnapshot.child("product_quantity").getValue(String.class));
                    }
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
        if (sp.getString(Session.goProfile, "").equals("cart")) {
            Intent intent = new Intent(product_display.this, MainActivity.class);
            startActivity(intent);
        }
        else if (sp.getString(Session.goProfile , "").equals("order")){
            Intent intent = new Intent(product_display.this, order_item.class);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(product_display.this, sub_category.class);
            startActivity(intent);
        }
    }

    public void delete_item (String key) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference removeItem = database.getReference("cart").child(key);
        removeItem.removeValue();
    }
}
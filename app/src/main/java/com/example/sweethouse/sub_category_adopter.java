package com.example.sweethouse;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class sub_category_adopter extends RecyclerView.Adapter<sub_category_adopter.MyHolder> {

    Context context;
    ArrayList<sub_category_list> arrayList;
    SharedPreferences sp;
    Dialog dialog;

    public sub_category_adopter(Context context, ArrayList<sub_category_list> arrayList) {

        this.context = context;
        this.arrayList = arrayList;
        sp = context.getSharedPreferences(Session.pref, Context.MODE_PRIVATE);

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_sub_category, parent, false);
        return new MyHolder(view);
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        ImageView img, cart;
        TextView title, prize, quantity, cart_quantity;
        LinearLayout cart_box;
        ImageView cart_add, cart_remove;
        FrameLayout out_of_stock;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.custom_sub_img);
            title = itemView.findViewById(R.id.custom_sub_title);
            prize = itemView.findViewById(R.id.custom_sub_prize);
            quantity = itemView.findViewById(R.id.custom_sub_quantity);
            cart = itemView.findViewById(R.id.custom_sub_cart);
            cart_box = itemView.findViewById(R.id.cart_box);
            cart_add = itemView.findViewById(R.id.cart_add);
            cart_remove = itemView.findViewById(R.id.cart_minus);
            cart_quantity = itemView.findViewById(R.id.cart_quantity);
            out_of_stock = itemView.findViewById(R.id.out_of_stock);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        CheckInStock(position, holder);
        Glide.with(context).load(arrayList.get(position).getSub_category_img()).into(holder.img);
        holder.title.setText(arrayList.get(position).getSub_category_name());
        holder.prize.setText("₹" + arrayList.get(position).getPrice());
        holder.quantity.setText(arrayList.get(position).getQuantity());
        checkProduct(holder, position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sp.edit().putString(Session.product_id, String.valueOf(arrayList.get(position).getItem_id())).commit();
                sp.edit().putString(Session.product_name, arrayList.get(position).getSub_category_name()).commit();
                sp.edit().putString(Session.product_img, arrayList.get(position).getSub_category_img()).commit();
                sp.edit().putString(Session.product_price, String.valueOf(arrayList.get(position).getPrice())).commit();
                sp.edit().putString(Session.product_quantity, arrayList.get(position).getQuantity()).commit();
                sp.edit().putString(Session.product_desc, arrayList.get(position).getSub_category_desc()).commit();
                sp.edit().putString(Session.stock_available_display, arrayList.get(position).getInStock()).commit();

                Intent intent = new Intent(context, product_display.class);
                context.startActivity(intent);
            }
        });

        holder.cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckStock("first", position, holder);
            }
        });

        holder.cart_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    CheckStock("add", position, holder);
//                    update_item(position, "add", holder);
            }
        });

        holder.cart_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckStock("minus", position, holder);

            }
        });
    }

    private void CheckInStock(int position, sub_category_adopter.MyHolder holder) {
        if (Objects.equals(arrayList.get(position).getInStock(), "no")){
            holder.out_of_stock.setVisibility(View.VISIBLE);
        }
        else {
            holder.out_of_stock.setVisibility(View.GONE);
        }
    }

    public void CheckStock (String condition, int position, sub_category_adopter.MyHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View progress = LayoutInflater.from(context).inflate(R.layout.custom_progress, null);
        builder.setView(progress);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("sub_category_stock");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String id = dataSnapshot.child("item_id").getValue(String.class);
                    if (Objects.equals(arrayList.get(position).getItem_id(), id)) {
                        int stock = Integer.parseInt(dataSnapshot.child("stock_number").getValue(String.class));
                        sp.edit().putString(Session.stock_available, String.valueOf(stock)).commit();

                        if (Objects.equals(condition, "first")){
                            if (stock >= 1) {
                                addItemCart(position, holder);
                                if (sp.getString(Session.total_amount, "").equals("")) {
                                    sp.edit().putString(Session.total_amount, "0").commit();
                                }
                                int total = Integer.parseInt(sp.getString(Session.total_amount, "")) + Integer.parseInt(arrayList.get(position).getPrice());
                                sp.edit().putString(Session.total_amount, String.valueOf(total)).commit();
                                new CommonMethod(context, "New product added to cart");
                                dialog.dismiss();
                            }
                            else {
                                dialog.dismiss();
                                new CommonMethod(context, "This item is not in stock");
                            }
                        }
                        else {
                            update_item(position, condition, holder);
                            dialog.dismiss();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void update_item(int position, String condition, sub_category_adopter.MyHolder holder) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("cart");


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String db_email = dataSnapshot.child("email").getValue(String.class);
                    String db_item_id = dataSnapshot.child("item_id").getValue(String.class);
                    if (Objects.equals(db_email, sp.getString(Session.email, "")) && Objects.equals(db_item_id, arrayList.get(position).getItem_id())) {
                        int total_quantity;
                        if (Objects.equals(condition, "add")) {
                            total_quantity = (Integer.parseInt(dataSnapshot.child("product_quantity").getValue(String.class)) + 1);
                        }else {
                            total_quantity = (Integer.parseInt(dataSnapshot.child("product_quantity").getValue(String.class)) - 1);
                        }
                        int total_amount = (Integer.parseInt(arrayList.get(position).getPrice()) * total_quantity);

                        if (total_quantity == 0){
                            delete_item(dataSnapshot.getKey());
                            int displayTotal = Integer.parseInt(sp.getString(Session.total_amount, "")) - Integer.parseInt(arrayList.get(position).getPrice());
                            sp.edit().putString(Session.total_amount, String.valueOf(displayTotal)).commit();
                            holder.cart.setVisibility(View.VISIBLE);
                            holder.cart_box.setVisibility(View.GONE);
                            break;
                        }else {
                            int stock = Integer.parseInt(sp.getString(Session.stock_available, ""));
                            if (stock >= total_quantity) {
                                HashMap<String, String> val = new HashMap<>();
                                val.put("email", sp.getString(Session.email, ""));
                                val.put("item_id", arrayList.get(position).getItem_id());
                                val.put("total", arrayList.get(position).getPrice());
                                val.put("product_quantity", String.valueOf(total_quantity));
                                val.put("total_amount", String.valueOf(total_amount));

                                myRef.child(dataSnapshot.getKey()).setValue(val);
                                holder.cart_quantity.setText(String.valueOf(total_quantity));

                                if (Objects.equals(condition, "add")) {
                                    int displayTotal = Integer.parseInt(sp.getString(Session.total_amount, "")) + Integer.parseInt(arrayList.get(position).getPrice());
                                    sp.edit().putString(Session.total_amount, String.valueOf(displayTotal)).commit();
                                } else {
                                    int displayTotal = Integer.parseInt(sp.getString(Session.total_amount, "")) - Integer.parseInt(arrayList.get(position).getPrice());
                                    sp.edit().putString(Session.total_amount, String.valueOf(displayTotal)).commit();
                                }
                            }
                            else {
                                new CommonMethod(context, "No more stock available");
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

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void addItemCart(int position, MyHolder holder) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("cart");

        HashMap<String, String> val = new HashMap<>();
        val.put("email", sp.getString(Session.email, ""));
        val.put("item_id", arrayList.get(position).getItem_id());
        val.put("product_quantity", "1");
        val.put("total", arrayList.get(position).getPrice());
        val.put("total_amount", arrayList.get(position).getPrice());

        String key = myRef.push().getKey();

        myRef.child(key).setValue(val).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                holder.cart.setVisibility(View.GONE);
                holder.cart_box.setVisibility(View.VISIBLE);
                holder.cart_quantity.setText("1");
            }
        });
    }

    public void checkProduct (MyHolder holder, int position) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("cart");
        Query check = myRef.orderByChild("email").equalTo(sp.getString(Session.email, ""));

        check.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String db_item_id = dataSnapshot.child("item_id").getValue(String.class);
                    if (Objects.equals(db_item_id, arrayList.get(position).getItem_id())){
                        holder.cart.setVisibility(View.GONE);
                        holder.cart_box.setVisibility(View.VISIBLE);
                        holder.cart_quantity.setText(dataSnapshot.child("product_quantity").getValue(String.class));
                        break;
                    }
                    else {
                        holder.cart.setVisibility(View.VISIBLE);
                        holder.cart_box.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void delete_item (String key) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference removeItem = database.getReference("cart").child(key);
        removeItem.removeValue();
    }
}
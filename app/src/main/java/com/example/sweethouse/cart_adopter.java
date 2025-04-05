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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class cart_adopter extends RecyclerView.Adapter<cart_adopter.MyHolder> {

    FragmentActivity activity;
    ArrayList<cart_list> arrayList;
    SharedPreferences sp;
    Dialog dialog;

    public cart_adopter(FragmentActivity activity, ArrayList<cart_list> arrayList, SharedPreferences sp) {

        this.activity = activity;
        this.arrayList = arrayList;
        this.sp = sp;

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_cart, parent, false);
        return new MyHolder(view);
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        ImageView img, add, remove;
        TextView title, price, quantity, cart_quantity;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.custom_cart_img);
            title = itemView.findViewById(R.id.custom_cart_title);
            price = itemView.findViewById(R.id.custom_cart_prize);
            quantity = itemView.findViewById(R.id.custom_cart_quantity);
            cart_quantity = itemView.findViewById(R.id.cart_quantity);
            add = itemView.findViewById(R.id.cart_add);
            remove = itemView.findViewById(R.id.cart_minus);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        check_already_added_product(position);
        Display_quantity(holder, position);
        select_product(holder, Integer.parseInt(arrayList.get(position).getItem_id()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireSubCategory(Integer.parseInt(arrayList.get(position).getItem_id()));
                sp.edit().putString(Session.goProfile, "cart").commit();
                Intent intent = new Intent(activity,product_display.class);
                activity.startActivity(intent);
            }
        });

        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckStock("add", position, holder);
            }
        });

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckStock("remove", position, holder);
            }
        });
    }

    private void Display_quantity(MyHolder holder, int position) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("cart");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String db_email = dataSnapshot.child("email").getValue(String.class);
                    String db_item_id = dataSnapshot.child("item_id").getValue(String.class);
                    if (Objects.equals(db_email, sp.getString(Session.email, "")) && Objects.equals(db_item_id, arrayList.get(position).getItem_id())) {
                        holder.cart_quantity.setText(dataSnapshot.child("product_quantity").getValue(String.class));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fireSubCategory(int id) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("sub_category");


        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String db_id = dataSnapshot.child("item_id").getValue(String.class);
                    if (Objects.equals(db_id, String.valueOf(id))) {
                        sp.edit().putString(Session.product_id, dataSnapshot.child("item_id").getValue(String.class)).commit();
                        sp.edit().putString(Session.product_name, dataSnapshot.child("sub_category_name").getValue(String.class)).commit();
                        sp.edit().putString(Session.product_img, dataSnapshot.child("sub_category_img").getValue(String.class)).commit();
                        sp.edit().putString(Session.product_price, dataSnapshot.child("price").getValue(String.class)).commit();
                        sp.edit().putString(Session.product_quantity, dataSnapshot.child("quantity").getValue(String.class)).commit();
                        sp.edit().putString(Session.product_desc, dataSnapshot.child("sub_category_desc").getValue(String.class)).commit();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void CheckStock (String condition, int position, cart_adopter.MyHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View progress = LayoutInflater.from(activity).inflate(R.layout.custom_progress, null);
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

                        update_item(position, condition, holder);
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void update_item (int position, String condition, MyHolder holder) {
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
                        int total_amount = Integer.parseInt(arrayList.get(position).getTotal()) * total_quantity;

                        if (total_quantity == 0){
                            delete_item(dataSnapshot.getKey());
                            int displayTotal = Integer.parseInt(sp.getString(Session.total_amount, "")) - Integer.parseInt(arrayList.get(position).getTotal());
                            sp.edit().putString(Session.total_amount, String.valueOf(displayTotal)).commit();
                            CartFragment.total_amount.setText("₹"+String.valueOf(displayTotal));
                            arrayList.remove(position);
                            notifyDataSetChanged();
                            break;
                        }else {
                            int stock = Integer.parseInt(sp.getString(Session.stock_available, ""));
                            if (stock >= total_quantity) {
                                HashMap<String, String> val = new HashMap<>();
                                val.put("email", sp.getString(Session.email, ""));
                                val.put("item_id", arrayList.get(position).getItem_id());
                                val.put("total", arrayList.get(position).getTotal());
                                val.put("product_quantity", String.valueOf(total_quantity));
                                val.put("total_amount", String.valueOf(total_amount));

                                myRef.child(dataSnapshot.getKey()).setValue(val);
                                holder.cart_quantity.setText(String.valueOf(total_quantity));

                                if (Objects.equals(condition, "add")) {
                                    int displayTotal = Integer.parseInt(sp.getString(Session.total_amount, "")) + Integer.parseInt(arrayList.get(position).getTotal());
                                    sp.edit().putString(Session.total_amount, String.valueOf(displayTotal)).commit();
                                    CartFragment.total_amount.setText("₹" + String.valueOf(displayTotal));
                                } else {
                                    int displayTotal = Integer.parseInt(sp.getString(Session.total_amount, "")) - Integer.parseInt(arrayList.get(position).getTotal());
                                    sp.edit().putString(Session.total_amount, String.valueOf(displayTotal)).commit();
                                    CartFragment.total_amount.setText("₹" + String.valueOf(displayTotal));
                                }
                            }
                            else {
                                new CommonMethod(activity, "No more stock available");
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


    private void select_product(MyHolder holder, int id) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("sub_category");


        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String db_id = dataSnapshot.child("item_id").getValue(String.class);
                    if (Objects.equals(db_id, String.valueOf(id))) {
                        Glide.with(activity).load(dataSnapshot.child("sub_category_img").getValue(String.class)).into(holder.img);
                        holder.title.setText(dataSnapshot.child("sub_category_name").getValue(String.class));
                        holder.price.setText("₹" + dataSnapshot.child("price").getValue(String.class));
                        holder.quantity.setText(dataSnapshot.child("quantity").getValue(String.class));
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

    public void check_already_added_product (int position) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("sub_category_stock");
        Query check = myRef.orderByChild("item_id").equalTo(arrayList.get(position).getItem_id());

        check.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if (Integer.parseInt(dataSnapshot.child("stock_number").getValue(String.class)) >= Integer.parseInt(arrayList.get(position).getProduct_quantity())){
//                        new CommonMethod(activity, dataSnapshot.child("item_id").getValue(String.class));
                    }
                    else {
//                        new CommonMethod(activity, arrayList.get(position).getKey());
                        delete_item(arrayList.get(position).getKey());

                        sp.edit().putString(Session.goProfile, "cart").commit();
                        int displayTotal = Integer.parseInt(sp.getString(Session.total_amount, "")) - Integer.parseInt(arrayList.get(position).getTotal_amount());
                        sp.edit().putString(Session.total_amount, String.valueOf(displayTotal)).commit();
                        CartFragment.total_amount.setText("₹"+String.valueOf(displayTotal));
                        Intent i = new Intent(activity, MainActivity.class);
                        activity.startActivity(i);
//                        arrayList.remove(position);
//                        notifyDataSetChanged();
//                        break;
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
}

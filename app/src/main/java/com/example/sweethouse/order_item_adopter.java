package com.example.sweethouse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class order_item_adopter extends RecyclerView.Adapter<order_item_adopter.MyHolder> {

    Context context;
    ArrayList<itemList> arrayList;
    SharedPreferences sp;

    public order_item_adopter(Context context, ArrayList<itemList> array) {

        this.context = context;
        this.arrayList = array;
        sp = context.getSharedPreferences(Session.pref, Context.MODE_PRIVATE);

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_order_item, parent, false);
        return new MyHolder(view);
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        ImageView img;
        TextView title, price, quantity, total_quantity_order;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.custom_img);
            title = itemView.findViewById(R.id.custom_title);
            price = itemView.findViewById(R.id.custom_prize);
            quantity = itemView.findViewById(R.id.custom_quantity);
            total_quantity_order = itemView.findViewById(R.id.total_order_quantity);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
//        holder.title.setText(arrayList.get(position).getItem_id());
        holder.total_quantity_order.setText(arrayList.get(position).getQuantity() + "*");
        holder.price.setText(arrayList.get(position).getAmount());
        select_product(holder, Integer.parseInt(arrayList.get(position).getItem_id()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storeDetails(arrayList.get(position).getItem_id());
            }
        });
    }

    private void storeDetails(String itemId) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("sub_category");

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String db_id = dataSnapshot.child("item_id").getValue(String.class);
                    if (Objects.equals(db_id, itemId)) {
                        sp.edit().putString(Session.product_id, dataSnapshot.child("item_id").getValue(String.class)).commit();
                        sp.edit().putString(Session.product_name, dataSnapshot.child("sub_category_name").getValue(String.class)).commit();
                        sp.edit().putString(Session.product_img, dataSnapshot.child("sub_category_img").getValue(String.class)).commit();
                        sp.edit().putString(Session.product_price, dataSnapshot.child("price").getValue(String.class)).commit();
                        sp.edit().putString(Session.product_quantity, dataSnapshot.child("quantity").getValue(String.class)).commit();
                        sp.edit().putString(Session.product_desc, dataSnapshot.child("sub_category_desc").getValue(String.class)).commit();
                        sp.edit().putString(Session.goProfile, "order").commit();

                        Intent intent = new Intent(context, product_display.class);
                        context.startActivity(intent);
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

    private void select_product(order_item_adopter.MyHolder holder, int id) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("sub_category");


        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String db_id = dataSnapshot.child("item_id").getValue(String.class);
                    if (Objects.equals(db_id, String.valueOf(id))) {
                        Glide.with(context).load(dataSnapshot.child("sub_category_img").getValue(String.class)).into(holder.img);
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
}

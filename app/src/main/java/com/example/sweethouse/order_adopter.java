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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class order_adopter extends RecyclerView.Adapter<order_adopter.MyHolder> {

    Context context;
    ArrayList<orderList> arrayList;
    SharedPreferences sp;

    public order_adopter(Context context, ArrayList<orderList> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
        sp = context.getSharedPreferences(Session.pref, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_order, parent, false);
        return new MyHolder(view);
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        TextView order, address, payment_type, amount, time, status, date;
        ImageView status_img;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            order = itemView.findViewById(R.id.order);
            address = itemView.findViewById(R.id.address);
            payment_type = itemView.findViewById(R.id.payment_type);
            amount = itemView.findViewById(R.id.amount);
            time = itemView.findViewById(R.id.time);
            date = itemView.findViewById(R.id.date);
            status = itemView.findViewById(R.id.status_text);
            status_img = itemView.findViewById(R.id.status_img);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        holder.order.setText(arrayList.get(position).getOrder_id());
        holder.address.setText("Address: "+arrayList.get(position).getAddress());
        holder.payment_type.setText(arrayList.get(position).getPayment_type());
        holder.amount.setText("₹"+arrayList.get(position).getAmount());
        holder.time.setText(arrayList.get(position).getTime());
        holder.date.setText(arrayList.get(position).getDate());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sp.edit().putString(Session.order_id, arrayList.get(position).getOrder_id()).commit();
                sp.edit().putString(Session.transaction_id, arrayList.get(position).getTransation_id()).commit();
                sp.edit().putString(Session.address, arrayList.get(position).getAddress()).commit();
                sp.edit().putString(Session.payment_type, arrayList.get(position).getPayment_type()).commit();
                sp.edit().putString(Session.amount, arrayList.get(position).getAmount()).commit();
                sp.edit().putString(Session.time, arrayList.get(position).getTime()).commit();
                sp.edit().putString(Session.status_text, arrayList.get(position).getStatus()).commit();
                sp.edit().putString(Session.date, arrayList.get(position).getDate()).commit();

                Intent intent = new Intent(context, order_item.class);
                context.startActivity(intent);
            }
        });

        if (Objects.equals(arrayList.get(position).getStatus(), "pending")){
            holder.status.setText(arrayList.get(position).getStatus());
            holder.status_img.setImageResource(R.drawable.pending);
        }
        else if (Objects.equals(arrayList.get(position).getStatus(), "success")){
            holder.status.setText(arrayList.get(position).getStatus());
            holder.status_img.setImageResource(R.drawable.success);
        }
        else {
            holder.status.setText(arrayList.get(position).getStatus());
            holder.status_img.setImageResource(R.drawable.fail);
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}

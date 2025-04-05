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
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class Fragment_category_adopter extends RecyclerView.Adapter<Fragment_category_adopter.MyHolder> {

    FragmentActivity activity;
    ArrayList<category_list> arrayList;
    SharedPreferences sp;

    public Fragment_category_adopter(FragmentActivity activity, ArrayList<category_list> arrayList) {

        this.activity = activity;
        this.arrayList = arrayList;
        sp = activity.getSharedPreferences(Session.pref, Context.MODE_PRIVATE);

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_fragment_category, parent, false);
        return new MyHolder(view);
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        ImageView img;
        TextView title;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.custom_fragment_img);
            title = itemView.findViewById(R.id.custom_fragment_title);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Glide.with(activity).load(arrayList.get(position).getCategory_img()).into(holder.img);
        holder.title.setText(arrayList.get(position).getCategory_name());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sp.edit().putString(Session.categoryId, String.valueOf(arrayList.get(position).getCategory_id())).commit();
                sp.edit().putString(Session.categoryName, arrayList.get(position).getCategory_name()).commit();

                Intent intent = new Intent(activity, sub_category.class);
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}

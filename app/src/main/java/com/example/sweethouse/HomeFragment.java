package com.example.sweethouse;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    SharedPreferences sp;
    ImageView profile_img;
    TextView title;
    ArrayList<category_list> arrayList;
    RecyclerView category_recycle;
    EditText search;
    AlertDialog dialog;

    //    database
    SQLiteDatabase db;

//    int [] category_id = {1,2,3,4,5,6,7,8,9,10};
//    int [] category_img = {R.drawable.category_tub, R.drawable.category_stick, R.drawable.category_share_pack, R.drawable.category_sandwich, R.drawable.category_cone, R.drawable.category_indian_sweet, R.drawable.category_chocolate,R.drawable.category_dark, R.drawable.category_cookie_cake, R.drawable.category_candies};
//    String [] category_name = {"Tub IceCream", "Stick IceCream", "Share pack", "Sandwich IceCream", "Cone IceCream", "Indian Sweet", "Chocolate", "Dark Chocolate", "Cookie & Cake", "Candies"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

//        connect sp
        sp = getActivity().getSharedPreferences(Session.pref, Context.MODE_PRIVATE);
//        connect database
        db = getActivity().openOrCreateDatabase("Sweethouse.db", Context.MODE_PRIVATE, null);

//        set title name
        title = view.findViewById(R.id.home_title);
        title.setText("Hello, "+ sp.getString(Session.name, ""));

//        profile intent by img
        profile_img = view.findViewById(R.id.profile_img);

        profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sp.edit().putString(Session.goProfile, "profile").commit();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });

        search = view.findViewById(R.id.homeSearch);
        search.setEnabled(false);

//        category display
        category_recycle = view.findViewById(R.id.category_recycle);
        category_recycle.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL));
        category_recycle.setItemAnimator(new DefaultItemAnimator());

        arrayList = new ArrayList<>();
//        select category from firebase

        fireSelectCategory();

        return view;
    }

    private void fireSelectCategory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View progress = LayoutInflater.from(getActivity()).inflate(R.layout.custom_progress, null);
        builder.setView(progress);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("category");

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    category_list values = dataSnapshot.getValue(category_list.class);
                    arrayList.add(values);
                }
                category_adopter adopter = new category_adopter(getActivity(), arrayList, sp);
                dialog.dismiss();
                category_recycle.setAdapter(adopter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
package com.example.sweethouse;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class CartFragment extends Fragment {

    public CartFragment() {
        // Required empty public constructor
    }
    public static TextView total_amount;
    RecyclerView recycle;
    SharedPreferences sp;
    Button check_out;
    SQLiteDatabase db;
    int total;
    public static ArrayList<cart_list> arrayList;
    AlertDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        sp = getActivity().getSharedPreferences(Session.pref, Context.MODE_PRIVATE);
        db = getActivity().openOrCreateDatabase("Sweethouse.db", Context.MODE_PRIVATE, null);

//        sp.edit().putString(Session.total_amount, "0").commit();

        total_amount = view.findViewById(R.id.cart_total_amount);
        total_amount.setText("₹" +sp.getString(Session.total_amount, ""));
        recycle = view.findViewById(R.id.cart_item_area);
        check_out = view.findViewById(R.id.cart_check_out);
        recycle.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL));
        recycle.setItemAnimator(new DefaultItemAnimator());
        arrayList = new ArrayList<cart_list>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View progress = LayoutInflater.from(getActivity()).inflate(R.layout.custom_progress, null);
        builder.setView(progress);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("cart");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String db_email = dataSnapshot.child("email").getValue(String.class);
                        if (Objects.equals(db_email, sp.getString(Session.email, ""))) {
                            cart_list list = dataSnapshot.getValue(cart_list.class);
                            list.key = dataSnapshot.getKey();
                            arrayList.add(list);
                        }
                        dialog.dismiss();
                        cart_adopter adopter = new cart_adopter(getActivity(), arrayList, sp);
                        recycle.setAdapter(adopter);
                    }
                }else {
                    dialog.dismiss();
                    new CommonMethod(getActivity(), "Add item First");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        check_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (arrayList.isEmpty()) {
                    new CommonMethod(getActivity(), "Select item first");
                }
                else {
                    Intent intent = new Intent(getActivity(), payout_details.class);
                    startActivity(intent);
                }
            }
        });

        if (sp.getString(Session.goProfile, "").equals("cart")){
            sp.edit().putString(Session.goProfile, "no").commit();
        }

        return view;
    }

    public void calAmount () {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("cart");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String db_email = dataSnapshot.child("email").getValue(String.class);
                        if (Objects.equals(db_email, sp.getString(Session.email, ""))) {
                            total += Integer.parseInt(dataSnapshot.child("total_amount").getValue(String.class));
                            total_amount.setText("₹" + String.valueOf(total));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
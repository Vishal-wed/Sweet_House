package com.example.sweethouse;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import java.util.Locale;

public class CategoryFragment extends Fragment {

    public CategoryFragment() {
        // Required empty public constructor
    }

    SharedPreferences sp;
    SQLiteDatabase db;
    ArrayList<category_list> arrayList;
    AlertDialog dialog;
    RecyclerView recycler_area;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

//        connect db
        db = getContext().openOrCreateDatabase("Sweethouse.db", Context.MODE_PRIVATE, null);

//        connect sp
        sp = getActivity().getSharedPreferences(Session.pref, Context.MODE_PRIVATE);

        recycler_area = view.findViewById(R.id.category_fragment_area);

        recycler_area.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL));
        recycler_area.setItemAnimator(new DefaultItemAnimator());

        arrayList = new ArrayList<>();

//        select category
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
                Fragment_category_adopter adopter = new Fragment_category_adopter(getActivity(), arrayList);
                dialog.dismiss();
                recycler_area.setAdapter(adopter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
package com.example.sweethouse;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class sub_category extends AppCompatActivity {

    TextView title;
    ImageView goToCart;
    SharedPreferences sp;
    RecyclerView recycler;
    static RecyclerView not_recycle;
    static LinearLayout soon;
    ArrayList<sub_category_list> arrayList;
    AlertDialog dialog;

   @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       EdgeToEdge.enable(this);
       setContentView(R.layout.activity_sub_category);
       ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
           Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
           v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
           return insets;
       });

//        connect sp
       sp = getSharedPreferences(Session.pref, MODE_PRIVATE);

       soon = findViewById(R.id.soon);
       title = findViewById(R.id.sub_categiry_title);
       title.setText(sp.getString(Session.categoryName, ""));

       goToCart = findViewById(R.id.goToCart);
       goToCart.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               sp.edit().putString(Session.goProfile, "cart").commit();
               Intent intent = new Intent(sub_category.this, MainActivity.class);
               startActivity(intent);
           }
       });

//        recycle view
       recycler = findViewById(R.id.sub_category_recycle);
       not_recycle = findViewById(R.id.not_recycle);

       recycler.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
       recycler.setItemAnimator(new DefaultItemAnimator());

       not_recycle.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
       not_recycle.setItemAnimator(new DefaultItemAnimator());

       arrayList = new ArrayList<>();

       fireSubCategory();
   }

    private void fireSubCategory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(sub_category.this);
        View progress = LayoutInflater.from(sub_category.this).inflate(R.layout.custom_progress, null);
        builder.setView(progress);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("sub_category");


        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String db_password = dataSnapshot.child("category_id").getValue(String.class);
                    if (Objects.equals(db_password, sp.getString(Session.categoryId, ""))) {

                        DatabaseReference database1 = FirebaseDatabase.getInstance().getReference("sub_category_stock");
                        database1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot1 : snapshot.getChildren()){
                                    String id = dataSnapshot1.child("item_id").getValue(String.class);
                                    if (Objects.equals(id, dataSnapshot.child("item_id").getValue(String.class))){
                                        String stock = dataSnapshot1.child("stock_number").getValue(String.class);
                                        if (Objects.equals(stock, "0")){
                                            sub_category_list val = dataSnapshot.getValue(sub_category_list.class);
                                            val.InStock = "no";
                                            arrayList.add(val);
                                        }
                                        else {
                                            sub_category_list values = dataSnapshot.getValue(sub_category_list.class);
                                            values.InStock = "yes";
                                            arrayList.add(values);
                                        }
                                    }
                                }
                                if (!arrayList.isEmpty()){
                                    sub_category_adopter adopter = new sub_category_adopter(sub_category.this, arrayList);
                                    dialog.dismiss();
                                    recycler.setAdapter(adopter);
                                }else {
                                    soonDisplay();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void soonDisplay() {
       soon.setVisibility(View.VISIBLE);
       dialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(sub_category.this, MainActivity.class);
        startActivity(intent);
    }
}
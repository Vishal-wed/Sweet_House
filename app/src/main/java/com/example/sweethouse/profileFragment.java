package com.example.sweethouse;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class profileFragment extends Fragment {

    public profileFragment() {
        // Required empty public constructor
    }

    Button edit;
    LinearLayout logout, delete, order, feedback;
    TextView name,email;

//    sp
    SharedPreferences sp;
    SQLiteDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

//        connect sp
        sp = getActivity().getSharedPreferences(Session.pref, Context.MODE_PRIVATE);
        db = getActivity().openOrCreateDatabase("Sweethouse.db", Context.MODE_PRIVATE, null);

        name = view.findViewById(R.id.profile_name);
        email = view.findViewById(R.id.profile_email);
        feedback = view.findViewById(R.id.feedback);
        name.setText(sp.getString(Session.name, ""));
        email.setText(sp.getString(Session.email, ""));

        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), user_feedback.class);
                startActivity(intent);
            }
        });

        delete = view.findViewById(R.id.delete);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setTitle("Logout Account");
                builder.setMessage("You want to Logout !!");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("details").child(sp.getString(Session.number, ""));

                        myRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                new CommonMethod(getActivity(), "Account Delete");
                                Intent intent = new Intent(getActivity(), sign_in.class);
                                startActivity(intent);
                            }
                        });
                    }
                });

                builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }
        });

        logout = view.findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setTitle("Logout Account");
                builder.setMessage("You want to Logout !!");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sp.edit().clear().commit();
                        Intent intent = new Intent(getActivity(), sign_in.class);
                        startActivity(intent);
                    }
                });

                builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }
        });

        order = view.findViewById(R.id.order);

        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), orderHistory.class);
                startActivity(intent);
            }
        });

//        condition of goProfile
        if (sp.getString(Session.goProfile, "").equals("profile")){
            sp.edit().putString(Session.goProfile, "no").commit();
        }

        edit = view.findViewById(R.id.profile_edit);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), editProfile.class);
                getActivity().startActivity(intent);
            }
        });

        return view;
    }
}
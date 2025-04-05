package com.example.sweethouse;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import java.util.Objects;

public class CommonMethod {

    CommonMethod (Context context, String mess){
        Toast.makeText(context, mess, Toast.LENGTH_SHORT).show();
    }
}

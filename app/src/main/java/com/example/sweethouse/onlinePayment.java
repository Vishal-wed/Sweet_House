package com.example.sweethouse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Text;

public class onlinePayment extends AppCompatActivity {

    LinearLayout upi, card, upiClick, clickCard;
    ImageView upi_img;
    TextView upi_text;
    Button card_button, upi_button;
    EditText user_upi, card_number, card_name, card_expiry;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_online_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sp = getSharedPreferences(Session.pref, MODE_PRIVATE);

        upi = findViewById(R.id.OnlineUPI);
        card = findViewById(R.id.OnlineCard);
        upi_img = findViewById(R.id.upi_img);
        upi_text = findViewById(R.id.upi_text);
        upiClick = findViewById(R.id.clickUPI);
        clickCard = findViewById(R.id.clickCard);
        card_name = findViewById(R.id.card_name);
        card_number = findViewById(R.id.card_number);
        card_expiry = findViewById(R.id.card_expiry);
        card_button = findViewById(R.id.card_button);
        upi_button = findViewById(R.id.upi_button);
        user_upi = findViewById(R.id.user_upi);

        upi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upiClick.setVisibility(View.VISIBLE);
                clickCard.setVisibility(View.GONE);
            }
        });

        upi_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user_upi.getText().toString().trim().equals("")){
                    user_upi.setError("UPI required");
                }
                else if (user_upi.getText().toString().trim().equals("try@okBank")){
                    sp.edit().putString(Session.payment, "yes").commit();
                    new CommonMethod(onlinePayment.this, "Payment Successfully");
                    payout_details.order_payment_done();
                    onBackPressed();
                }
                else {
                    new CommonMethod(onlinePayment.this, "Invalid UPI");
                }
            }
        });

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upiClick.setVisibility(View.GONE);
                clickCard.setVisibility(View.VISIBLE);
            }
        });

        card_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (card_number.getText().toString().trim().equals("")){
                    card_number.setError("Number required");
                }
                else if (!(card_number.getText().toString().trim().length() == 16)){
                    card_number.setError("Invalid number");
                }
                else if (card_name.getText().toString().trim().equals("")){
                    card_name.setError("Name required");
                }
                else if (card_expiry.getText().toString().trim().equals("")){
                    card_expiry.setError("Expiry Date required");
                }
                else if (card_number.getText().toString().equals("0000000000000000") && card_name.getText().toString().equals("test") && card_expiry.getText().toString().equals("10-24")){
                    sp.edit().putString(Session.payment, "yes").commit();
                    new CommonMethod(onlinePayment.this, "Payment Successfully");
                }
                else {
                    new CommonMethod(onlinePayment.this, "Invalid card details");
                }
            }
        });
    }
}
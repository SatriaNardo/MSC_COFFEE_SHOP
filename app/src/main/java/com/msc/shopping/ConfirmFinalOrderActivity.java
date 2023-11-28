package com.msc.shopping;

import android.content.Intent;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.msc.shopping.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConfirmFinalOrderActivity extends AppCompatActivity {
    private EditText nameEditText,phoneEditText,addressEditText,cityEditText;
    private Button confirmOrderBtn;
    private String totalAmount = "";
    private int i = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_final_order);

        totalAmount = getIntent().getStringExtra("Total Price");
        Toast.makeText(this, "Total Price = Rp. "+totalAmount,Toast.LENGTH_SHORT).show();
        confirmOrderBtn = (Button) findViewById(R.id.confirm_final_order_btn);
        nameEditText =(EditText) findViewById(R.id.shippment_name);
        phoneEditText =(EditText) findViewById(R.id.shippment_phone_number);
        addressEditText =(EditText) findViewById(R.id.shippment_address);
        confirmOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Check();
            }
        });
    }

    private void Check() {
        if (TextUtils.isEmpty(nameEditText.getText().toString())) {
            Toast.makeText(this, "Please Provide Your Full Name", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(phoneEditText.getText().toString())) {
            Toast.makeText(this, "Please Provide Your Phone Number", Toast.LENGTH_SHORT).show();
        } else {
            if (TextUtils.isEmpty(addressEditText.getText().toString())) {
                Toast.makeText(this, "Your order will be serve in the cafe", Toast.LENGTH_SHORT).show();

            }
            ConfirmOrder();
}
    }

    private void ConfirmOrder() {
        final String saveCurrentDate;
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        saveCurrentDate = currentDate.format(calForDate.getTime());
        //SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        //saveCurrentTime = currentDate.format(calForDate.getTime());
        final DatabaseReference ordersRef= FirebaseDatabase.getInstance().getReference()
                .child("Order")
                .child(Prevalent.currentOnlineUser.getPhone());
        final DatabaseReference ordersKeep = FirebaseDatabase.getInstance().getReference();
        ordersKeep.addListenerForSingleValueEvent(new ValueEventListener() {
                                                   @Override
                                                   public void onDataChange(DataSnapshot dataSnapshot) {
                                                       for(i = 0;i<31;i++)
                                                       {
                                                           String s = String.valueOf(i);
                                                       if(!(dataSnapshot.child("Orders").child(Prevalent.currentOnlineUser.getPhone()).child(s).exists())) {
                                                           HashMap<String, Object> ordersMaps = new HashMap<>();
                                                           ordersMaps.put("totalAmount", totalAmount);
                                                           ordersMaps.put("name", nameEditText.getText().toString());
                                                           ordersMaps.put("phone", phoneEditText.getText().toString());
                                                           if (TextUtils.isEmpty(addressEditText.getText().toString())) {
                                                               ordersMaps.put("address", "Served in Cafe");
                                                           } else {
                                                               ordersMaps.put("address", addressEditText.getText().toString());
                                                           }
                                                           ordersMaps.put("date", saveCurrentDate);
                                                           ordersMaps.put("totalAmount", totalAmount);
                                                           ordersKeep.child("Orders").child(Prevalent.currentOnlineUser.getPhone()).child(s).updateChildren(ordersMaps);
                                                           break;
                                                       }
                                                       }

                                                   }
                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                               });
        HashMap<String, Object> ordersMap = new HashMap<>();
        ordersMap.put("totalAmount", totalAmount);
        ordersMap.put("name",nameEditText.getText().toString());
        ordersMap.put("phone",phoneEditText.getText().toString());
        if (TextUtils.isEmpty(addressEditText.getText().toString())) {
            ordersMap.put("address","Served in Cafe");
        }
        else {
            ordersMap.put("address",addressEditText.getText().toString());
        }
        ordersMap.put("date",saveCurrentDate);
        if (TextUtils.isEmpty(addressEditText.getText().toString())) {
            ordersMap.put("state","Not Served");
        }
        else {
            ordersMap.put("state", "Not Shipped");
        }
        ordersRef.updateChildren(ordersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    FirebaseDatabase.getInstance().getReference()
                            .child("Cart List")
                            .child("User view")
                            .child(Prevalent.currentOnlineUser.getPhone())
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(ConfirmFinalOrderActivity.this,"Your final Order has been placed successfully.",Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(ConfirmFinalOrderActivity.this,HomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                }
            }
        });


    }
}

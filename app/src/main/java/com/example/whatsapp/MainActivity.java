package com.example.whatsapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {


    private androidx.appcompat.widget.Toolbar mToolbar;
    private Toolbar supportActionBar;

    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private  TabsAccessorAdapter myTabsAccessorAdapter;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
private  DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        RootRef= FirebaseDatabase.getInstance().getReference();



        mToolbar=findViewById(R.id.maon_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Messaging Spire");
        myViewPager= this.<ViewPager>findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter=new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout=this.<TabLayout> findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.options_menu,menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser==null){
            SendUserToLoginActivity();
        }
        else{
            VerifyUserExistence();
        }
    }

    private void VerifyUserExistence()
    {
        String currentuserID=mAuth.getUid();
        RootRef.child("Users").child(currentuserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if((dataSnapshot.child("name").exists())){
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else{
                    SendUserToSettingsActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToLoginActivity() {
        Intent loginIntent=new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_option){
            mAuth.signOut();
            SendUserToLoginActivity();

        }
        if(item.getItemId()==R.id.main_settings_option){
SendUserToSettingsActivity();
        }
        if(item.getItemId()==R.id.main_create_group_option){
            RequestNewGroup();

        }
        if(item.getItemId()==R.id.main_find_friends_option){
            SendUserToFindFriendsActivity();
        }

        return true;
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name:");
        final EditText groupNameField= new EditText(MainActivity.this);
        groupNameField.setHint("e.g Alpha Coders");
        builder.setView(groupNameField);
        builder.setPositiveButton("create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String groupName=groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this,"Please write group name",Toast.LENGTH_SHORT).show();
                }
                else{
                    createNewGroup(groupName);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }
    private  void  createNewGroup(final String groupName){
        RootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this,groupName+"group is created successfully...",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void SendUserToSettingsActivity() {
        Intent settingIntent=new Intent(MainActivity.this, SettingsActivity.class);

        startActivity(settingIntent);

    }

    private void SendUserToFindFriendsActivity() {
        Intent findfriendsIntent=new Intent(MainActivity.this, FindFriendsActivity.class);

        startActivity(findfriendsIntent);

    }

}
package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private StorageReference UserProfileImageRef;

    private static final int GalleryPick=1;
    private ProgressDialog loadingbar;
    private String photoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("Proffile Images");
        
        
        InitializeFields();
        


        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSettings();
            }


        });

        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GalleryPick);
            }
        });

    }

    private void InitializeFields() {
        UpdateAccountSettings=findViewById(R.id.update_settings_button);
        userName=findViewById(R.id.set_user_name);
        userStatus=findViewById(R.id.set_profile_status);
        userProfileImage=findViewById(R.id.set_profile_image);
        loadingbar =new ProgressDialog(this);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            Uri ImageUri= data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){

                loadingbar.setTitle("Set Profile Image");
                loadingbar.setMessage("Please wait, profile image is updating...");
                loadingbar.setCanceledOnTouchOutside(false);
                loadingbar.show();

                Uri resultUri=result.getUri();

                StorageReference filePath=UserProfileImageRef.child(currentUserID+".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Profile Image uploaded Successfully...", Toast.LENGTH_SHORT).show();
                            final String downloaedUrl = task.getResult().getStorage().getDownloadUrl().toString();
                            RootRef.child("Users").child(currentUserID).child("image").setValue(downloaedUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(SettingsActivity.this, "Image saved in database, Successfully...", Toast.LENGTH_SHORT).show();
                                          loadingbar.dismiss();
                                    }
                                    else{
                                        String mesaage=task.getException().toString();
                                        Toast.makeText(SettingsActivity.this, "Error: "+mesaage, Toast.LENGTH_SHORT).show();
                                        loadingbar.dismiss();
                                    }
                                }
                            });

                        }
                        else{
                            String message=task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error"+ message, Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();
                        }
                    }
                });
            }
        }

    }




    private void UpdateSettings() {
        String setUserName=userName.getText().toString();
        String setStatus=userStatus.getText().toString();
        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this,"Please enter User Name...",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus)){
            Toast.makeText(this,"Please enter User Status...",Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String,String> profileMap=new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);
            profileMap.put("image",photoUrl);

            RootRef.child("Users").child(currentUserID).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this,"Profile Updated Successfully",Toast.LENGTH_SHORT).show();



                    }
                    else{
                        String message=task.getException().toString();
                        Toast.makeText(SettingsActivity.this,"Error:"+message,Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }


    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.exists())&& (dataSnapshot.hasChild("name")&&(dataSnapshot.hasChild("image")))){

                    String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus=dataSnapshot.child("status").getValue().toString();
                    String retrieveProfileImage=dataSnapshot.child("image").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);
                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                    photoUrl=retrieveProfileImage;


                }
                else if((dataSnapshot.exists())&& (dataSnapshot.hasChild("name")))
                {
                    String retrieveUserName=dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus=dataSnapshot.child("status").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);


                }
                else{
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingsActivity.this,"Please set and update your profile information..",Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

   private void SendUserToMainActivity() {
        Intent mainIntent=new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
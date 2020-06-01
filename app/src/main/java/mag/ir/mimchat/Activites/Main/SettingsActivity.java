package mag.ir.mimchat.Activites.Main;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import mag.ir.mimchat.R;
import mag.ir.mimchat.Utilities.Utils;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.blurView)
    BlurView blurView;
    @BindView(R.id.back)
    carbon.widget.LinearLayout back;
    @BindView(R.id.updateStatus)
    carbon.widget.Button updateStatus;
    @BindView(R.id.status)
    EditText status;
    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.statusText)
    TextView statusText;
    @BindView(R.id.profileImageButton)
    carbon.widget.Button profileImageButton;
    @BindView(R.id.wallpaperButton)
    carbon.widget.Button wallpaperButton;
    @BindView(R.id.profileImage)
    CircleImageView profileImage;
    @BindView(R.id.backgroundImageView)
    ImageView backgroundImageView;

    private float radius = 10f;
    private String currentUserId;
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private StorageReference profileImageRef, wallRef;

    private static final int galleryPick = 1;
    private static final int wallPick = 2;
    private String profileOrBackground = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        init();
    }

    private void init() {

        View decorView = getWindow().getDecorView();
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();

        blurView.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(radius)
                .setHasFixedTransformationMatrix(true);

        Utils.hideKeyboard(SettingsActivity.this);
        back.setOnClickListener(this);
        updateStatus.setOnClickListener(this);
        wallpaperButton.setOnClickListener(this);
        profileImageButton.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        profileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        wallRef = FirebaseStorage.getInstance().getReference().child("Wallpapers");

        retrieveUserInfo();
    }

    private void retrieveUserInfo() {

        Dialog dialog = Utils.loading(this);
        dialog.show();

        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image") && dataSnapshot.hasChild("background")))) {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    if (dataSnapshot.child("status").exists()) {
                        String userStatusString = dataSnapshot.child("status").getValue().toString();
                        statusText.setText(userStatusString);
                    }
                    String userProfileImage = dataSnapshot.child("image").getValue().toString();
                    String userbackgroundImage = dataSnapshot.child("background").getValue().toString();

                    Picasso.get().load(userProfileImage).into(profileImage);
                    Picasso.get().load(userbackgroundImage).into(backgroundImageView);
                    name.setText(userName);
                    dialog.dismiss();

                } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))) {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    if (dataSnapshot.child("status").exists()) {
                        String userStatusString = dataSnapshot.child("status").getValue().toString();
                        statusText.setText(userStatusString);
                    }
                    String userProfileImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(userProfileImage).into(profileImage);
                    name.setText(userName);

                    dialog.dismiss();

                } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("background")))) {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    if (dataSnapshot.child("status").exists()) {
                        String userStatusString = dataSnapshot.child("status").getValue().toString();
                        statusText.setText(userStatusString);
                    }
                    String userbackgroundImage = dataSnapshot.child("background").getValue().toString();

                    Picasso.get().load(userbackgroundImage).into(backgroundImageView);
                    name.setText(userName);
                    dialog.dismiss();

                } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    if (dataSnapshot.child("status").exists()) {
                        String userStatusString = dataSnapshot.child("status").getValue().toString();
                        statusText.setText(userStatusString);
                    }

                    name.setText(userName);
                    dialog.dismiss();

                } else {
                    dialog.dismiss();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                Utils.sendToMainActivity(this);
                break;

            case R.id.updateStatus:
                updateUserStatus();
                break;

            case R.id.profileImageButton:
                profileOrBackground = "prof";
                setProfilePicture();
                break;

            case R.id.wallpaperButton:
                profileOrBackground = "wall";
                setWallPicture();
                break;
        }
    }

    private void setWallPicture() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, wallPick);
    }

    private void setProfilePicture() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, galleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);

        }
        if (requestCode == wallPick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);

        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Dialog dialog = Utils.loading(this);
                dialog.show();

                if (profileOrBackground.equals("prof")) {

                    Uri resultUri = result.getUri();
                    final StorageReference filePath = profileImageRef.child(currentUserId + ".jpg");

                    filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUri = uri;
                                    Toast.makeText(SettingsActivity.this, "آواتار با موفقیت بارگذاری شد...", Toast.LENGTH_SHORT).show();
                                    String generatedFilePath = downloadUri.toString();

                                    rootRef.child("Users").child(currentUserId).child("image").setValue(generatedFilePath).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                dialog.dismiss();
                                            } else {
                                                String message = task.getException().toString();
                                                Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            String message = e.toString();
                                            Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    });
                                }
                            });
                        }
                    });
                } else {
                    Uri resultUri = result.getUri();
                    final StorageReference filePath = wallRef.child(currentUserId + ".jpg");

                    filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downloadUri = uri;
                                    Toast.makeText(SettingsActivity.this, "پس زمینه با موفقیت بارگذاری شد...", Toast.LENGTH_SHORT).show();
                                    String generatedFilePath = downloadUri.toString();

                                    rootRef.child("Users").child(currentUserId).child("background").setValue(generatedFilePath).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                dialog.dismiss();

                                            } else {
                                                String message = task.getException().toString();
                                                Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            String message = e.toString();
                                            Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Utils.sendToMainActivity(this);
    }

    private void updateUserStatus() {
        String userStatus = status.getText().toString();

        if (TextUtils.isEmpty(userStatus)) {
            Utils.showErrorMessage(SettingsActivity.this, "اول وضعیتو ثبت کن");
            return;
        }

        Dialog dialog = Utils.loading(this);
        dialog.show();

        rootRef.child("Users").child(currentUserId).child("status").setValue(userStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    dialog.dismiss();
                    Utils.showToast(SettingsActivity.this, "وضعیتت در سرور ذخیره شد...");
                    status.setText("");
                } else {
                    dialog.dismiss();
                    Utils.showErrorMessage(SettingsActivity.this, "Error : " + task.getException().toString());
                }
            }
        });
    }
}

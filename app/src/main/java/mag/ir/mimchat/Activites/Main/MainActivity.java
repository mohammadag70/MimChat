package mag.ir.mimchat.Activites.Main;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.gauravk.bubblenavigation.BubbleNavigationConstraintView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.skydoves.powermenu.CircularEffect;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import mag.ir.mimchat.Fragments.ContactsFragment;
import mag.ir.mimchat.Fragments.GroupChatFragment;
import mag.ir.mimchat.Fragments.RequestFragment;
import mag.ir.mimchat.Fragments.SingleChatFragment;
import mag.ir.mimchat.R;
import mag.ir.mimchat.Utilities.SolarCalendar;
import mag.ir.mimchat.Utilities.Utils;

import static maes.tech.intentanim.CustomIntent.customType;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int TIME_INTERVAL = 2000;
    @BindView(R.id.bubbleNavigation)
    BubbleNavigationConstraintView bubbleNavigation;
    @BindView(R.id.navBar)
    carbon.widget.LinearLayout navbar;

    private FragmentManager fm;
    private FragmentTransaction fmt;
    private List<PowerMenuItem> list = new ArrayList<>();
    private PowerMenu powerMenu;
    private FirebaseAuth auth;
    private DatabaseReference rootRef;
    private Fragment currentFrag;
    private FirebaseUser currentUser;
    private String currentUserId;
    private long mBackPressed;

    public static boolean gotoAnotherPage = false;

    private OnMenuItemClickListener<PowerMenuItem> onMenuItemClickListener = new OnMenuItemClickListener<PowerMenuItem>() {
        @Override
        public void onItemClick(int position, PowerMenuItem item) {
            powerMenu.setSelectedPosition(position); // change selected item
            powerMenu.dismiss();

            switch (position) {
                case 0:
                    Intent intent = new Intent(MainActivity.this, FindFriendsActivity.class);
                    customType(MainActivity.this, "left-to-right");
                    MainActivity.gotoAnotherPage = true;
                    startActivity(intent);
                    break;
                case 1:
                    requestNewGroup();
                    break;
                case 2:
                    MainActivity.gotoAnotherPage = true;
                    Utils.sendToSettingsActivity(MainActivity.this);
                    break;
                case 3:
                    if (currentUser != null) {
                        updateUserStatus("آفلاین");
                    }
                    gotoAnotherPage = true;
                    auth.signOut();
                    Utils.sendToLoginActivity(MainActivity.this);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        getPermissions();
        init();

    }

    private void init() {

        navbar.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

        list.add(new PowerMenuItem("یافتن دوستان"));
        list.add(new PowerMenuItem("ساختن گروه"));
        list.add(new PowerMenuItem("تنظیمات"));
        list.add(new PowerMenuItem("خروج"));

        bubbleNavigation.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                Fragment fragment = null;
                Fragment f = null;
                Fragment currentFragment = fm.findFragmentById(R.id.container);

                switch (position) {
                    case 0:
                        f = getSupportFragmentManager().findFragmentById(R.id.container);
                        if (f instanceof SingleChatFragment) {
                            break;
                        }
                        fragment = new SingleChatFragment();
                        fm.beginTransaction()
                                .remove(currentFragment)
                                .add(R.id.container, fragment)
                                .commit();
                        break;
                    case 1:
                        f = getSupportFragmentManager().findFragmentById(R.id.container);
                        if (f instanceof GroupChatFragment) {
                            break;
                        }
                        fragment = new GroupChatFragment();
                        fm.beginTransaction()
                                .remove(currentFragment)
                                .add(R.id.container, fragment)
                                .commit();
                        currentFrag = fragment;
                        break;
                    case 2:
                        f = getSupportFragmentManager().findFragmentById(R.id.container);
                        if (f instanceof ContactsFragment) {
                            break;
                        }
                        fragment = new ContactsFragment();
                        fm.beginTransaction()
                                .remove(currentFragment)
                                .add(R.id.container, fragment)
                                .commit();
                        break;
                    case 3:
                        f = getSupportFragmentManager().findFragmentById(R.id.container);
                        if (f instanceof RequestFragment) {
                            break;
                        }
                        fragment = new RequestFragment();
                        fm.beginTransaction()
                                .remove(currentFragment)
                                .add(R.id.container, fragment)
                                .commit();
                        break;
                }

            }
        });

        bubbleNavigation.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Vazir.ttf"));

    }

    private void getPermissions() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (report.areAllPermissionsGranted()) {
                }
                if (report.isAnyPermissionPermanentlyDenied()) {
                    Toast.makeText(MainActivity.this, "Oops!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.navBar:
                powerMenu = new PowerMenu.Builder(MainActivity.this)
                        .addItemList(list)
                        .setAnimation(MenuAnimation.SHOWUP_TOP_LEFT)
                        .setCircularEffect(CircularEffect.INNER)
                        .setMenuRadius(10f) // sets the corner radius.
                        .setMenuShadow(10f) // sets the shadow.
                        .setTextColor(getResources().getColor(R.color.colorPrimaryDark))
                        .setTextGravity(Gravity.CENTER)
                        .setTextTypeface(Typeface.createFromAsset(getAssets(), "fonts/Vazir.ttf"))
                        .setOnMenuItemClickListener(onMenuItemClickListener)
                        .build();
                powerMenu.showAsDropDown(view); // view is an anchor
                break;
        }
    }

    private void requestNewGroup() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.create_group_layout);

        Button cancel = dialog.findViewById(R.id.cancel);
        Button submit = dialog.findViewById(R.id.submit);
        EditText gpName = dialog.findViewById(R.id.gpName);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gpName.getText().toString().isEmpty()) {
                    Utils.showErrorMessage(MainActivity.this, "نام گروه را وارد نمایید!");
                    return;
                }

                createNewGp(gpName.getText().toString(), dialog);
            }
        });

        dialog.show();
    }

    private void createNewGp(String gpName, Dialog d) {
        Dialog dialog = Utils.loading(this);
        dialog.show();

        SolarCalendar solarCalendar = new SolarCalendar();
        String dateOfSubmit = solarCalendar.date + " " + Utils.getMonth(solarCalendar.month) + " " + solarCalendar.year;
        Date date = new Date();
        String timeOfSubmit = DateFormat.getTimeInstance().format(date);

        String currentUserId = auth.getCurrentUser().getUid();

        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("name").getValue().toString();
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("uid", currentUserId);
                hashMap.put("date", dateOfSubmit);
                hashMap.put("time", timeOfSubmit);
                hashMap.put("username", username);

                rootRef.child("Groups").child(gpName).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            d.dismiss();
                            Utils.showSuccessMessage(MainActivity.this, "گروه با موفقیت ساخته شد...");

                            if (getSupportFragmentManager().findFragmentById(R.id.container) instanceof GroupChatFragment) {
                                ((GroupChatFragment) currentFrag).notifyAdapter();
                            }
                        } else {
                            dialog.dismiss();
                            Utils.showErrorMessage(MainActivity.this, "خطا از سرور. مجددا اقدام کن");
                            d.dismiss();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        gotoAnotherPage = false;
        if (currentUser == null) {
            Utils.sendToLoginActivity(this);
        } else {

            try {

                String extra = getIntent().getExtras().get("extra").toString();

                if (extra.equals("g")) {
                    fm = getSupportFragmentManager();
                    fmt = fm.beginTransaction();
                    fmt.replace(R.id.container, new GroupChatFragment());
                    fmt.commit();
                    bubbleNavigation.setCurrentActiveItem(1);
                } else {
                    fm = getSupportFragmentManager();
                    fmt = fm.beginTransaction();
                    fmt.replace(R.id.container, new SingleChatFragment());
                    fmt.commit();
                    bubbleNavigation.setCurrentActiveItem(0);
                }

            } catch (Exception ignored) {
                fm = getSupportFragmentManager();
                fmt = fm.beginTransaction();
                fmt.replace(R.id.container, new SingleChatFragment());
                fmt.commit();
                bubbleNavigation.setCurrentActiveItem(0);
            }

            updateUserStatus("آنلاین");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!gotoAnotherPage) {
            if (currentUser != null) {
                updateUserStatus("آفلاین");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!gotoAnotherPage) {
            if (currentUser != null) {
                updateUserStatus("آفلاین");
            }
        }
    }

    private void updateUserStatus(String state) {
        String dateOfMessage, timeOfMessage;

        SolarCalendar solarCalendar = new SolarCalendar();
        dateOfMessage = solarCalendar.date + " " + Utils.getMonth(solarCalendar.month) + " " + solarCalendar.year;

        Date date = new Date();
        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
        timeOfMessage = displayFormat.format(date);

        HashMap<String, Object> onlineState = new HashMap<>();
        onlineState.put("time", timeOfMessage);
        onlineState.put("date", dateOfMessage);
        onlineState.put("state", state);

        currentUserId = auth.getCurrentUser().getUid();

        rootRef.child("Users").child(currentUserId).child("userState").updateChildren(onlineState);

    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {

            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed();
                finishAffinity();
                System.exit(0);
                return;
            } else {
                Utils.showInfoMessage(MainActivity.this, "برای خروج دوباره دکمه بازگشت را فشار دهید");
            }
            mBackPressed = System.currentTimeMillis();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}

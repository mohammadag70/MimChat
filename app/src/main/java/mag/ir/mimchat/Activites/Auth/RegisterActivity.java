package mag.ir.mimchat.Activites.Auth;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import carbon.view.View;
import mag.ir.mimchat.R;
import mag.ir.mimchat.Utilities.Utils;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.name)
    EditText name;
    @BindView(R.id.email)
    EditText email;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.confirmPassword)
    EditText confirmPassword;
    @BindView(R.id.registerButton)
    carbon.widget.Button registerButton;
    @BindView(R.id.goToLoginPage)
    TextView goToLoginPage;

    private FirebaseAuth auth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        init();

    }

    private void init() {
        Utils.hideUnderLine(email);
        Utils.hideUnderLine(name);
        Utils.hideKeyboard(this);

        rootRef = FirebaseDatabase.getInstance().getReference();

        goToLoginPage.setOnClickListener(this);
        registerButton.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(android.view.View view) {
        switch (view.getId()) {
            case R.id.goToLoginPage:
                Utils.sendToLoginActivity(RegisterActivity.this);
                break;

            case R.id.registerButton:
                createNewAccount();
                break;
        }
    }

    private void createNewAccount() {
        String nameV = name.getText().toString();
        String emailV = email.getText().toString();
        String passwordV = password.getText().toString();
        String confirmPasswordV = confirmPassword.getText().toString();

        if (TextUtils.isEmpty(nameV)) {
            Utils.showErrorMessage(RegisterActivity.this, "نامتو وارد کن");
            return;
        }

        if (TextUtils.isEmpty(emailV)) {
            Utils.showErrorMessage(RegisterActivity.this, "ایمیلتو وارد کن");
            return;
        }

        if (TextUtils.isEmpty(passwordV)) {
            Utils.showErrorMessage(RegisterActivity.this, "گذرواژتو وارد کن");
            return;
        }

        if (!passwordV.equals(confirmPasswordV)) {
            Utils.showErrorMessage(RegisterActivity.this, "گذرواژه ها مطابقت ندارن");
            return;
        }

        if (!Utils.isEmailValid(emailV)) {
            Utils.showErrorMessage(RegisterActivity.this, "ایمیلت معتبر نیست!");
            return;
        }

        Dialog dialog = Utils.loading(this);
        dialog.show();

        if (Utils.hasNetworkOrNot(this)) {
            auth.createUserWithEmailAndPassword(emailV, passwordV)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                String currentUserId = auth.getCurrentUser().getUid();

                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("uid", currentUserId);
                                hashMap.put("name", nameV);
                                hashMap.put("device_token", deviceToken);

                                rootRef.child("Users").child(currentUserId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Utils.sendToMainActivity(RegisterActivity.this);
                                            dialog.dismiss();
                                            Utils.showToast(RegisterActivity.this, "حسابت با موفقیت ساخته شد :)");
                                        } else {
                                            dialog.dismiss();
                                            Utils.showErrorMessage(RegisterActivity.this, "Error : " + task.getException().toString());
                                        }
                                    }
                                });

                            } else {
                                dialog.dismiss();
                                Utils.showErrorMessage(RegisterActivity.this, "Error : " + task.getException().toString());
                            }
                        }
                    });
        } else {
            Utils.showNoInternet(RegisterActivity.this);
        }
    }
}

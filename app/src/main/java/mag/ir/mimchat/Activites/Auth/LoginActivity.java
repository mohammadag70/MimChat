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
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import carbon.view.View;
import mag.ir.mimchat.R;
import mag.ir.mimchat.Utilities.Utils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.email)
    EditText email;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.loginButton)
    carbon.widget.Button loginButton;
    @BindView(R.id.forgetPassword)
    TextView forgetPassword;
    @BindView(R.id.goToRegisterPage)
    TextView goToRegisterPage;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        init();
    }

    private void init() {
        Utils.hideUnderLine(email);
        Utils.hideKeyboard(this);

        goToRegisterPage.setOnClickListener(this);
        loginButton.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(android.view.View view) {
        switch (view.getId()) {
            case R.id.goToRegisterPage:
                Utils.sendToRegisterActivity(LoginActivity.this);
                break;

            case R.id.loginButton:
                allowUserToLogin();
                break;
        }
    }

    private void allowUserToLogin() {
        String emailV = email.getText().toString();
        String passwordV = password.getText().toString();

        if (TextUtils.isEmpty(emailV)) {
            Utils.showErrorMessage(LoginActivity.this, "ایمیلتو وارد کن");
            return;
        }

        if (TextUtils.isEmpty(passwordV)) {
            Utils.showErrorMessage(LoginActivity.this, "گذرواژتو وارد کن");
            return;
        }

        if (!Utils.isEmailValid(emailV)) {
            Utils.showErrorMessage(LoginActivity.this, "ایمیلت معتبر نیست!");
            return;
        }

        Dialog dialog = Utils.loading(this);
        dialog.show();

        auth.signInWithEmailAndPassword(emailV, passwordV).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Utils.sendToMainActivity(LoginActivity.this);
                    dialog.dismiss();
                    Utils.showToast(LoginActivity.this, "با موفقیت وارد شدید...");
                } else {
                    dialog.dismiss();
                    Utils.showErrorMessage(LoginActivity.this, "Error : " + task.getException().toString());
                }
            }
        });
    }
}

package mag.ir.mimchat.Activites.Main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import mag.ir.mimchat.R;

import static maes.tech.intentanim.CustomIntent.customType;

public class SplashActivity extends AppCompatActivity {

    @BindView(R.id.loadingBar)
    LottieAnimationView loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        loadingBar.playAnimation();

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    customType(SplashActivity.this, "left-to-right");
                    finish();
                }
            }
        };
        timer.start();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

}

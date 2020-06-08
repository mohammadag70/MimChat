package mag.ir.mimchat.Utilities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.InputType;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ceylonlabs.imageviewpopup.ImagePopup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.mateware.snacky.Snacky;
import mag.ir.mimchat.Activites.Auth.LoginActivity;
import mag.ir.mimchat.Activites.Auth.RegisterActivity;
import mag.ir.mimchat.Activites.Main.MainActivity;
import mag.ir.mimchat.Activites.Main.SettingsActivity;
import mag.ir.mimchat.R;

import static maes.tech.intentanim.CustomIntent.customType;

public class Utils {

    private static String[] persianNumbers = new String[]{"۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹"};

    public static void showSuccessMessage(Activity activity, String msg) {
        Snacky.builder()
                .setActivity(activity)
                .setText(msg)
                .setTextTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/Vazir.ttf"))
                .centerText().setDuration(Snacky.LENGTH_SHORT).success().show();
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static void showErrorMessage(Activity activity, String msg) {
        Snacky.builder()
                .setActivity(activity)
                .setText(msg)
                .setTextTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/Vazir.ttf"))
                .centerText().setDuration(Snacky.LENGTH_SHORT).error().show();
    }

    public static void showInfoMessage(Activity activity, String msg) {
        Snacky.builder()
                .setActivity(activity)
                .setText(msg)
                .setTextTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/Vazir.ttf"))
                .centerText().setDuration(Snacky.LENGTH_SHORT).warning().show();
    }

    public static void sendToLoginActivity(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        customType(activity, "left-to-right");
        activity.startActivity(intent);
        activity.finish();
    }

    public static void sendToSettingsActivity(Activity activity) {
        Intent intent = new Intent(activity, SettingsActivity.class);
        customType(activity, "left-to-right");
        activity.startActivity(intent);
        activity.finish();
    }

    public static void sendToMainActivity(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        customType(activity, "left-to-right");
        activity.startActivity(intent);
        activity.finish();
    }

    public static void sendToMainActivityWithExtra(Activity activity, String extra) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra("extra", extra);
        customType(activity, "left-to-right");
        activity.startActivity(intent);
        activity.finish();
    }

    public static void sendToRegisterActivity(Activity activity) {
        Intent intent = new Intent(activity, RegisterActivity.class);
        customType(activity, "left-to-right");
        activity.startActivity(intent);
        activity.finish();
    }

    public static void hideUnderLine(EditText editText) {
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

    public static void hideKeyboard(Activity activity) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public static boolean hasNetworkOrNot(Activity activity) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public static void showNoInternet(Activity activity) {
        Snacky.builder()
                .setActivity(activity)
                .setText("ارتباز اینترنت رو چک کن!")
                .centerText().setDuration(Snacky.LENGTH_SHORT).error().show();
    }

    public static Dialog loading(Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.loader);
        return dialog;
    }

    public static void showToast(Activity activity, String msg) {
        Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
        LinearLayout toastLayout = (LinearLayout) toast.getView();
        TextView toastTV = (TextView) toastLayout.getChildAt(0);
        toastTV.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/Vazir.ttf"));
        toast.show();
    }

    public static String getMonth(int month) {

        String strMonth = "";
        switch (month) {
            case 1:
                strMonth = "فروردين";
                break;
            case 2:
                strMonth = "ارديبهشت";
                break;
            case 3:
                strMonth = "خرداد";
                break;
            case 4:
                strMonth = "تير";
                break;
            case 5:
                strMonth = "مرداد";
                break;
            case 6:
                strMonth = "شهريور";
                break;
            case 7:
                strMonth = "مهر";
                break;
            case 8:
                strMonth = "آبان";
                break;
            case 9:
                strMonth = "آذر";
                break;
            case 10:
                strMonth = "دي";
                break;
            case 11:
                strMonth = "بهمن";
                break;
            case 12:
                strMonth = "اسفند";
                break;
        }

        return strMonth;
    }

    public static String toPersianNumber(String text) {
        if (text.length() == 0) {
            return "";
        }
        String out = "";
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if ('0' <= c && c <= '9') {
                int number = Integer.parseInt(String.valueOf(c));
                out += persianNumbers[number];
            } else if (c == '٫') {
                out += '،';
            } else {
                out += c;
            }
        }
        return out;
    }

    public static void popUpImage(Context activity, String path) {
        ImagePopup imagePopup = new ImagePopup(activity);
        imagePopup.setWindowWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        imagePopup.setWindowHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        imagePopup.setBackgroundColor(activity.getResources().getColor(R.color.trans));  // Optional
        imagePopup.setImageOnClickClose(true);
        imagePopup.setHideCloseIcon(true);

        imagePopup.initiatePopupWithPicasso(path);
        imagePopup.viewPopup();
    }

    public static boolean isPersian(String str) {
        Pattern RTL_CHARACTERS = Pattern.compile("[\u0600-\u06FF\u0750-\u077F\u0590-\u05FF\uFE70-\uFEFF]");
        Matcher matcher = RTL_CHARACTERS.matcher(str);
        if (matcher.find()) {
            return true;
        } else {
            return false;
        }
    }

}

package ua.com.anyapps.makemoneyfast;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class MyFunctions {
    private static final String TAG = "debapp";


    // диалог вход/регистрация
    public static ProgressDialog showProgressDialog(Context _context, ProgressDialog _mProgressDialog, String _caption) {
        ProgressDialog mProgressDialog = _mProgressDialog;
        if (_mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(_context);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
        }

        mProgressDialog.setMessage(_caption);
        mProgressDialog.show();
        return mProgressDialog;
    }

    public static void hideProgressDialog(ProgressDialog _mProgressDialog) {
        if (_mProgressDialog != null && _mProgressDialog.isShowing()) {
            _mProgressDialog.dismiss();
        }
    }

    // расстояние между координатами gps без высоты в метрах
    public static double distance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }



}

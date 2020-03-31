package ua.com.anyapps.makemoneyfast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.toRadians;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class CompassActivity extends AppCompatActivity  implements OnMapReadyCallback {
    private AdView mAdView;

    private static final String TAG = "debapp";

    TextView tvPlaceName;
    TextView tvVicinity;
    TextView tvDistance;
    Double placeLat;
    Double placeLng;
    String placeName;
    String placeVicinity;
    String placeTypes;
    String placeId;
    private ImageView ivPhoto;

    LocationManager locationManager;
    Button btnSendPhoto;

    SharedPreferences sharedPreferences;

    public static final int REQUEST_LOCATION_PERMISSION = 0;

    AlertDialog dialogPermissionRequest;

    private FirebaseAuth mAuth;
    FirebaseUser user;
    final int REQUEST_CODE_PHOTO = 1;

    private GoogleMap mMap;
    private PolylineOptions pathOptions;
    private Polyline pathToPlace; // линия от телефона к объекту

    Bitmap smallArrow;

    Marker placeMarker;
    Marker selfMarker;

    private Bitmap bitmap = null; // фото объекта

    private StorageReference mStorageRef;
    private ProgressDialog mProgressDialog;

    private InterstitialAd interstitialAd;

    private FirebaseAnalytics mFirebaseAnalytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        locationManager = (LocationManager) getSystemService( LOCATION_SERVICE );

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(CompassActivity.this);

        tvPlaceName = (TextView)findViewById(R.id.tvPlaceName);
        tvVicinity = (TextView)findViewById(R.id.tvVicinity);
        tvDistance = (TextView)findViewById(R.id.tvDistance);
        ivPhoto = findViewById(R.id.ivPhoto);
        btnSendPhoto = findViewById(R.id.btnSendPhoto);

        // данные из активити
        Intent intent = getIntent();
        placeName = intent.getStringExtra("placeName");
        tvPlaceName.setText(intent.getStringExtra("placeName"));
        tvVicinity.setText(intent.getStringExtra("placeVicinity"));
        placeLat = intent.getDoubleExtra("placeLat", -1);
        placeLng = intent.getDoubleExtra("placeLng", -1);
        placeTypes = intent.getStringExtra("placeTypes");
        placeId = intent.getStringExtra("placeId");
        placeVicinity = intent.getStringExtra("placeVicinity");

        // фотик
        ivPhoto.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CODE_PHOTO);
            }
        });

        // инициализация закладок
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tabSpec1 = tabHost.newTabSpec("tag1");
        TabHost.TabSpec tabSpec2 = tabHost.newTabSpec("tag2");

        tabSpec1.setContent(R.id.tab1);
        tabSpec1.setIndicator(getResources().getString(R.string.compass_tab1_title));
        tabHost.addTab(tabSpec1);

        tabSpec2.setContent(R.id.tab2);
        tabSpec2.setIndicator(getResources().getString(R.string.compass_tab2_title));
        tabHost.addTab(tabSpec2);

        tabHost.setCurrentTab(0);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(CompassActivity.this);

        // стрелка на карте -Я
        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.arrow);
        Bitmap b=bitmapdraw.getBitmap();
        smallArrow = Bitmap.createScaledBitmap(b, width, height, false);

        // firebase хранилище
        mStorageRef = FirebaseStorage.getInstance().getReference();

        MobileAds.initialize(this, "ca-app-pub-4761500786576152~8215465788");
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        AdRequest request = new AdRequest.Builder().build();
        interstitialAd.loadAd(request);
        interstitialAd.setAdListener(new AdListener(){
            public void onAdLoaded(){
                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                }
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "Пользователь нажал на объявление " + user.getEmail());
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("clicks").child(user.getUid()).push();
                myRef.child("adtype").setValue("interstitial1");
                myRef.child("clicktime").setValue(ServerValue.TIMESTAMP);

                String locale = getResources().getConfiguration().locale.getCountry();
                TelephonyManager tm = (TelephonyManager)CompassActivity.this.getSystemService(CompassActivity.this.TELEPHONY_SERVICE);
                String countryCodeValue = tm.getNetworkCountryIso();
                myRef.child("locale").setValue(locale);
                myRef.child("countrycode").setValue(countryCodeValue);

                Bundle bundle = new Bundle();
                //bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
                bundle.putString("user_id", user.getUid());
                bundle.putString("time", ServerValue.TIMESTAMP.toString());
                mFirebaseAnalytics.logEvent("click_on_interstitial_ad", bundle);

                super.onAdClicked();
            }
        });

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }





    @Override
    protected void onPause() {
        if(locationManager!=null){
            // отключение уведомлений об изменении координат
            locationManager.removeUpdates(locationListener);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        // пользователь вышел - переход к авторизации
        if (user == null){
            Intent intent = new Intent(this, EmailPasswordActivity.class);
            startActivity(intent);
            finish();
        }

        if(ActivityCompat.checkSelfPermission(CompassActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CompassActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            // есть разрешения
            setLocationListener();
        }else{
            // разрешения отсутствуют. запрос
            // До android 6 и выше
            if (Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(CompassActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(CompassActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // показать объяснение зачем включать и запросить разрешение
                    dialogPermissionRequest = explanationAndPermissionRequest();
                }else{
                    // запросить разрешение
                    requestLocationPermissions();
                }
            }
        }
        super.onResume();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == REQUEST_CODE_PHOTO) {
            if (resultCode == RESULT_OK) {
                if (intent == null) {
                    // фото не получено
                } else {
                    Bundle bndl = intent.getExtras();
                    if (bndl != null) {
                        Object obj = intent.getExtras().get("data");
                        if (obj instanceof Bitmap) {
                            bitmap = (Bitmap) obj;
                            //Log.d(TAG, "bitmap " + bitmap.getWidth() + " x "
                            //        + bitmap.getHeight() + " размер "+bitmap.getByteCount()/ 1024 + "KB");

                            // для более высокого качества фото не использовать bitmap, а использовать Uri https://stackoverflow.com/questions/34609275/android-camera-intent-low-bitmap-quality
                            ivPhoto.setImageBitmap(bitmap);
                        }
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Canceled");
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    private LocationListener locationListener = new LocationListener() {
        private Location deviceCoordinates;
        boolean firstTime = true;
        @Override
        public void onLocationChanged(Location _location) {
            deviceCoordinates = _location;

            DecimalFormat df = new DecimalFormat("0.00");
            Double distance = MyFunctions.distance(deviceCoordinates.getLatitude(), deviceCoordinates.getLongitude(), placeLat, placeLng);

            // если расстояние от телефона к объекту меньше 50 метров
            if((int) Math.round(distance)<=getResources().getInteger(R.integer.distance) && distance>0) {
                tvDistance.setTextColor(getResources().getColor(R.color.compassPhoneInZone));
                // активировать кнопку если в радиусе и фото сделано
                if(bitmap!=null) {
                    btnSendPhoto.setEnabled(true);
                }
            }else{
                tvDistance.setTextColor(getResources().getColor(R.color.compassPhoneOutOfZone));
                btnSendPhoto.setEnabled(false);
            }

            tvDistance.setText(getResources().getString(R.string.nearby_places_list_item_distance_title) + df.format(distance) + getResources().getString(R.string.nearby_places_list_item_distance_unit_title));

            // очистка карты перед рисованием
            mMap.clear();

            placeMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(placeLat, placeLng)).title(placeName));

            // свойства линии от меня до объекта
            pathOptions = new PolylineOptions();
            pathOptions.color(getResources().getColor(R.color.colorAccent) );
            pathOptions.width( 10 );
            pathOptions.visible( true );
            pathOptions.add( new LatLng(_location.getLatitude(), _location.getLongitude()) );
            pathOptions.add( new LatLng(placeLat, placeLng) );
            pathToPlace = mMap.addPolyline(pathOptions);

            // маркер положения телефона
            selfMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(_location.getLatitude(), _location.getLongitude()))
                    .icon(BitmapDescriptorFactory.fromBitmap(smallArrow))
                    .anchor(0.5f, 0.5f)
                    .rotation(_location.getBearing())
                    .flat(true));

            //Toast.makeText(CompassActivity.this, "Bearing "  + _location.getBearing(), Toast.LENGTH_SHORT).show();

            // передвинуть камеру на маркер только первый раз
            if(firstTime) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(_location.getLatitude(), _location.getLongitude())));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(100), 2000, null);
                firstTime=false;
            }
            //ivCompass.setRotation(_location.getBearing());


            // два маркера в центре
            /*LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(m1.getPosition());
            builder.include(m2.getPosition());
            LatLngBounds bounds = builder.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.12); // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,width,height, padding);
            mMap.moveCamera(cu);*/

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            // gps включен
            Log.d(TAG, "GPS enabled");
        }


        @Override
        public void onProviderDisabled(String provider) {
            // gps выключен
            Log.d(TAG, "GPS disabled");
            enableGpsMessageDialogBox();
        }
    };

    // установка слушателя для получения координат из gps или спутника
    private void setLocationListener(){
        /*
        - минимальное время (в миллисекундах) между получением данных. Я укажу здесь 10 секунд, мне этого вполне хватит. Если хотите получать координаты без задержек – передавайте 0. Но учитывайте, что это только минимальное время. Реальное ожидание может быть дольше.
        - минимальное расстояние (в метрах). Т.е. если ваше местоположение изменилось на указанное кол-во метров, то вам придут новые координаты.
         */

        try{
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 1, locationListener);
            } else {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 0, 1,
                        locationListener);
            }
        }catch (SecurityException ex){
            Log.e(TAG, ex.getMessage());
        }
    }

    // предложение включить gps
    private AlertDialog enableGpsMessageDialogBox() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.enable_gps_message))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.enable_gps_positive_button), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(getResources().getString(R.string.enable_gps_negative_button), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                        // закрыть приложение, если пользователь не включил gps
                        if (Build.VERSION.SDK_INT >= 16&&Build.VERSION.SDK_INT<21) {
                            finishAffinity();
                            System.exit(0);
                        }
                        if (Build.VERSION.SDK_INT >= 21) {
                            finishAndRemoveTask ();
                        }


                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
        return alert;
    }

    // объяснение для пользователя
    private AlertDialog explanationAndPermissionRequest(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.explanation_message))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.explanation_positive_button), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        // запрос разрешения
                        requestLocationPermissions();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.explanation_negative_button), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        //dialog.cancel();
                        // закрыть приложение, если пользователь не включил gps
                        if (Build.VERSION.SDK_INT >= 16&&Build.VERSION.SDK_INT<21) {
                            finishAffinity();
                            System.exit(0);
                        }
                        if (Build.VERSION.SDK_INT >= 21) {
                            finishAndRemoveTask ();
                        }
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
        return alert;
    }

    // запрос разрешения
    private void requestLocationPermissions(){
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
    }


    // сохранение фото
    StorageReference riversRef;
    byte[] bitmapData;
    public void btnSendPhotoClick(View v) {
        Log.d(TAG, "SEND PHOTO");

        riversRef = mStorageRef.child("images/"+user.getUid()+"/"+ UUID.randomUUID().toString()+".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        bitmapData = baos.toByteArray();

        mProgressDialog = MyFunctions.showProgressDialog(this, mProgressDialog, getResources().getString(R.string.progress_dialogLoading_text));

        // Если в базе есть уже Id объекта, то сравнить время с текущим и если больше лимита, то позволить отправку
        DatabaseReference firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("images").child(user.getUid());;
        firebaseDatabaseReference.orderByChild("placeid").equalTo(placeId).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            long creationTimeMillis = 0; // время создания фото текущего объекта на сервере
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if(dataSnapshot.getChildrenCount()>=1){
                    //Log.d(TAG, "Уже есть!");
                    for (DataSnapshot imageSnapshot : dataSnapshot.getChildren()) {
                        creationTimeMillis = Long.valueOf(imageSnapshot.child("creationtimemillis").getValue().toString());
                        break;
                    }
                }else{
                    //Log.d(TAG, "Такого объекта нет");
                }

                // отправка фото
                riversRef.putBytes(bitmapData)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                //Uri downloadUrl = taskSnapshot.getDownloadUrl();

                                //Сделать запрос по placeid и если есть то сравнить время
                                // фото сохранено

                                Log.d(TAG, "Разница " + (taskSnapshot.getMetadata().getUpdatedTimeMillis()-creationTimeMillis));
                                long difference = taskSnapshot.getMetadata().getUpdatedTimeMillis()-creationTimeMillis;
                                if(difference <getResources().getInteger(R.integer.period)) {
                                    Log.d(TAG, "Не прошел период. Осталось " + TimeUnit.MILLISECONDS.toMinutes(getResources().getInteger(R.integer.period)-difference) + " min");
                                    MyFunctions.hideProgressDialog(mProgressDialog);
                                    Toast.makeText(CompassActivity.this, getResources().getString(R.string.photo_time_limit1)+" "+TimeUnit.MILLISECONDS.toMinutes(getResources().getInteger(R.integer.period)-difference)+" "+getResources().getString(R.string.photo_time_limit2), Toast.LENGTH_LONG).show();
                                    return;
                                }else{
                                    Log.d(TAG, "Период нормально. Создается фото");
                                }

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                // автоматически сгенерированное id
                                DatabaseReference myRef = database.getReference("images").child(user.getUid()).push();
                                myRef.child("path").setValue(taskSnapshot.getMetadata().getPath());
                                myRef.child("bucket").setValue(taskSnapshot.getMetadata().getBucket());
                                myRef.child("contenttype").setValue(taskSnapshot.getMetadata().getContentType());
                                myRef.child("generation").setValue(taskSnapshot.getMetadata().getGeneration());
                                myRef.child("name").setValue(taskSnapshot.getMetadata().getName());
                                myRef.child("md5hash").setValue(taskSnapshot.getMetadata().getMd5Hash());
                                myRef.child("creationtimemillis").setValue(taskSnapshot.getMetadata().getCreationTimeMillis());
                                myRef.child("updatedtimemillis").setValue(taskSnapshot.getMetadata().getUpdatedTimeMillis());
                                myRef.child("sizebytes").setValue(taskSnapshot.getMetadata().getSizeBytes());
                                myRef.child("paid").setValue(0);
                                myRef.child("placeid").setValue(placeId);
                                //myRef.child("placetypes").setValue(placeTypes);
                                String locale = getResources().getConfiguration().locale.getCountry();
                                TelephonyManager tm = (TelephonyManager)CompassActivity.this.getSystemService(CompassActivity.this.TELEPHONY_SERVICE);
                                String countryCodeValue = tm.getNetworkCountryIso();
                                myRef.child("locale").setValue(locale);
                                myRef.child("countrycode").setValue(countryCodeValue);
                                myRef.child("placeName").setValue(placeName);
                                myRef.child("placeVicinity").setValue(placeVicinity);
                                myRef.child("addtime").setValue(ServerValue.TIMESTAMP);


                                MyFunctions.hideProgressDialog(mProgressDialog);
                                MediaPlayer mp = MediaPlayer.create(CompassActivity.this, R.raw.getmoneysound);
                                mp.start();
                                //MyFunctions.showCongratulatoryDialog(CompassActivity.this, getString(R.string.congratulatory_dialog_title), getString(R.string.congratulatory_dialog_positive_button_text));
                                AlertDialog.Builder builder = new AlertDialog.Builder(CompassActivity.this);
                                builder.setCancelable(false);
                                //builder.setIcon(mImage);
                                ImageView showImage = new ImageView(CompassActivity.this);
                                showImage.setImageDrawable(CompassActivity.this.getResources().getDrawable(R.drawable.getmoney));
                                builder.setView(showImage);
                                builder.setTitle(getString(R.string.congratulatory_dialog_title));
                                builder.setCancelable(false);
                                builder.setPositiveButton(getString(R.string.congratulatory_dialog_positive_button_text),new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        CompassActivity.this.finish();
                                        dialog.dismiss();
                                    }
                                });

                                builder.create().show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                                Log.e(TAG, "Failed to save photo");
                                MyFunctions.hideProgressDialog(mProgressDialog);
                                Toast.makeText(CompassActivity.this, getResources().getString(R.string.upload_photo_failed_toast_text), Toast.LENGTH_LONG).show();
                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                // ошибка при поиске в базе placeId
                MyFunctions.hideProgressDialog(mProgressDialog);
                Toast.makeText(CompassActivity.this, getResources().getString(R.string.upload_photo_failed_toast_text), Toast.LENGTH_LONG).show();
                Log.d(TAG, "Ошибка при поиске placeId в Firebase Database - " +databaseError.getMessage() );
            }
        });




        /*riversRef.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        //Сделать запрос по placeid и если есть то сравнить время

                        Log.d(TAG, "Photo uploaded getPath: " + taskSnapshot.getMetadata().getPath());
                        Log.d(TAG, "Photo uploaded getBucket: " + taskSnapshot.getMetadata().getBucket());
                        Log.d(TAG, "Photo uploaded getCacheControl: " + taskSnapshot.getMetadata().getCacheControl());
                        Log.d(TAG, "Photo uploaded getContentDisposition: " + taskSnapshot.getMetadata().getContentDisposition());
                        Log.d(TAG, "Photo uploaded getContentEncoding: " + taskSnapshot.getMetadata().getContentEncoding());
                        Log.d(TAG, "Photo uploaded getContentLanguage: " + taskSnapshot.getMetadata().getContentLanguage());
                        Log.d(TAG, "Photo uploaded getContentType: " + taskSnapshot.getMetadata().getContentType());
                        Log.d(TAG, "Photo uploaded getGeneration: " + taskSnapshot.getMetadata().getGeneration());
                        Log.d(TAG, "Photo uploaded getName: " + taskSnapshot.getMetadata().getName());
                        Log.d(TAG, "Photo uploaded getMd5Hash: " + taskSnapshot.getMetadata().getMd5Hash());
                        Log.d(TAG, "Photo uploaded getCreationTimeMillis: " + taskSnapshot.getMetadata().getCreationTimeMillis());
                        Log.d(TAG, "Photo uploaded getUpdatedTimeMillis: " + taskSnapshot.getMetadata().getUpdatedTimeMillis());
                        Log.d(TAG, "Photo uploaded getSizeBytes: " + taskSnapshot.getMetadata().getSizeBytes());
                        Log.d(TAG, "Photo uploaded getMetadataGeneration: " + taskSnapshot.getMetadata().getMetadataGeneration());
                        // фото сохранено

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        // автоматически сгенерированное id
                        DatabaseReference myRef = database.getReference("images").child(user.getUid()).push();
                        myRef.child("path").setValue(taskSnapshot.getMetadata().getPath());
                        myRef.child("bucket").setValue(taskSnapshot.getMetadata().getBucket());
                        myRef.child("contenttype").setValue(taskSnapshot.getMetadata().getContentType());
                        myRef.child("generation").setValue(taskSnapshot.getMetadata().getGeneration());
                        myRef.child("name").setValue(taskSnapshot.getMetadata().getName());
                        myRef.child("md5hash").setValue(taskSnapshot.getMetadata().getMd5Hash());
                        myRef.child("creationtimemillis").setValue(taskSnapshot.getMetadata().getCreationTimeMillis());
                        myRef.child("updatedtimemillis").setValue(taskSnapshot.getMetadata().getUpdatedTimeMillis());
                        myRef.child("sizebytes").setValue(taskSnapshot.getMetadata().getSizeBytes());
                        myRef.child("paid").setValue(0);
                        myRef.child("placeid").setValue(placeId);
                        //myRef.child("placetypes").setValue(placeTypes);
                        String locale = getResources().getConfiguration().locale.getCountry();
                        TelephonyManager tm = (TelephonyManager)CompassActivity.this.getSystemService(CompassActivity.this.TELEPHONY_SERVICE);
                        String countryCodeValue = tm.getNetworkCountryIso();
                        myRef.child("locale").setValue(locale);
                        myRef.child("countrycode").setValue(countryCodeValue);
                        myRef.child("placeName").setValue(placeName);
                        myRef.child("placeVicinity").setValue(placeVicinity);


                        MyFunctions.hideProgressDialog(mProgressDialog);
                        MediaPlayer mp = MediaPlayer.create(CompassActivity.this, R.raw.getmoneysound);
                        mp.start();
                        //MyFunctions.showCongratulatoryDialog(CompassActivity.this, getString(R.string.congratulatory_dialog_title), getString(R.string.congratulatory_dialog_positive_button_text));
                        AlertDialog.Builder builder = new AlertDialog.Builder(CompassActivity.this);
                        builder.setCancelable(false);
                        //builder.setIcon(mImage);
                        ImageView showImage = new ImageView(CompassActivity.this);
                        showImage.setImageDrawable(CompassActivity.this.getResources().getDrawable(R.drawable.getmoney));
                        builder.setView(showImage);
                        builder.setTitle(getString(R.string.congratulatory_dialog_title));
                        builder.setCancelable(false);
                        builder.setPositiveButton(getString(R.string.congratulatory_dialog_positive_button_text),new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CompassActivity.this.finish();
                                dialog.dismiss();
                            }
                        });

                        builder.create().show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Log.e(TAG, "Failed to save photo");
                        MyFunctions.hideProgressDialog(mProgressDialog);
                        Toast.makeText(CompassActivity.this, "", Toast.LENGTH_LONG).show();
                    }
                });*/
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


}

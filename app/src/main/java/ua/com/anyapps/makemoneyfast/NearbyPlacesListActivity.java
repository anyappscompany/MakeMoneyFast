package ua.com.anyapps.makemoneyfast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.com.anyapps.makemoneyfast.Server.NearbySearch.NearbySearch;
import ua.com.anyapps.makemoneyfast.Server.NearbySearch.Results;
import ua.com.anyapps.makemoneyfast.Server.ServerConnect;

public class NearbyPlacesListActivity extends AppCompatActivity {


    SharedPreferences sharedPreferences;
    LocationManager locationManager;
    AlertDialog dialogPermissionRequest;
    private static final String TAG = "debapp";
    public static final int REQUEST_LOCATION_PERMISSION = 0;

    ProgressBar pbLoading;

    private String gMapKey;
    ListView lvNearbyPlacesList;

    private FirebaseAuth mAuth;
    FirebaseUser user;

    // список ближайших объектов
    ArrayList<Results> nearbyPlaces = new ArrayList<Results>();
    NearbyPlacesListAdapter nearbyPlacesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_places_list);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        pbLoading = (ProgressBar)findViewById(R.id.pbLoading);
        lvNearbyPlacesList = (ListView) findViewById(R.id.lvNearbyPlacesList);

        pbLoading.setVisibility(View.VISIBLE);

        locationManager = (LocationManager) getSystemService( LOCATION_SERVICE );
        gMapKey = getResources().getString(R.string.g_map_key);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(NearbyPlacesListActivity.this);

        // создание адаптера
        nearbyPlacesListAdapter = new NearbyPlacesListAdapter(NearbyPlacesListActivity.this, nearbyPlaces);

        // настройка списка
        lvNearbyPlacesList.setAdapter(nearbyPlacesListAdapter);

        // при клике на одном из мест, открыть активити для отправки фото
        lvNearbyPlacesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Results clickedItem = nearbyPlaces.get(position);

                // отправка фото
                // данные объекта
                Intent intent = new Intent(NearbyPlacesListActivity.this, CompassActivity.class);
                intent.putExtra("placeLat", clickedItem.getGeometry().getLocation().getLat());
                intent.putExtra("placeLng", clickedItem.getGeometry().getLocation().getLng());
                intent.putExtra("placeName", clickedItem.getName());
                intent.putExtra("placeVicinity", clickedItem.getVicinity());
                intent.putExtra("placeId", clickedItem.getId());
                // типы объекта
                intent.putExtra("placeTypes", String.valueOf(clickedItem.getTypes()));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        // не получать gps данные, если активити не активно
        if(locationManager!=null){
            locationManager.removeUpdates(locationListener);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        // если пользователь вышел, то прейти на авторизацию
        if (user == null){
            Intent intent = new Intent(this, EmailPasswordActivity.class);
            startActivity(intent);
            finish();
        }

        pbLoading.setVisibility(View.VISIBLE);
        lvNearbyPlacesList.setVisibility(View.GONE);

        if(ActivityCompat.checkSelfPermission(NearbyPlacesListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NearbyPlacesListActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            // есть разрешения
            setLocationListener();
        }else{
            // разрешения отсутствуют. запрос
            // До android 6 и выше
            if (Build.VERSION.SDK_INT >= 23 && ActivityCompat.checkSelfPermission(NearbyPlacesListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(NearbyPlacesListActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
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

    // установка слушателя для получения координат из gps или спутника
    private void setLocationListener(){
        /*
        - минимальное время (в миллисекундах) между получением данных. Я укажу здесь 10 секунд, мне этого вполне хватит. Если хотите получать координаты без задержек – передавайте 0. Но учитывайте, что это только минимальное время. Реальное ожидание может быть дольше.
        - минимальное расстояние (в метрах). Т.е. если ваше местоположение изменилось на указанное кол-во метров, то вам придут новые координаты.
         */

        try{
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        Integer.valueOf(sharedPreferences.getString("gps_minimum_time_between_receiving_data", getResources().getString(R.string.gps_default_minimum_time_between_receiving_data)))*1000, Integer.valueOf(sharedPreferences.getString("gps_minimum_distance_to_update", getResources().getString(R.string.gps_default_minimum_distance_to_update))), locationListener);
            } else {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, Integer.valueOf(sharedPreferences.getString("gps_minimum_time_between_receiving_data", getResources().getString(R.string.gps_default_minimum_time_between_receiving_data)))*1000, Integer.valueOf(sharedPreferences.getString("gps_minimum_distance_to_update", getResources().getString(R.string.gps_default_minimum_distance_to_update))),
                        locationListener);
            }
        }catch (SecurityException ex){
            Log.e(TAG, ex.getMessage());
        }
    }

    private LocationListener locationListener = new LocationListener() {
        private Location deviceCoordinates;
        @Override
        public void onLocationChanged(Location _location) {
            deviceCoordinates = _location;

            pbLoading.setVisibility(View.VISIBLE);
            ServerConnect.getInstance()
                    .getJSONApi()
                    .searchNearbyPlaces(String.valueOf(_location.getLatitude())+","+String.valueOf(_location.getLongitude()), String.valueOf(sharedPreferences.getString("gps_detect_radius", getResources().getString(R.string.gps_detect_default_radius))), gMapKey)
                    .enqueue(new Callback<NearbySearch>() {
                        @Override
                        public void onResponse(Call<ua.com.anyapps.makemoneyfast.Server.NearbySearch.NearbySearch> call, Response<ua.com.anyapps.makemoneyfast.Server.NearbySearch.NearbySearch> response) {
                            pbLoading.setVisibility(View.GONE);
                            lvNearbyPlacesList.setVisibility(View.VISIBLE);

                            if (response.isSuccessful()) {
                                ua.com.anyapps.makemoneyfast.Server.NearbySearch.NearbySearch nearbyPlacesInfo = response.body();
                                // обновление списка ближайших при каждом обращении к google places api
                                nearbyPlaces.clear();
                                if (nearbyPlacesInfo.getStatus().equals("OK")) {
                                    // зполнение списка с ближайшими объектами
                                    for(Results res:nearbyPlacesInfo.getResults()){
                                        // scope теперь содержит дистанцию в метрах до объекта
                                        res.setScope(String.valueOf(MyFunctions.distance(deviceCoordinates.getLatitude(), deviceCoordinates.getLongitude(), res.getGeometry().getLocation().getLat(), res.getGeometry().getLocation().getLng())));
                                        nearbyPlaces.add(res);
                                    }
                                    // сортировака по дистанции
                                    Collections.sort(nearbyPlaces, new PlacesSorter());

                                    // передача собственных координат в адаптер для вычисления расстояни до объекта
                                    if(deviceCoordinates!=null) {
                                        nearbyPlacesListAdapter.setDeviceCoordibates(deviceCoordinates);
                                        deviceCoordinates = null;
                                    }
                                }
                                // уведомляем, что данные изменились
                                nearbyPlacesListAdapter.notifyDataSetChanged();


                            } else {
                                Log.e(TAG, "Во время входа, сервер вернул ошибку " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<ua.com.anyapps.makemoneyfast.Server.NearbySearch.NearbySearch> call, Throwable t) {
                            pbLoading.setVisibility(View.GONE);
                            lvNearbyPlacesList.setVisibility(View.VISIBLE);
                            Log.e(TAG, "Отказ. Не удалось достучаться до сервера во время активации - " + t);
                        }
                    });
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "GPS enabled");
        }


        @Override
        public void onProviderDisabled(String provider) {
            // показать окно с информацией, что gps отключен
            Log.d(TAG, "GPS disabled");
            enableGpsMessageDialogBox();
        }
    };

    // запрос разрешения
    private void requestLocationPermissions(){
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
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
                        // закрыть активити, если пользователь не включил gps
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

                        // закрыть активити, если пользователь не включил gps
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
}

package ua.com.anyapps.makemoneyfast;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import ua.com.anyapps.makemoneyfast.R;
import ua.com.anyapps.makemoneyfast.Server.NearbySearch.Photos;
import ua.com.anyapps.makemoneyfast.Server.NearbySearch.Results;

public class NearbyPlacesListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater lInflater;
    ArrayList<Results> nearbyPlaces;
    private String gMapKey;

    private Location deviceCoordinates;

    public void setDeviceCoordibates(Location _deviceCoordinates){
        deviceCoordinates = _deviceCoordinates;
    }

    public void resetDeviceCoordinates(){
        deviceCoordinates = null;
    }

    public NearbyPlacesListAdapter(Context _context, ArrayList<Results> _nearbyPlaces) {
        context = _context;
        nearbyPlaces = _nearbyPlaces;
        lInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        gMapKey = _context.getResources().getString(R.string.g_map_key);
    }

    @Override
    public int getCount() {
        return nearbyPlaces.size();
    }

    @Override
    public Object getItem(int position) {
        return nearbyPlaces.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // используем созданные, но не используемые view
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.nearby_places_list_item, parent, false);
        }

        Results r = getProduct(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        TextView tvPlaceName = (TextView)view.findViewById(R.id.tvPlaceName);
        tvPlaceName.setText(r.getName());

        TextView tvVicinity = (TextView)view.findViewById(R.id.tvVicinity);
        tvVicinity.setText(context.getResources().getString(R.string.nearby_places_list_item_vicinity_title)+r.getVicinity());

        ImageView ivPlacePhoto = (ImageView) view.findViewById(R.id.ivPlacePhoto);
        TextView tvDistance = (TextView)view.findViewById(R.id.tvDistance);

        ConstraintLayout constLayRow = (ConstraintLayout)view.findViewById(R.id.constraintLayout);

        // если устройство в радиусе, то выделить место
        //constLayRow.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        tvDistance.setTextColor(context.getResources().getColor(R.color.compassPhoneOutOfZone));
        if(Double.valueOf(r.getScope())<=context.getResources().getInteger(R.integer.distance)){
            //constLayRow.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            tvDistance.setTextColor(context.getResources().getColor(R.color.compassPhoneInZone));
        }



        String photoPath = "";
        // если гугл вернул фото объекта, то установить его
        // иначе гугловскую иконку типа объекта
        List<Photos> placePhotos = r.getPhotos();
        if(placePhotos!=null && placePhotos.size()>0){
            photoPath = "https://maps.googleapis.com/maps/api/place/photo?photoreference="+r.getPhotos().get(0).getPhoto_reference()+"&sensor=false&maxheight=64&maxwidth=64&key=" + gMapKey;
        }else{
            photoPath = r.getIcon();
        }

        Picasso.get().load(photoPath).into(ivPlacePhoto);
        // преобразование расстояния к двум знакам после запятой
        DecimalFormat df = new DecimalFormat("0.00");
        tvDistance.setText(context.getResources().getString(R.string.nearby_places_list_item_distance_title) + df.format(Double.valueOf(r.getScope())) + context.getResources().getString(R.string.nearby_places_list_item_distance_unit_title));

        return view;
    }

    // место по позиции
    Results getProduct(int position) {
        return ((Results) getItem(position));
    }
}

package ua.com.anyapps.makemoneyfast;

import java.util.Comparator;

import ua.com.anyapps.makemoneyfast.Server.NearbySearch.Results;

// сортировка списка ближайших объетов по дистанции
public class PlacesSorter implements Comparator<Results> {
    @Override
    public int compare(Results one, Results another) {
        int returnVal = 0;

        if(Double.valueOf(one.getScope()) < Double.valueOf(another.getScope())){
            returnVal =  -1;
        }else if(Double.valueOf(one.getScope()) > Double.valueOf(another.getScope())){
            returnVal =  1;
        }else if(Double.valueOf(one.getScope()) == Double.valueOf(another.getScope())){
            returnVal =  0;
        }
        return returnVal;
    }
}
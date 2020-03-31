package ua.com.anyapps.makemoneyfast.Server.NearbySearch;
import java.util.ArrayList;
import java.util.List;
public class Results
{
    private Geometry geometry;

    private String icon;

    private String id;

    private String name;

    private List<Photos> photos;

    private String place_id;

    private String reference;

    private String scope;

    private List<String> types;

    private String vicinity;

    public void setGeometry(Geometry geometry){
        this.geometry = geometry;
    }
    public Geometry getGeometry(){
        return this.geometry;
    }
    public void setIcon(String icon){
        this.icon = icon;
    }
    public String getIcon(){
        return this.icon;
    }
    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return this.id;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public void setPhotos(List<Photos> photos){
        this.photos = photos;
    }
    public List<Photos> getPhotos(){
        return this.photos;
    }
    public void setPlace_id(String place_id){
        this.place_id = place_id;
    }
    public String getPlace_id(){
        return this.place_id;
    }
    public void setReference(String reference){
        this.reference = reference;
    }
    public String getReference(){
        return this.reference;
    }
    public void setScope(String scope){
        this.scope = scope;
    }
    public String getScope(){
        return this.scope;
    }
    public void setTypes(List<String> types){
        this.types = types;
    }
    public List<String> getTypes(){
        return this.types;
    }
    public void setVicinity(String vicinity){
        this.vicinity = vicinity;
    }
    public String getVicinity(){
        return this.vicinity;
    }
}
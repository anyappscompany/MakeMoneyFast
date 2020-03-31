package ua.com.anyapps.makemoneyfast.Server.NearbySearch;
public class Geometry
{
    private Location location;

    private Viewport viewport;

    public void setLocation(Location location){
        this.location = location;
    }
    public Location getLocation(){
        return this.location;
    }
    public void setViewport(Viewport viewport){
        this.viewport = viewport;
    }
    public Viewport getViewport(){
        return this.viewport;
    }
}

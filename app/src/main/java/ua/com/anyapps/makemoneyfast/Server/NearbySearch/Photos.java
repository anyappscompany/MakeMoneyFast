package ua.com.anyapps.makemoneyfast.Server.NearbySearch;
import java.util.ArrayList;
import java.util.List;
public class Photos
{
    private int height;

    private List<String> html_attributions;

    private String photo_reference;

    private int width;

    public void setHeight(int height){
        this.height = height;
    }
    public int getHeight(){
        return this.height;
    }
    public void setHtml_attributions(List<String> html_attributions){
        this.html_attributions = html_attributions;
    }
    public List<String> getHtml_attributions(){
        return this.html_attributions;
    }
    public void setPhoto_reference(String photo_reference){
        this.photo_reference = photo_reference;
    }
    public String getPhoto_reference(){
        return this.photo_reference;
    }
    public void setWidth(int width){
        this.width = width;
    }
    public int getWidth(){
        return this.width;
    }
}
package ua.com.anyapps.makemoneyfast.Server.NearbySearch;

import java.util.ArrayList;
import java.util.List;

public class NearbySearch
{
    private List<String> html_attributions;

    private String next_page_token;

    private List<Results> results;

    private String status;

    public void setHtml_attributions(List<String> html_attributions){
        this.html_attributions = html_attributions;
    }
    public List<String> getHtml_attributions(){
        return this.html_attributions;
    }
    public void setNext_page_token(String next_page_token){
        this.next_page_token = next_page_token;
    }
    public String getNext_page_token(){
        return this.next_page_token;
    }
    public void setResults(List<Results> results){
        this.results = results;
    }
    public List<Results> getResults(){
        return this.results;
    }
    public void setStatus(String status){
        this.status = status;
    }
    public String getStatus(){
        return this.status;
    }
}

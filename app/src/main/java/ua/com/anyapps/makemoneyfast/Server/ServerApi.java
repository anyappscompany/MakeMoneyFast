package ua.com.anyapps.makemoneyfast.Server;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServerApi {
    // новый ключ доступа
    //@Headers({"Host: i98825p1.bget.ru", "Connection: keep-alive", "Cache-Control: max-age=0", "Upgrade-Insecure-Requests: 1", "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.27 Safari/537.36", "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3", "Accept-Encoding: gzip, deflate", "Accept-Language: ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7"})
    @GET("place/nearbysearch/json")
    Call<ua.com.anyapps.makemoneyfast.Server.NearbySearch.NearbySearch> searchNearbyPlaces(@Query("location") String location, @Query("radius") String radius, @Query("key") String key);

    /*
    // вход

    @GET("devicelogin/{accesskey}")
    Call<ua.com.anyapps.prihodiuhodsotrudnikov.Server.DeviceLogin.AccessKey> deviceLogin(@Path("accesskey") String accessKey);

    // список сотрудников для accessKey

    @GET("getemployees/{accesskey}")
    Call<ua.com.anyapps.prihodiuhodsotrudnikov.Server.Employees.Employees> getEmployees(@Path("accesskey") String accessKey);

    // время на сервере
    @GET("getservertime")
    Call<ua.com.anyapps.prihodiuhodsotrudnikov.Server.ServerTime.ServerTime> getServerTime();

    // отправка сотрудников
    @POST("addnewemployee/{dataquery}")
    Call<UploadEmployee> uploadEmployees(@Body RequestBody photo, @Path("dataquery") String dataQuery);
    //{"sucess":true,"data":[1,2,3]} - список id в базе, которые добавились на сервер
    */
}

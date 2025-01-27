package cu.arr.lntcapp.api;

import androidx.annotation.Keep;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Keep
public class RetrofitClient {

    private static final String BASE_URL = "https://sheets.googleapis.com/";
    private static Retrofit retrofit =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build();

    public static GoogleSheetsApi getClient() {
        return retrofit.create(GoogleSheetsApi.class);
    }
}

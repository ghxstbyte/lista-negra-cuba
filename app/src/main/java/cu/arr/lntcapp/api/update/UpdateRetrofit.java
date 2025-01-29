package cu.arr.lntcapp.api.update;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class UpdateRetrofit {


    private static Retrofit retrofit =
            new Retrofit.Builder()
                    .baseUrl("https://raw.githubusercontent.com/ghxstbyte/lista-negra-cuba/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    .build();

    public static UpdateApi getClient() {
        return retrofit.create(UpdateApi.class);
    }
}

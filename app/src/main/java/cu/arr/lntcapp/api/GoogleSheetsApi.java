package cu.arr.lntcapp.api;

import androidx.annotation.Keep;
import cu.arr.lntcapp.api.model.GoogleSheetsResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

@Keep
public interface GoogleSheetsApi {

    @GET("v4/spreadsheets/{spreadsheetsId}/values/{range}")
    Single<GoogleSheetsResponse> getSheetData(
            @Path("spreadsheetsId") String spreadsheetsId,
            @Path("range") String range,
            @Query("key") String apiKey);
}

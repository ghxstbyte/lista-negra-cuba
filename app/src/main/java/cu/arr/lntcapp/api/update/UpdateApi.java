package cu.arr.lntcapp.api.update;

import androidx.annotation.Keep;
import cu.arr.lntcapp.api.update.model.UpdateResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;

@Keep
public interface UpdateApi {

    @GET("refs/heads/main/changelog.json")
    Single<UpdateResponse> getUpdate();
}

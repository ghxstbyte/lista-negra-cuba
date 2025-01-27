package cu.arr.lntcapp.api.model;

import androidx.annotation.Keep;
import java.util.List;

@Keep
public class GoogleSheetsResponse {

    private String range;
    private String majorDimension;
    private List<List<String>> values;

    public String getRange() {
        return range;
    }

    public String getMajorDimension() {
        return majorDimension;
    }

    public List<List<String>> getValues() {
        return values;
    }
}

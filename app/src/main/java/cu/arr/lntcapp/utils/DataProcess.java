package cu.arr.lntcapp.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cu.arr.lntcapp.models.FilterItems;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataProcess {

    public static void processAndGenerateJson(List<List<String>> values, Context context) {
        JsonObject rootObject = new JsonObject();
        JsonArray filterArray = new JsonArray();
        String update = "";
        for (int i = 1; i < values.size(); i++) {
            List<String> row = values.get(i);
            if (row.size() >= 2) {
                JsonObject filterObject = new JsonObject();
                filterObject.addProperty("no", row.get(0));
                for (String cell : row) {
                    if (cell.startsWith("http")) {
                        filterObject.addProperty("link", cell);
                    } else if (cell.contains("Actualizado")) {
                        update = cell.split(":")[1].trim();
                    }
                }

                filterArray.add(filterObject);
            }
        }

        rootObject.addProperty("update", update);
        rootObject.add("Filter", filterArray);
        saveJsonToFile(rootObject, context);
    }

    private static void saveJsonToFile(JsonObject jsonObject, Context context) {
        try {
            FileWriter writer = new FileWriter(getDirectory(context));
            writer.write(jsonObject.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<FilterItems> listData(Context context) {
        Gson gson = new Gson();
        JsonObject object = gson.fromJson(getReadJson(context), JsonObject.class);
        if (object == null || !object.has("Filter")) {
            return new ArrayList<>();
        }
        JsonArray array = object.getAsJsonArray("Filter");
        List<FilterItems> list = new ArrayList<>();
        list.clear();
        for (int i = 0; i < array.size(); i++) {
            JsonObject item = array.get(i).getAsJsonObject();
            String no = item.get("no").getAsString();
            String link = item.get("link").getAsString();
            list.add(new FilterItems(no, link));
        }
        Collections.reverse(list);
        return list;
    }

    private static String getReadJson(Context context) {
        try {
            File file = getDirectory(context);
            if (file == null || !file.exists()) {
                return null;
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUpdateFromJson(Context c) {
        try {
            Gson gson = new Gson();
            JsonObject jsonObject =
                    gson.fromJson(new FileReader(getDirectory(c)), JsonObject.class);
            return jsonObject.get("update").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File getDirectory(Context context) {
        try {
            //  File file = context.getDataDir();
            File file = context.getExternalFilesDir("data");

            if (file != null && !file.exists()) {
                file.mkdirs();
            }
            return new File(file, "filtered_data.json");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

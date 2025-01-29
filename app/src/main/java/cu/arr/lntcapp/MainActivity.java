package cu.arr.lntcapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import cu.arr.lntcapp.adapters.FilterAdapter;
import cu.arr.lntcapp.api.GoogleSheetsApi;
import cu.arr.lntcapp.api.RetrofitClient;
import cu.arr.lntcapp.api.model.GoogleSheetsResponse;
import cu.arr.lntcapp.api.update.UpdateApi;
import cu.arr.lntcapp.api.update.UpdateRetrofit;
import cu.arr.lntcapp.api.update.model.UpdateResponse;
import cu.arr.lntcapp.databinding.ActivityMainBinding;
import cu.arr.lntcapp.models.FilterItems;
import cu.arr.lntcapp.utils.DataProcess;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private String spreadsheetsId = "1_0e4WIOJHamLgTo08d1eHyrjFNxCrjvlQe-X_f6ybSo";
    private String apiKey = BuildConfig.API_KEY;

    private GoogleSheetsApi api;
    private UpdateApi apiUpdate;

    private FilterAdapter adapter;

    private List<FilterItems> filteredList;
    private List<FilterItems> fullList;

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().hide();

        binding.toolbar.setTitleCentered(true);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstAccess = sp.getBoolean("firstAccess", true);
        if (isFirstAccess) {
            disclaimer();
        }

        // api google sheets
        api = RetrofitClient.getClient();
        apiUpdate = UpdateRetrofit.getClient();
        getDataUser();
        getNewUpdateApp();

        // list items
        fullList = DataProcess.listData(this);
        filteredList = new ArrayList<>(fullList);
        adapter = new FilterAdapter(this, filteredList);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        // button report
        binding.reportar.setOnClickListener(this::startToReport);

        // facebook group
        binding.canalFacebook.setOnClickListener(
                v -> startLink("https://www.facebook.com/groups/577666448442033/"));

        // validate text
        binding.editSearch.addTextChangedListener(new ValidateSearch());

        // btn search
        binding.btnSearch.setOnClickListener(this::openKeyboard);

        // about
        binding.about.setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));
    }

    private void getDataUser() {
        disposable =
                api.getSheetData(spreadsheetsId, "Hoja 1", apiKey)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(
                                dispose -> {
                                    binding.recycler.setVisibility(View.GONE);
                                    binding.reportar.setVisibility(View.GONE);
                                    binding.textUpdate.setVisibility(View.GONE);
                                    binding.title.setText("Cargando...");
                                })
                        .doFinally(
                                () -> {
                                    binding.title.setText("Reportes más recientes");
                                    binding.textUpdate.setText(
                                            "Actualizado el "
                                                    + DataProcess.getUpdateFromJson(this));
                                    binding.textUpdate.setVisibility(View.VISIBLE);
                                    binding.recycler.setVisibility(View.VISIBLE);
                                    binding.reportar.setVisibility(View.VISIBLE);
                                })
                        .subscribe(this::handlerSuccess, this::handlerError);
    }

    private void handlerSuccess(GoogleSheetsResponse response) {
        DataProcess.processAndGenerateJson(response.getValues(), this);
        fullList = new ArrayList<>(DataProcess.listData(this));
        filteredList.clear();
        filteredList.addAll(fullList);
        adapter.notifyDataSetChanged();
    }

    private void handlerError(Throwable e) {
        showMessage(e.getMessage());
    }

    private void startLink(String link) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void openKeyboard(View view) {
        binding.editSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(binding.editSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void disclaimer() {
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setCancelable(false)
                .setTitle("Descargo de responsabilidad")
                .setMessage(R.string.disclaimer)
                .setPositiveButton(
                        "Acepto",
                        (v, w) -> {
                            SharedPreferences sp =
                                    PreferenceManager.getDefaultSharedPreferences(this);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean("firstAccess", false);
                            editor.apply();
                        })
                .setNegativeButton("No Acepto", (v, w) -> finish())
                .show();
    }

    private void startToReport(View view) {
        new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("Reporte")
                .setMessage(R.string.report)
                .setPositiveButton(
                        "Reportar",
                        (v, w) -> {
                            startLink("https://www.facebook.com/groups/577666448442033/");
                        })
                .show();
    }

    private void getNewUpdateApp() {
        Single<UpdateResponse> update = apiUpdate.getUpdate();
        update.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handlerUpdate, this::handlerError);
    }

    private void handlerUpdate(UpdateResponse response) {
        // Comparar las versiones usando un método adecuado
        if (isNewVersionAvailable(BuildConfig.VERSION_NAME, response.lastVersion)) {
            dialogUpdate(response.lastVersion, response.changelog);
            Log.e("Response", new Gson().toJson(response));
        }
    }

    /**
     * Método para comparar versiones
     *
     * @param currentVersion Versión actual de la app (ej. "1.0.1")
     * @param latestVersion Versión más reciente disponible (ej. "1.0.3")
     * @return true si hay una nueva versión disponible
     */
    private boolean isNewVersionAvailable(String currentVersion, String latestVersion) {
        String[] currentParts = currentVersion.split("\\.");
        String[] latestParts = latestVersion.split("\\.");
        Log.e("Versiones: ", "last " + latestVersion + " current " + currentVersion);
        for (int i = 0; i < Math.max(currentParts.length, latestParts.length); i++) {
            int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;

            if (latestPart > currentPart) {
                Log.e("Boolean", "hay una nueva versión");
                return true;
            } else if (latestPart < currentPart) {
                Log.e("Boolean", "no hay una nueva versión");
                return false;
            }
        }
        return false;
    }

    private void dialogUpdate(String version, String changelog) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_view_new_release, null);
        TextView txtChangelog = dialogView.findViewById(R.id.changelog);
        TextView txtVersion = dialogView.findViewById(R.id.version);
        txtChangelog.setText(changelog);
        txtVersion.setText("Versión: " + version);
        builder.setView(dialogView)
                .setPositiveButton(
                        "Descargar",
                        (dialog, which) -> {
                            startLink(
                                    "https://www.mediafire.com/folder/55brtpko62nfg/Lista+Negra+Cubana+Apk");
                        });
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public class ValidateSearch implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

        @Override
        public void onTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            filterList(s.toString());
        }

        @Override
        public void afterTextChanged(Editable arg0) {}

        private void filterList(String query) {
            filteredList.clear();
            if (query.isEmpty()) {
                filteredList.addAll(fullList);
                binding.textUpdate.setVisibility(View.VISIBLE);
                binding.title.setText("Reportes más recientes");
            } else {
                boolean found = false;
                for (FilterItems i : fullList) {
                    String normalizedId = i.id.replace("-", "").toLowerCase();

                    if (normalizedId.contains(query)) {
                        filteredList.add(i);
                        binding.title.setText("Número reportado");
                        binding.textUpdate.setVisibility(View.GONE);
                        found = true;
                    }
                }

                if (!found) {
                    binding.textUpdate.setVisibility(View.GONE);
                    binding.title.setText(
                            "El número " + query + " no se encuentra en la lista negra.");
                }
            }
            adapter.updateList(filteredList);
        }
    }
}

package cu.arr.lntcapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import cu.arr.lntcapp.adapters.FilterAdapter;
import cu.arr.lntcapp.api.GoogleSheetsApi;
import cu.arr.lntcapp.api.RetrofitClient;
import cu.arr.lntcapp.api.model.GoogleSheetsResponse;
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
        getDataUser();

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
                        .observeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(
                                dispose -> {
                                    binding.recycler.setVisibility(View.GONE);
                                    binding.reportar.setVisibility(View.GONE);
                                    binding.title.setText("Cargando...");
                                })
                        .doFinally(
                                () -> {
                                    binding.title.setText("Reportes más recientes");
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
        new MaterialAlertDialogBuilder(this)
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
        new MaterialAlertDialogBuilder(this)
                .setTitle("Reporte")
                .setMessage(R.string.report)
                .setPositiveButton(
                        "Reportar",
                        (v, w) -> {
                            startLink("https://www.facebook.com/groups/577666448442033/");
                        })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
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
                binding.title.setText("Reportes más recientes");
            } else {
                boolean found = false;
                for (FilterItems i : fullList) {
                    String normalizedId = i.id.replace("-", "").toLowerCase();

                    if (normalizedId.contains(query)) {
                        filteredList.add(i);
                        binding.title.setText("Número reportado");
                        found = true;
                    }
                }

                if (!found) {
                    binding.title.setText(
                            "El número " + query + " no se encuentra en la lista negra.");
                }
            }
            adapter.updateList(filteredList);
        }
    }
}

package cu.arr.lntcapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import cu.arr.lntcapp.adapters.AboutAdapter;
import cu.arr.lntcapp.databinding.ActivityAboutBinding;
import cu.arr.lntcapp.models.Items;
import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        List<Items> list = new ArrayList<>();
        AboutAdapter adapter = new AboutAdapter(this, list, position -> onClick(position));
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setNestedScrollingEnabled(false);
        binding.recycler.setAdapter(adapter);
        list.add(new Items(0, "Contacto"));
        list.add(new Items(R.drawable.ic_telegram, "Grupo en Telegram"));
        list.add(new Items(R.drawable.ic_email_black, "Correo electrónico"));
        list.add(new Items(R.drawable.ic_bug, "Reportar un error"));

        list.add(new Items(0, "Social"));
        list.add(new Items(R.drawable.ic_github, "GitHub"));
        list.add(new Items(R.drawable.ic_facebook_black, "Facebook"));
        list.add(new Items(R.drawable.ic_telegram, "Canal en Telegram"));
        list.add(new Items(R.drawable.ic_website, "Página Web"));

        // versión app
        binding.version.setText("v" + BuildConfig.VERSION_NAME + " (beta)");
    }

    private void onClick(int position) {
        switch (position) {
                // header
            case 1 -> link(getString(R.string.link_telegram_group));
            case 2 -> link(getString(R.string.link_mail));
            case 3 -> link(getString(R.string.link_github_report));
                // header
            case 5 -> link(getString(R.string.link_github));
            case 6 -> link(getString(R.string.link_fb));
            case 7 -> link(getString(R.string.link_telegram));
            case 8 -> link(getString(R.string.link_web));
        }
    }

    private void link(String resourse) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(resourse)));
    }
}

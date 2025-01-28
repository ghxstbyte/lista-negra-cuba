package cu.arr.lntcapp.adapters;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.annotations.Expose;
import cu.arr.lntcapp.databinding.LayoutItemsReportBinding;
import cu.arr.lntcapp.models.FilterItems;
import java.util.ArrayList;
import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<FilterItems> list = new ArrayList<>();
    private Context mContext;

    public FilterAdapter(Context context, List<FilterItems> list) {
        this.list = list;
        this.mContext = context;
    }

    public void updateData(List<FilterItems> newData) {
        if (newData != null) {
            this.list.clear();
            this.list.addAll(newData);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int arg1) {
        LayoutItemsReportBinding binding =
                LayoutItemsReportBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false);
        return new FilterView(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FilterView view) {
            FilterItems model = list.get(position);

            view.binding.textNumber.setText(model.id);

            // ver reporte
            view.binding.viewReport.setOnClickListener(
                    v -> {
                        mContext.startActivity(
                                new Intent(Intent.ACTION_VIEW, Uri.parse(model.link)));
                    });

            // apelar
            view.binding.appear.setOnClickListener(
                    v -> {
                        sendEmailAppear(model.id);
                    });

            // copiar numero
            view.binding.getRoot().setOnClickListener(v -> copyToClipboard(model.id));
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(list.size(), 5);
    }

    public void updateList(List<FilterItems> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    private void copyToClipboard(String number) {
        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Number", number);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                Toast.makeText(mContext, "Número copiado al portapapeles", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendEmailAppear(String number) {
        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.putExtra(Intent.EXTRA_EMAIL, new String[] {"listanegracubana@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "APELACIÓN #" + number);
        i.putExtra(
                Intent.EXTRA_TEXT,
                "** Díganos que error hay con su número, por favor adjunte pruebas de que se ha cometido un error con el número reportado.");
        i.setType("text/plain");
        i.setData(Uri.parse("mailto:"));
        try {
            mContext.startActivity(Intent.createChooser(i, "Enviar por correo"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, "No tiene un cliente de correo en su móvil", Toast.LENGTH_LONG)
                    .show();
        }
    }

    class FilterView extends RecyclerView.ViewHolder {

        private LayoutItemsReportBinding binding;

        public FilterView(LayoutItemsReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

package cu.arr.lntcapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import cu.arr.lntcapp.R;
import cu.arr.lntcapp.databinding.LayoutItemsAboutBinding;
import cu.arr.lntcapp.models.Items;
import java.util.List;

public class AboutAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Items> mList;
    private Context context;
    private OnClickItemsListener mListener;

    public AboutAdapter(Context c, List<Items> list, OnClickItemsListener listener) {
        this.mList = list;
        this.context = c;
        this.mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutItemsAboutBinding binding =
                LayoutItemsAboutBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false);
        return new AboutView(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AboutView view) {
            Items model = mList.get(position);
            if (model.icon == 0) {
                view.binding.title.setTextColor(context.getColor(R.color.md_theme_primary));
                view.binding.icon.setVisibility(View.GONE);
            } else {
                view.binding.icon.setImageResource(model.icon);
            }
            view.binding.title.setText(model.title);
            
            view.binding.root.setOnClickListener(v-> mListener.onClickItem(position));
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public interface OnClickItemsListener {
        void onClickItem(int position);
    }

    class AboutView extends RecyclerView.ViewHolder {

        private LayoutItemsAboutBinding binding;

        public AboutView(LayoutItemsAboutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

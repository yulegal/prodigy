package com.vapid_software.prodigy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.api.ApiBuilder;
import com.vapid_software.prodigy.helpers.Defs;
import com.vapid_software.prodigy.helpers.ViewHolder;
import com.vapid_software.prodigy.models.CategoryModel;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter {
    private List<CategoryModel> categories;
    private OnCategoryItemClickedListener onCategoryItemClickedListener;

    public interface OnCategoryItemClickedListener {
        void onCategoryItemClicked(CategoryModel category);
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private ImageView icon;

        public CustomViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.name);
            icon = v.findViewById(R.id.icon);
        }

        public TextView getName() {
            return name;
        }

        public ImageView getIcon() {
            return icon;
        }
    }

    public CategoryAdapter(List<CategoryModel> categories) {
        this.categories = categories;
    }

    public void setOnCategoryItemClickedListener(OnCategoryItemClickedListener onCategoryItemClickedListener) {
        this.onCategoryItemClickedListener = onCategoryItemClickedListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.categories_item, parent, false);
        CustomViewHolder holder = new CustomViewHolder(root);
        root.setOnClickListener((View v) -> {
            if(onCategoryItemClickedListener != null) {
                onCategoryItemClickedListener.onCategoryItemClicked(categories.get(holder.getAdapterPosition()));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        CustomViewHolder holder = (CustomViewHolder) h;
        TextView name = holder.getName();
        ImageView icon = holder.getIcon();
        String locale = holder.itemView.getContext().getResources().getConfiguration().getLocales().get(0).getLanguage();
        CategoryModel category = categories.get(holder.getAdapterPosition());
        name.setText(category.getName().get(locale));
        if(category.getIcon() == null) {
            icon.setImageResource(Defs.CategoryType.ICONS.get(category.getType()));
        }
        else {
            Picasso.get().load(String.join("/", ApiBuilder.PUBLIC_PATH, category.getIcon())).into(icon);
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}

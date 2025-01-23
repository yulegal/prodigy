package com.vapid_software.prodigy.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.helpers.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public class CafeServiceExtraFragment extends Fragment {
    private RecyclerView rv;
    private View button;
    private OnItemChangeListener onItemChangeListener;
    private List<String> items = new ArrayList<>();
    {
        items.add("");
    }

    public interface OnItemChangeListener {
        void onItemChange(List<String> items);
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    private class Adapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(getLayoutInflater().inflate(R.layout.cafe_extra_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            View root = holder.itemView;
            EditText tables = root.findViewById(R.id.tables);
            View delete = root.findViewById(R.id.delete);
            String item = items.get(holder.getAdapterPosition());
            delete.setVisibility(items.size() > 1 ? View.VISIBLE : View.GONE);
            delete.setOnClickListener((View view) -> {
                items.remove(holder.getAdapterPosition());
                notifyDataSetChanged();
            });
            tables.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    items.set(holder.getAdapterPosition(), s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            tables.setText(item);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public void setOnItemChangeListener(OnItemChangeListener onItemChangeListener) {
        this.onItemChangeListener = onItemChangeListener;
    }

    private View.OnClickListener buttonClicked = (View v) -> {
        items.add("");
        rv.getAdapter().notifyDataSetChanged();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cafe_service_extra, container, false);
        rv = root.findViewById(R.id.rv);
        button = root.findViewById(R.id.button);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rv.setAdapter(new Adapter());
        button.setOnClickListener(buttonClicked);
        if(onItemChangeListener != null) {
            onItemChangeListener.onItemChange(items);
        }
        return root;
    }
}
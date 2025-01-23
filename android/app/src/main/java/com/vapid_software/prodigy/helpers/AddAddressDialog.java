package com.vapid_software.prodigy.helpers;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.qkopy.richlink.RichLinkViewQkopy;
import com.qkopy.richlink.ViewListener;
import com.qkopy.richlink.data.model.MetaData;
import com.vapid_software.prodigy.R;
import com.vapid_software.prodigy.models.AddressModel;

public class AddAddressDialog extends Dialog {
    private boolean canAddUrl = true;
    private RichLinkViewQkopy rl;
    private AddressModel address;
    private OnAddressAddedListener onAddressAddedListener;

    public interface OnAddressAddedListener {
        void onAddressAdded(MetaData metaData, AddressModel address);
    }

    public AddAddressDialog(Context context, AddressModel address) {
        super(context);
        this.address = address;
        rl = new RichLinkViewQkopy(context);
        init();
    }

    public void setOnAddressAddedListener(OnAddressAddedListener onAddressAddedListener) {
        this.onAddressAddedListener = onAddressAddedListener;
    }

    private void init() {
        View root = getLayoutInflater().inflate(R.layout.add_address_dialog, null);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        View close = root.findViewById(R.id.close);
        View button = root.findViewById(R.id.button);
        EditText url = root.findViewById(R.id.url);
        TextView error = root.findViewById(R.id.error);
        View loader = root.findViewById(R.id.loader);
        EditText address = root.findViewById(R.id.address);

        close.setOnClickListener((View view) -> {
            dismiss();
        });

        button.setOnClickListener((View view) -> {
            if(!canAddUrl) return;
            canAddUrl = false;
            error.setVisibility(View.GONE);
            String urlValue = url.getText().toString().trim();
            String addressValue = address.getText().toString().trim();
            address.setBackgroundResource(R.drawable.auth_input_wrp);
            if(addressValue.isEmpty()) {
                error.setVisibility(View.VISIBLE);
                error.setText(getContext().getResources().getString(R.string.fill_in_all_fields));
                address.setBackgroundResource(R.drawable.standard_input_e);
                canAddUrl = true;
                return;
            }
            if(!urlValue.isEmpty() && !URLUtil.isValidUrl(urlValue)) {
                canAddUrl = true;
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.invalid_url), Toast.LENGTH_LONG).show();
                return;
            }
            if(!urlValue.isEmpty()) {
                Uri uri = Uri.parse(urlValue);
                if(!uri.getHost().equals("go.2gis.com")) {
                    canAddUrl = true;
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.invalid_url), Toast.LENGTH_LONG).show();
                    return;
                }
            }
            loader.setVisibility(View.VISIBLE);
            if(!urlValue.isEmpty()) {
                rl.setLink(urlValue, getContext(), new ViewListener() {
                    @Override
                    public void onSuccess(boolean b) {
                        if(b) {
                            if(onAddressAddedListener != null) {
                                onAddressAddedListener.onAddressAdded(rl.getMetaData(), new AddressModel(addressValue, urlValue));
                            }
                        }
                        else {
                            Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                        }
                        dismiss();
                    }

                    @Override
                    public void onError(@NonNull Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_has_occurred), Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                });
            }
            else {
                if(onAddressAddedListener != null) {
                    onAddressAddedListener.onAddressAdded(null, new AddressModel(addressValue, null));
                }
                dismiss();
            }
        });

        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        canAddUrl = true;

        if(this.address != null) {
            if(this.address.getUrl() != null) {
                url.setText(this.address.getUrl());
            }
            if(this.address.getAddress() != null) {
                address.setText(this.address.getAddress());
            }
        }

        setContentView(root);
        setCanceledOnTouchOutside(false);
        getWindow().setAttributes(lp);
    }
}

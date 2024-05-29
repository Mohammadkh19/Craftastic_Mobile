package com.example.craftastic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DeleteDialog extends DialogFragment {
    TextView cancel, delete;
    int position;

    public DeleteDialog(int position){
        this.position = position;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.delete_dialog, container);
        cancel = view.findViewById(R.id.btnDeleteCancel);
        delete = view.findViewById(R.id.btnDeleteConfirm);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteConfirm(true, position);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setLayout(width, height);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
    }

    private void deleteConfirm(boolean choice, int position) {
        DeleteDialog.saveDeleteListener activity = (DeleteDialog.saveDeleteListener) getActivity();
        activity.didFinishDeleteDialog(choice, position);
        getDialog().dismiss();
    }

    public interface saveDeleteListener {
        void didFinishDeleteDialog(boolean choice, int position);
    }
}

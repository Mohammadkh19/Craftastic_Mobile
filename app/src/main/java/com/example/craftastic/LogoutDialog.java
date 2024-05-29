package com.example.craftastic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class LogoutDialog extends DialogFragment {
    Button cancel, logout;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.logout_dialog, container);
        cancel = view.findViewById(R.id.btnCancel);
        logout = view.findViewById(R.id.btnLogoutConfirm);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutConfirm(true);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        int width = (int) (getResources().getDisplayMetrics().widthPixels*0.9);
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setLayout(width,height);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
    }

    private void logoutConfirm(boolean choice){
        saveLogoutListener activity = (saveLogoutListener) getActivity();
        activity.didFinishLogoutDialog(choice);
        getDialog().dismiss();
    }

    public interface saveLogoutListener{
        void didFinishLogoutDialog(boolean choice);
    }
}

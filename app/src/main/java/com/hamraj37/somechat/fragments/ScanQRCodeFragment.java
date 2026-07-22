package com.hamraj37.somechat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hamraj37.somechat.ProfileInfoActivity;
import com.hamraj37.somechat.R;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

public class ScanQRCodeFragment extends Fragment {

    private CompoundBarcodeView barcodeView;

    private boolean isProcessing = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_qr, container, false);
        barcodeView = view.findViewById(R.id.barcode_scanner);
        
        // Hide default status text to use our custom overlay
        if (barcodeView.getStatusView() != null) {
            barcodeView.getStatusView().setVisibility(View.GONE);
        }
        
        barcodeView.decodeContinuous(result -> {
            if (isProcessing || result.getText() == null) return;
            
            String scannedData = result.getText().trim();
            String uid = null;
            
            if (scannedData.startsWith("somechat_profile:")) {
                uid = scannedData.substring("somechat_profile:".length());
            } else if (scannedData.contains("hamraj37.github.io/SomeChat")) {
                try {
                    android.net.Uri uri = android.net.Uri.parse(scannedData);
                    uid = uri.getQueryParameter("uid");
                } catch (Exception ignored) {}
            }
            
            if (uid != null && !uid.isEmpty()) {
                isProcessing = true;
                barcodeView.pause();
                
                Intent intent = new Intent(getContext(), ProfileInfoActivity.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
                
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
            // Removed the "Invalid QR" toast because it triggers too often in continuous mode.
            // If scanning fails, the user simply waits or tries another angle.
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isProcessing = false;
        barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}

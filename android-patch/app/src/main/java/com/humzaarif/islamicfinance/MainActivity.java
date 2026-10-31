package com.humzaarif.islamicfinance;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.getcapacitor.BridgeActivity;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView webView = getBridge().getWebView();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new AndroidShareBridge(), "Android");
    }

    private class AndroidShareBridge {
        @JavascriptInterface
        public void sharePdf(String base64, String fileName, String mimeType) {
            try {
                if (base64 == null || base64.isEmpty()) {
                    throw new IllegalArgumentException("Empty PDF data");
                }

                String safeName = (fileName == null || fileName.trim().isEmpty())
                        ? "document.pdf" : fileName.replaceAll("[^A-Za-z0-9._-]", "_");
                if (!safeName.toLowerCase().endsWith(".pdf")) safeName += ".pdf";

                File shareDir = new File(getCacheDir(), "shared_pdfs");
                if (!shareDir.exists() && !shareDir.mkdirs()) {
                    throw new IllegalStateException("Could not create share directory");
                }

                File pdfFile = new File(shareDir, safeName);
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                try (FileOutputStream output = new FileOutputStream(pdfFile)) {
                    output.write(bytes);
                    output.flush();
                }

                Uri uri = FileProvider.getUriForFile(
                        MainActivity.this,
                        getPackageName() + ".fileprovider",
                        pdfFile
                );

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType(mimeType == null || mimeType.isEmpty() ? "application/pdf" : mimeType);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setClipData(ClipData.newRawUri("PDF", uri));

                Intent chooser = Intent.createChooser(shareIntent, "Share PDF");
                chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                runOnUiThread(() -> startActivity(chooser));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        MainActivity.this,
                        "Unable to share PDF: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
            }
        }
    }
}

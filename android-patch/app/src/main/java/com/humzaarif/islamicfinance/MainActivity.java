package com.humzaarif.islamicfinance;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.print.PrintAttributes;
import android.print.PrintManager;

import androidx.core.content.FileProvider;

import com.getcapacitor.BridgeActivity;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends BridgeActivity {

    private WebView printWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // JavaScript se native Android functions access karne ke liye
        getBridge()
                .getWebView()
                .addJavascriptInterface(new AndroidBridge(), "Android");
    }

    private class AndroidBridge {

        // ============================================================
        // PDF SHARE
        // ============================================================

        @JavascriptInterface
        public void sharePdf(
                String base64,
                String fileName,
                String mimeType
        ) {
            try {

                if (fileName == null || fileName.trim().isEmpty()) {
                    fileName = "document.pdf";
                }

                if (mimeType == null || mimeType.trim().isEmpty()) {
                    mimeType = "application/pdf";
                }

                // Temporary sharing folder
                File shareDir = new File(
                        getCacheDir(),
                        "shared_documents"
                );

                if (!shareDir.exists()) {
                    shareDir.mkdirs();
                }

                // Safe filename
                String safeFileName =
                        fileName.replaceAll(
                                "[^a-zA-Z0-9._-]",
                                "_"
                        );

                File file = new File(
                        shareDir,
                        safeFileName
                );

                // Decode Base64 PDF
                byte[] bytes = Base64.decode(
                        base64,
                        Base64.DEFAULT
                );

                // Save PDF
                try (FileOutputStream fos =
                             new FileOutputStream(file)) {

                    fos.write(bytes);
                    fos.flush();
                }

                // Get secure FileProvider URI
                Uri uri = FileProvider.getUriForFile(
                        MainActivity.this,
                        getPackageName()
                                + ".fileprovider",
                        file
                );

                // Android native Share Sheet
                Intent intent =
                        new Intent(Intent.ACTION_SEND);

                intent.setType(mimeType);

                intent.putExtra(
                        Intent.EXTRA_STREAM,
                        uri
                );

                intent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );

                intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                Intent chooser =
                        Intent.createChooser(
                                intent,
                                "Share PDF"
                        );

                startActivity(chooser);

            } catch (Exception e) {

                e.printStackTrace();

            }
        }

        // ============================================================
        // NATIVE ANDROID PRINT
        // ============================================================

        @JavascriptInterface
        public void printHtml(
                final String html,
                final String title
        ) {

            runOnUiThread(() -> {

                try {

                    // Purana print WebView clean karein
                    if (printWebView != null) {

                        try {
                            printWebView.stopLoading();
                            printWebView.destroy();
                        } catch (Exception ignored) {
                        }

                        printWebView = null;
                    }

                    // New hidden WebView
                    printWebView =
                            new WebView(MainActivity.this);

                    WebSettings settings =
                            printWebView.getSettings();

                    settings.setJavaScriptEnabled(true);
                    settings.setDomStorageEnabled(true);

                    printWebView.setWebViewClient(
                            new WebViewClient() {

                                @Override
                                public void onPageFinished(
                                        WebView view,
                                        String url
                                ) {

                                    super.onPageFinished(
                                            view,
                                            url
                                    );

                                    // Thoda wait taake HTML/CSS properly render ho
                                    view.postDelayed(() -> {

                                        try {

                                            PrintManager printManager =
                                                    (PrintManager)
                                                            getSystemService(
                                                                    Context.PRINT_SERVICE
                                                            );

                                            if (printManager == null) {
                                                return;
                                            }

                                            String jobName;

                                            if (title == null
                                                    || title.trim().isEmpty()) {

                                                jobName =
                                                        "Amortization Sheet";

                                            } else {

                                                jobName =
                                                        title.trim();
                                            }

                                            // Android print adapter
                                            android.print.PrintDocumentAdapter adapter =
                                                    view.createPrintDocumentAdapter(
                                                            jobName
                                                    );

                                            // A4 print settings
                                            PrintAttributes attributes =
                                                    new PrintAttributes.Builder()
                                                            .setMediaSize(
                                                                    PrintAttributes.MediaSize.ISO_A4
                                                            )
                                                            .setMinMargins(
                                                                    PrintAttributes.Margins.NO_MARGINS
                                                            )
                                                            .build();

                                            // Native Android Print dialog
                                            printManager.print(
                                                    jobName,
                                                    adapter,
                                                    attributes
                                            );

                                        } catch (Exception e) {

                                            e.printStackTrace();

                                        }

                                    }, 700);
                                }
                            }
                    );

                    // HTML ko WebView mein load karein
                    printWebView.loadDataWithBaseURL(
                            "https://localhost/",
                            html,
                            "text/html",
                            "UTF-8",
                            null
                    );

                } catch (Exception e) {

                    e.printStackTrace();

                }
            });
        }
    }

    // ============================================================
    // CLEANUP
    // ============================================================

    @Override
    protected void onDestroy() {

        if (printWebView != null) {

            try {
                printWebView.stopLoading();
                printWebView.destroy();
            } catch (Exception ignored) {
            }

            printWebView = null;
        }

        super.onDestroy();
    }
}

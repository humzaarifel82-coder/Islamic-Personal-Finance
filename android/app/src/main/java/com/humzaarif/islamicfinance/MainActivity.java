package com.humzaarif.islamicfinance;

import android.content.Context;
import android.content.Intent;
import android.content.ClipData;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.os.Build;
import android.os.Environment;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.getcapacitor.BridgeActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends BridgeActivity {

    private WebView printWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attachBridgeInterface();
    }

    @Override
    public void onResume() {
        super.onResume();
        attachBridgeInterface();
    }

    private void attachBridgeInterface() {
        try {
            if (getBridge() != null && getBridge().getWebView() != null) {
                getBridge().getWebView().addJavascriptInterface(new AndroidBridge(), "Android");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class AndroidBridge {

        // ============================================================
        // NATIVE PDF SHARE VIA ANDROID SHARE SHEET (A4 PDF)
        // ============================================================

        @JavascriptInterface
        public void sharePdf(
                String base64,
                String fileName,
                String mimeType
        ) {
            try {
                if (base64 == null || base64.trim().isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "PDF content is empty", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Strip data URI header if present (e.g., data:application/pdf;base64,...)
                if (base64.contains(",")) {
                    base64 = base64.substring(base64.indexOf(",") + 1);
                }
                base64 = base64.trim().replaceAll("\\s+", "");

                if (fileName == null || fileName.trim().isEmpty()) {
                    fileName = "Assessment-Summary.pdf";
                }
                if (!fileName.toLowerCase().endsWith(".pdf")) {
                    fileName += ".pdf";
                }

                if (mimeType == null || mimeType.trim().isEmpty()) {
                    mimeType = "application/pdf";
                }

                // Clean filename
                String safeFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");

                // Save to cache directory
                File shareDir = new File(getCacheDir(), "shared_documents");
                if (!shareDir.exists()) {
                    shareDir.mkdirs();
                }

                File file = new File(shareDir, safeFileName);

                // Decode base64 bytes
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(bytes);
                    fos.flush();
                }

                // Generate secure FileProvider URI
                String authority = getPackageName() + ".fileprovider";
                Uri uri = FileProvider.getUriForFile(MainActivity.this, authority, file);

                // Native Android Share Intent
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType(mimeType);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Islamic Personal Finance - Assessment Summary");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Assessment Summary Sheet (A4 PDF)");
                shareIntent.setClipData(ClipData.newUri(getContentResolver(), safeFileName, uri));
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Intent chooser = Intent.createChooser(shareIntent, "Share Assessment Sheet (A4 PDF)");
                chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // Explicitly grant read URI permissions to target activities for maximum compatibility
                try {
                    List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String targetPackage = resolveInfo.activityInfo.packageName;
                        grantUriPermission(targetPackage, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                runOnUiThread(() -> {
                    try {
                        startActivity(chooser);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Unable to share file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                final String msg = e.getMessage() != null ? e.getMessage() : "Unknown error";
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Share error: " + msg, Toast.LENGTH_SHORT).show();
                });
            }
        }

        // ============================================================
        // NATIVE ANDROID SAVE TO DEVICE (DOWNLOADS)
        // ============================================================

        @JavascriptInterface
        public void savePdf(
                String base64,
                String fileName,
                String mimeType
        ) {
            try {
                if (fileName == null || fileName.trim().isEmpty()) {
                    fileName = "Assessment-Summary.pdf";
                }
                if (mimeType == null || mimeType.trim().isEmpty()) {
                    mimeType = "application/pdf";
                }

                if (base64.contains(",")) {
                    base64 = base64.substring(base64.indexOf(",") + 1);
                }
                base64 = base64.trim().replaceAll("\\s+", "");

                String safeFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);

                boolean saved = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, safeFileName);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                    Uri uri = getContentResolver().insert(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            values
                    );

                    if (uri != null) {
                        try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                            if (os != null) {
                                os.write(bytes);
                                os.flush();
                                saved = true;
                            }
                        }
                    }
                }

                if (!saved) {
                    File downloadsDir = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                    );
                    if (!downloadsDir.exists()) {
                        downloadsDir.mkdirs();
                    }
                    File outFile = new File(downloadsDir, safeFileName);
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        fos.write(bytes);
                        fos.flush();
                        saved = true;
                    }
                }

                final boolean isSaved = saved;
                final String finalName = safeFileName;
                runOnUiThread(() -> {
                    if (isSaved) {
                        Toast.makeText(MainActivity.this, "PDF saved to Downloads: " + finalName, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Could not save PDF to Downloads", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                final String errMsg = e.getMessage();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Failed to save file: " + errMsg, Toast.LENGTH_SHORT).show();
                });
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
                    if (printWebView != null) {
                        try {
                            printWebView.stopLoading();
                            printWebView.destroy();
                        } catch (Exception ignored) {
                        }
                        printWebView = null;
                    }

                    printWebView = new WebView(MainActivity.this);
                    WebSettings settings = printWebView.getSettings();
                    settings.setJavaScriptEnabled(true);
                    settings.setDomStorageEnabled(true);

                    printWebView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            view.postDelayed(() -> {
                                try {
                                    PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                                    if (printManager == null) return;

                                    String jobName = (title == null || title.trim().isEmpty()) ? "Assessment Summary" : title.trim();
                                    android.print.PrintDocumentAdapter adapter = view.createPrintDocumentAdapter(jobName);

                                    PrintAttributes attributes = new PrintAttributes.Builder()
                                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                                            .build();

                                    printManager.print(jobName, adapter, attributes);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }, 700);
                        }
                    });

                    printWebView.loadDataWithBaseURL("https://localhost/", html, "text/html", "UTF-8", null);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
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

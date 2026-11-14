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
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends BridgeActivity {

    private WebView printWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.registerPlugin(PdfSharePlugin.class);
        super.onCreate(savedInstanceState);
        setupAndroidBridge();
    }

    @Override
    public void onStart() {
        super.onStart();
        setupAndroidBridge();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupAndroidBridge();
    }

    private void setupAndroidBridge() {
        try {
            if (getBridge() != null && getBridge().getWebView() != null) {
                getBridge().getWebView().addJavascriptInterface(new AndroidBridge(this), "Android");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void launchPdfShareChooser(android.app.Activity activity, String base64, String fileName, String title, String text) throws Exception {
        if (activity == null) {
            throw new IllegalStateException("Activity is null");
        }
        if (base64 == null || base64.trim().isEmpty()) {
            throw new IllegalArgumentException("PDF content is empty");
        }

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
        String safeFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");

        File shareDir = new File(activity.getCacheDir(), "shared_documents");
        if (!shareDir.exists()) {
            shareDir.mkdirs();
        }

        File file = new File(shareDir, safeFileName);
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
            fos.flush();
        }

        String authority = activity.getPackageName() + ".fileprovider";
        Uri uri = FileProvider.getUriForFile(activity, authority, file);

        String shareSubject = (title != null && !title.trim().isEmpty()) ? title.trim() : "Islamic Personal Finance Assessment";
        String shareBody = (text != null && !text.trim().isEmpty()) ? text.trim() : "Financial Assessment Summary Sheet (A4 PDF)";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        shareIntent.setClipData(ClipData.newRawUri("Assessment Sheet", uri));
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                activity.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } catch (Exception ignored) {
        }

        Intent chooser = Intent.createChooser(shareIntent, "Share Summary Sheet");
        chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        activity.runOnUiThread(() -> {
            try {
                activity.startActivity(chooser);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(activity, "Share error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ============================================================
    // CAPACITOR NATIVE PLUGIN FOR GUARANTEED EARLY BRIDGE REGISTRATION
    // ============================================================

    @CapacitorPlugin(name = "PdfShare")
    public static class PdfSharePlugin extends Plugin {
        @PluginMethod
        public void sharePdf(PluginCall call) {
            String base64 = call.getString("base64");
            String fileName = call.getString("fileName", "Assessment-Summary.pdf");
            String title = call.getString("title", "Islamic Personal Finance Assessment");
            String text = call.getString("text", "Financial Assessment Summary Sheet (A4 PDF)");

            try {
                launchPdfShareChooser(getActivity(), base64, fileName, title, text);
                call.resolve();
            } catch (Exception e) {
                e.printStackTrace();
                call.reject("Could not share PDF: " + e.getMessage(), e);
            }
        }
    }

    public static class AndroidBridge {

        private final MainActivity activity;

        public AndroidBridge(MainActivity activity) {
            this.activity = activity;
        }

        @JavascriptInterface
        public boolean isNative() {
            return true;
        }

        // ============================================================
        // NATIVE PDF SHARE VIA ANDROID SHARE SHEET (A4 PDF)
        // ============================================================

        @JavascriptInterface
        public void sharePdf(
                String base64,
                String fileName,
                String mimeType
        ) {
            if (activity == null) return;

            try {
                launchPdfShareChooser(
                    activity,
                    base64,
                    fileName,
                    "Islamic Personal Finance Assessment",
                    "Financial Assessment Summary Sheet (A4 PDF)"
                );
            } catch (Exception e) {
                e.printStackTrace();
                final String msg = e.getMessage() != null ? e.getMessage() : "Unknown error";
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, "Could not share PDF: " + msg, Toast.LENGTH_LONG).show();
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
            if (activity == null) return;

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

                    Uri uri = activity.getContentResolver().insert(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            values
                    );

                    if (uri != null) {
                        try (OutputStream os = activity.getContentResolver().openOutputStream(uri)) {
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
                activity.runOnUiThread(() -> {
                    if (isSaved) {
                        Toast.makeText(activity, "PDF saved to Downloads: " + finalName, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(activity, "Could not save PDF to Downloads", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                final String errMsg = e.getMessage();
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, "Failed to save file: " + errMsg, Toast.LENGTH_SHORT).show();
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
            if (activity == null) return;

            activity.runOnUiThread(() -> {
                try {
                    if (activity.printWebView != null) {
                        try {
                            activity.printWebView.stopLoading();
                            activity.printWebView.destroy();
                        } catch (Exception ignored) {
                        }
                        activity.printWebView = null;
                    }

                    activity.printWebView = new WebView(activity);
                    WebSettings settings = activity.printWebView.getSettings();
                    settings.setJavaScriptEnabled(true);
                    settings.setDomStorageEnabled(true);

                    activity.printWebView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            view.postDelayed(() -> {
                                try {
                                    PrintManager printManager = (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);
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

                    activity.printWebView.loadDataWithBaseURL("https://localhost/", html, "text/html", "UTF-8", null);

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

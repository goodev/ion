
package com.koushikdutta.ion.sample;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.koushikdutta.async.util.StreamUtility;
import com.koushikdutta.ion.Loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FontTextView extends TextView {
    public static final String TEST_PRE_URL = "http://upaicdn.xinmei365.com/wfs/2014-06/shijiebeiyulan.zip";
    public static final String TEST_PRE_NAME = "世界杯字体";

    public FontTextView(Context context) {
        super(context);
        mCtx = context.getApplicationContext();
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCtx = context.getApplicationContext();
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mCtx = context.getApplicationContext();
    }

    private static final int LOADING_THREADS = 4;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(LOADING_THREADS);

//    private DownloadPreviewFontTask currentTask;
    private Context mCtx;
    private String mFontUrl;

    public String getFontUrl() {
        return mFontUrl;
    }

    public void setFontUrl(String uri) {
        try {
            String zipUri = uri;
            String ttfUri = FontUtils.getTtfFilePath(uri);
            File file = new File( ttfUri);
            System.out.println("..... 11"+ ttfUri);
            if (!file.exists()) {
                File zipFile = new File( zipUri);
                ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
                try {
                    ZipEntry ze = null;
                    while ((ze = zis.getNextEntry()) != null) {
                        if (ze.getName().endsWith(FontUtils.TTF_SUFFIX)) {
                            FileOutputStream fout = new FileOutputStream(file, false);
                            try {
                                for (int c = zis.read(); c != -1; c = zis.read()) {
                                    fout.write(c);
                                }
                                zis.closeEntry();
                            } finally {
                                fout.close();
                            }
                        }
                    }

                } catch (Exception e) {
                    Log.e("FileLoader", "Unzip exception", e);
                } finally {
                    StreamUtility.closeQuietly(zis);
                }
            }
            try {
                Typeface typeface = Typeface.createFromFile(file);
                setTypeface(typeface);
            } catch (Exception e) {
                Log.e("FileLoader", "create typeface from file exception", e);
                throw new Exception("create typeface from file exception");
            }

        } catch (OutOfMemoryError e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    public void setFontUrl(String url) {
//        if(!url.startsWith("http:")) {
//            try {
//                Typeface type = Typeface.createFromFile(url);
//                if(type != null) {
//                    setTypeface(type);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return;
//        }
//        Typeface typeface = PreviewFontCache.getInstance(mCtx).get(url);
//        if (typeface != null) {
//            setTypeface(typeface);
//            return;
//        }
//        mFontUrl = url;
//        setFont(new PreviewFont(url));
//    }

//    public void setFont(final PreviewFont image) {
//        if (currentTask != null) {
//            currentTask.cancel();
//            currentTask = null;
//        }
//
//        // Set up the new task
//        currentTask = new DownloadPreviewFontTask(getContext(), image);
//        currentTask.setOnCompleteHandler(new DownloadPreviewFontTask.OnCompleteHandler() {
//            @Override
//            public void onComplete(Typeface typeface, String filePath) {
//                if (typeface != null) {
//                    setTypeface(typeface);
//                }
//
//            }
//        });
//        // Run the task in a threadpool
//        threadPool.execute(currentTask);
//    }

    public static void cancelAllTasks() {
        threadPool.shutdownNow();
        threadPool = Executors.newFixedThreadPool(LOADING_THREADS);
    }

//    static class DownloadPreviewFontTask implements Runnable {
//        private static final int FONT_READY = 0;
//
//        private boolean cancelled = false;
//        private OnCompleteHandler onCompleteHandler;
//        private PreviewFont font;
//        private Context context;
//
//        public static class OnCompleteHandler extends Handler {
//            @Override
//            public void handleMessage(Message msg) {
//                Typeface typeface = (Typeface) msg.obj;
//                Bundle bundle = msg.getData();
//                onComplete(typeface, bundle.getString("cachePath"));
//            }
//
//            public void onComplete(Typeface typeface, String filePath) {
//            };
//        }
//
//        public abstract static class OnCompleteListener {
//            public abstract void onComplete(String cachePath);
//        }
//
//        public DownloadPreviewFontTask(Context context, PreviewFont font) {
//            this.font = font;
//            this.context = context;
//        }
//
//        @Override
//        public void run() {
//            if (font != null) {
//                complete(font.getTypeface(context));
//                context = null;
//            }
//        }
//
//        public void setOnCompleteHandler(OnCompleteHandler handler) {
//            this.onCompleteHandler = handler;
//        }
//
//        public void cancel() {
//            cancelled = true;
//        }
//
//        public void complete(Typeface typeface) {
//            if (onCompleteHandler != null && !cancelled) {
//                Message msg = onCompleteHandler.obtainMessage(FONT_READY, typeface);
//                Bundle bundle = new Bundle();
//                bundle.putString("cachePath", font.getCachePath());
//                msg.setData(bundle);
//                onCompleteHandler.sendMessage(msg);
//            }
//        }
//
//    }

}


package com.koushikdutta.ion.font;

import android.graphics.Typeface;
import android.util.Log;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.future.SimpleFuture;
import com.koushikdutta.async.future.TransformFuture;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.util.FileCache;
import com.koushikdutta.async.util.StreamUtility;
import com.koushikdutta.ion.InputStreamParser;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.IonRequestBuilder;
import com.koushikdutta.ion.L;
import com.koushikdutta.ion.Loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TypefaceFetcher implements IonRequestBuilder.LoadRequestCallback {
    String downloadKey;
    public String typefaceKey;
    TypefaceInfo info;
    IonRequestBuilder builder;

    private boolean fastLoad(String uri) {
        Ion ion = builder.ion;

        for (Loader loader : ion.configure().getLoaders()) {
            Future<TypefaceInfo> future = loader.loadTypeface(builder.contextReference.getContext(), ion, downloadKey, uri);

            L.d("loader .... " + future);
            if (future != null) {
                final LoadTypefaceBase callback = new LoadTypefaceBase(ion, downloadKey, true);
                future.setCallback(new FutureCallback<TypefaceInfo>() {
                    @Override
                    public void onCompleted(Exception e, TypefaceInfo result) {
                        L.d("report....");
                        callback.report(e, result);
                    }
                });
                return true;
            }
        }
        return false;
    }

    public static final int MAX_TYPEFACE_LOAD = 2;

    public static boolean shouldDeferTextView(Ion ion) {
        int size = ion.typefacesPending.keySet().size();
        L.d("shouldDeferTextView ... "+size);
        if (size <= MAX_TYPEFACE_LOAD)
            return false;
        int loadCount = 0;
        for (String key : ion.typefacesPending.keySet()) {
            Object owner = ion.typefacesPending.tag(key);
            if (owner instanceof LoadTypefaceBase) {
                loadCount++;
                if (loadCount > MAX_TYPEFACE_LOAD)
                    return true;
            }
        }
        return false;
    }

    public DeferredLoadTypeface defer() {
        DeferredLoadTypeface ret = new DeferredLoadTypeface(builder.ion, downloadKey, this);
        return ret;
    }

    @Override
    public boolean loadRequest(AsyncHttpRequest request) {
        return !fastLoad(request.getUri().toString());
    }

    public static void getTypefaceFromFile(final Ion ion, final String transformKey, final String file) {
        // don't do this if this is already loading
        if (ion.typefacesPending.tag(transformKey) != null)
            return;
        final TypefaceCallback callback = new TypefaceCallback(ion, transformKey, true);
        Ion.getBitmapLoadExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                if (ion.typefacesPending.tag(transformKey) != callback) {
                    Log.d("IonTypefaceLoader", "Typeface cache load cancelled (no longer needed)");
                    return;
                }

                try {
                    L.d("cache file -- " + file);
                    try {
                        Typeface typeface = Typeface.createFromFile(file);
                        TypefaceInfo info = new TypefaceInfo(transformKey, typeface, (int) new File(file).length());
                        info.loadedFrom = Loader.LoaderEmitter.LOADED_FROM_CACHE;
                        callback.report(null, info);
                    } catch (Exception e) {
                        callback.report(e, null);
                    } finally {
                    }
                } catch (OutOfMemoryError e) {
                    callback.report(new Exception(e), null);
                } catch (Exception e) {
                    callback.report(e, null);
                    try {
                        ion.getCache().remove(transformKey);
                    } catch (Exception ex) {
                    }
                }
            }
        });
    }

    public static void getTypefaceFromHttpCache(final Ion ion, final String transformKey, final File ttf) {
        // don't do this if this is already loading
        if (ion.typefacesPending.tag(transformKey) != null)
            return;
        final TypefaceCallback callback = new TypefaceCallback(ion, transformKey, true);
        Ion.getBitmapLoadExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                if (ion.typefacesPending.tag(transformKey) != callback) {
                     Log.d("IonTypefaceLoader", "Typeface cache load cancelled (no longer needed)");
                    return;
                }

                try {
                    File file = ion.getCache().getFile(transformKey);
                    L.d("cache file " + file.getAbsolutePath());
                    
                  if (file.exists()) {

                      ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
                      try {
                          ZipEntry ze = null;
                          while ((ze = zis.getNextEntry()) != null) {
                              if (ze.getName().endsWith(".ttf")) {
                                  FileOutputStream fout = new FileOutputStream(ttf, false);
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
                    
                    Typeface bitmap = IonTypefaceCache.loadTypeface(ttf);
                    if (bitmap == null)
                        throw new Exception("Bitmap failed to load");
                    TypefaceInfo info = new TypefaceInfo(transformKey, bitmap, (int) file.length());
                    info.loadedFrom = Loader.LoaderEmitter.LOADED_FROM_CACHE;
                    //ttf.delete();//TODO 为了避免占用太多空间， 生成typeface后再删除预览文件
                    // 提供空间管理界面，让用户可以选择删除 预览字体
                    callback.report(null, info);
                } catch (OutOfMemoryError e) {
                    callback.report(new Exception(e), null);
                } catch (Exception e) {
                    callback.report(e, null);
                    try {
                        ion.getCache().remove(transformKey);
                    } catch (Exception ex) {
                    }
                }
            }
        });
    }

    public void execute() {
        final Ion ion = builder.ion;

        final File file = getFileFromUri(builder.uri);
        final File ttf = new File(file.getAbsolutePath().replace(".zip", ".ttf"));
        if (!builder.noCache && (/*file.exists() ||*/ ttf.exists())) {
            L.d("get font from file " + ttf.getAbsolutePath());
            getTypefaceFromFile(ion, typefaceKey, ttf.getAbsolutePath());
            return;
        }

        // bitmaps that were transformed are put into the FileCache to prevent
        // subsequent retransformation. See if we can retrieve the bitmap from
        // the disk cache.
        // See TransformBitmap for where the cache is populated.
        FileCache fileCache = ion.getCache();
        L.d("cache..............||| " + fileCache);
        if (!builder.noCache && fileCache.exists(typefaceKey)) {
            getTypefaceFromHttpCache(ion, typefaceKey, ttf);
            return;
        }

        L.d("cache..............11" + fileCache);
        // Perform a download as necessary.
        if (ion.typefacesPending.tag(downloadKey) == null && !fastLoad(builder.uri)) {
            builder.setHandler(null);
            builder.loadRequestCallback = this;

            L.d("http excute...." + file.getAbsolutePath());
            L.d("http excute uri --- ...." +builder.uri);

            SimpleFuture<File> ff = builder.execute(new InputStreamParser(), new Runnable() {
                @Override
                public void run() {
                    AsyncServer.post(Ion.mainHandler, new Runnable() {
                        @Override
                        public void run() {
                            ion.typefacesPending.remove(downloadKey);
                        }
                    });
                }
            }).then(new TransformFuture<File, InputStream>() {

                @Override
                protected void transform(InputStream result) throws Exception {
                    try {
                        L.d("------------===");
                        ZipInputStream zis = new ZipInputStream(result);
                        try {
                            ZipEntry ze = null;
                            while ((ze = zis.getNextEntry()) != null) {
                                if (ze.getName().endsWith(".ttf")) {
                                    FileOutputStream fout = new FileOutputStream(ttf, false);
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

                        Typeface typeface = Typeface.createFromFile(ttf);
                        L.d("typeface.... " + typeface);
                        setComplete(null, ttf);
                    } catch (Exception e) {
                        L.d("----------------");
                        setComplete(e, null);
                    } finally {
                    }

                }
            });

            L.d("set .. LoadTypeface  callback...");
            ff.setCallback(new LoadTypeface(ion, downloadKey, true, null));
        }

    }

    private File getFileFromUri(String uri) {
        int index = uri.lastIndexOf("/");
        String name = uri.substring(index, uri.length());
        File dir = builder.contextReference.getContext().getFilesDir();
        dir = new File(dir, "prettf");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, name);
    }
}

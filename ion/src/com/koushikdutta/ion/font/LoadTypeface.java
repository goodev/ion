
package com.koushikdutta.ion.font;

import android.graphics.Typeface;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.IonRequestBuilder;
import com.koushikdutta.ion.L;
import com.koushikdutta.ion.Loader;

import java.io.File;

public class LoadTypeface extends /* LoadTypefaceEmitter */LoadTypefaceBase implements FutureCallback<File> {

    public LoadTypeface(Ion ion, String urlKey, boolean put, IonRequestBuilder.EmitterTransform<File> emitterTransform) {
        // super(ion, urlKey, put, emitterTransform);
        // this.emitterTransform = emitterTransform;
        super(ion, urlKey, put);
    }

    @Override
    public void onCompleted(Exception e, final File result) {
        L.d("load file completed..." + e + "  " + result);
        if (e != null) {
            report(e, null);
            return;
        }

        if (ion.typefacesPending.tag(key) != this) {
            return;
        }

        Ion.getBitmapLoadExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ion.typefacesPending.tag(key) != LoadTypeface.this) {
                        return;
                    }
                    Typeface typeface = Typeface.createFromFile(result);
                    TypefaceInfo info = new TypefaceInfo(key, typeface, (int) result.length());
                    info.loadedFrom = Loader.LoaderEmitter.LOADED_FROM_NETWORK;
                    report(null, info);
                } catch (Exception e) {
                    report(e, null);
                } finally {
                }
            }
        });
    }
}

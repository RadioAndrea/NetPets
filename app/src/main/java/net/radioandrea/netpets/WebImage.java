package net.radioandrea.netpets;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebImage extends ImageView {

    private Drawable mPlaceholder, mImage;

    //very dangerous, if DownloadActivity is destroyed while the downloadTask is running
    //below then a reference to the DownloadActivity is held by the below member var.
    //THe GC will NOT collect the DownloadActivity until the downloadTask exits and the
    //reference to DownloadActivity is released.  In other words a memory leak.  Very bad
    //on a mobile device. Better to release and reaquire the context when the activity is being destroyed.
    // see Asynctask_OnRetainNonConfigurationInstance project for how to do this
    private NetPets myContext;

    public WebImage(NetPets context) {
        super(context, null, 0);
        this.myContext = context;
    }

    public void setImageUrl(String url) {
        DownloadTask task = new DownloadTask();
        task.execute(url);
    }

    private class DownloadTask extends AsyncTask<String, Void, Bitmap> {
        private static final String TAG = "DownloadTask";
        private static final int DEFAULTBUFFERSIZE = 50;
        private static final int NODATA = -1;

        @Override
        protected Bitmap doInBackground(String... params) {

            // site we want to connect to
            String url = params[0];

            // note streams are left willy-nilly here because it declutters the
            // example
            try {
                URL url1 = new URL(url);

                // this does no network IO
                HttpURLConnection connection = (HttpURLConnection) url1.openConnection();

                // can further configure connection before getting data
                // cannot do this after connected
                // connection.setRequestMethod("GET");
                // connection.setReadTimeout(timeoutMillis);
                // connection.setConnectTimeout(timeoutMillis);

                // this opens a connection, then sends GET & headers
                connection.connect();

                // lets see what we got make sure its one of
                // the 200 codes (there can be 100 of them
                // http_status / 100 != 2 does integer div any 200 code will = 2
                int statusCode = connection.getResponseCode();
                if (statusCode / 100 != 2) {
                    Log.e(TAG, "Error-connection.getResponseCode returned "
                            + Integer.toString(statusCode));
                    return null;
                }

                // get our streams, a more concise implementation is
                // BufferedInputStream bis = new
                // BufferedInputStream(connection.getInputStream());
                InputStream is = connection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                // the following buffer will grow as needed
                ByteArrayBuffer baf = new ByteArrayBuffer(DEFAULTBUFFERSIZE);
                int current = 0;

                // wrap in finally so that stream bis is sure to close
                try {
                    while ((current = bis.read()) != NODATA) {
                        baf.append((byte) current);
                    }

                    // convert to a bitmap
                    byte[] imageData = baf.toByteArray();
                    return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                } finally {
                    // close resource no matter what exception occurs
                    bis.close();
                }
            } catch (Exception exc) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mImage = new BitmapDrawable(result);
            if (mImage != null) {
                setImageDrawable(mImage);
            }

            // the only place where we will need the context
            // we have not checked for null yet
           // if (myContext != null)
            //    myContext.enableProgressBar(false);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.os.AsyncTask#onCancelled()
         */
        @Override
        protected void onCancelled() {
            super.onCancelled();

            // the only place where we will need the context
            //if (myContext != null)
            //    myContext.enableProgressBar(false);
        }
    };

    /**
     * default image to show if we cannot load desired one
     *
     * @param drawable
     */
    public void setPlaceholderImage(Drawable drawable) {
        // error check
        if (drawable != null) {
            mPlaceholder = drawable;
            if (mImage == null) {
                setImageDrawable(mPlaceholder);
            }
        }
    }

    /**
     * get default from resources
     *
     * @param resid
     */
    public void setPlaceholderImage(int resid) {
        mPlaceholder = getResources().getDrawable(resid);
        if (mImage == null) {
            setImageDrawable(mPlaceholder);
        }
    }
}

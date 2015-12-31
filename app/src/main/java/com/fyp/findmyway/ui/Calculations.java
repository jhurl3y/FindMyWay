package com.fyp.findmyway.ui;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.model.LatLng;

/**
 * A Utility Service for doing various calculations
 * and background operations.
 */
public class Calculations {

    /**
     * Offsets a location in metres
     * @param location location to offset
     * @param lat_offset latitude to offset in metres
     * @param long_offset longitude to offset in metres
     * @return new offset location
     */
    public static LatLng calcLatLngOffset(LatLng location, double lat_offset, double long_offset) {
        lat_offset = 1.0/111111.0*lat_offset;
        long_offset = 1.0/111111.0*Math.cos(location.latitude)*long_offset;

        return new LatLng(location.latitude + lat_offset, location.longitude + long_offset);
    }

    /**
     * Method to scale an ImageView..
     * https://argillander.wordpress.com/2011/11/24/scale-image-into-imageview-then-resize-imageview-to-match-the-image/
     * @param view ImageView to scale (Relative Layout)
     * @param boundBoxInDp size to scale (dp)
     */
    public static void scaleImage(ImageView view, int boundBoxInDp)
    {
        // Get the ImageView and its bitmap
        Drawable drawing = view.getDrawable();
        Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

        // Get current dimensions
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) boundBoxInDp) / width;
        float yScale = ((float) boundBoxInDp) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);
        width = scaledBitmap.getWidth();
        height = scaledBitmap.getHeight();

        // Apply the scaled bitmap
        view.setImageDrawable(result);

        // Now change ImageView's dimensions to match the scaled image
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }

}

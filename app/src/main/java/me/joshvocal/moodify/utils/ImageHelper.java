package me.joshvocal.moodify.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.microsoft.projectoxford.emotion.contract.FaceRectangle;

/**
 * Created by josh on 8/15/17.
 */

public class ImageHelper {

    private static final int STROKE_WIDTH = 35;

    public static Bitmap drawRectangleOnBitmap(
            Bitmap mBitmap, FaceRectangle faceRectangle) {

        Bitmap bitmap = mBitmap.copy(Bitmap.Config.RGB_565, true);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(STROKE_WIDTH);

        canvas.drawRect(faceRectangle.left,
                faceRectangle.top,
                faceRectangle.left + faceRectangle.width,
                faceRectangle.top + faceRectangle.height,
                paint);

        return bitmap;
    }

}

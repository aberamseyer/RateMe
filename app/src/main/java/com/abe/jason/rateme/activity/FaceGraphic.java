/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.abe.jason.rateme.activity;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Rating;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.abe.jason.rateme.R;
import com.abe.jason.rateme.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.face.Face;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final String TAG = "FaceGraphic.java";

    private static final float ID_TEXT_SIZE = 40.0f;
//    private static final float ID_Y_OFFSET = 50.0f;
//    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;
    boolean inBounds = false;
    static String info = "";
    static String name = "";
    static float rating = 0.0f;
    private volatile Face mFace;
    private Context context;
    LayoutInflater mInflater;


    FaceGraphic(GraphicOverlay overlay, Context context) {
        super(overlay);
        this.context = context;
        final int selectedColor = Color.YELLOW;

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

//        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
//        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
//        canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);
//        canvas.drawText("id: " + info, x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
//        canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
//        canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);
        canvas.drawText(info, left + 10, bottom - 20, mIdPaint);


        //fancy info overlay-------------------------------------------

        //Set a Rect for the 200 x 200 px center of a 400 x 400 px area
        Rect rect = new Rect();
        rect.set((int)left, (int)bottom, (int)right, (int)top+300);

        //Allocate a new Bitmap to draw into
        Bitmap bitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888);
        canvas.drawBitmap(bitmap, rect, rect, null );

        //inflate the main layout, aka the root from info_overlay
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.info_overlay, null);

        //accessing layout children
        RatingBar ratingBar = (RatingBar)v.findViewById(R.id.ratingBar);
        ratingBar.setRating(rating);
        TextView userName = (TextView)v.findViewById(R.id.name);
        userName.setText(name);

        v.measure(View.MeasureSpec.getSize(v.getMeasuredWidth()), View.MeasureSpec.getSize(v.getMeasuredHeight()));
        v.layout(0, 0, rect.width(), rect.height());

        canvas.save();
        canvas.translate(rect.left, rect.top);
        v.draw(canvas);
        canvas.restore();

        //the rendering of the view into the bitmap
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageBitmap(bitmap);

        // determine if the box is entirely on the device's screen
        Rect bounds = canvas.getClipBounds();
        if(left >= 0 && top >= 0) {
            if(!inBounds && right < bounds.right && bottom < bounds.bottom) {
                inBounds = true;
            }
        }
    }
}

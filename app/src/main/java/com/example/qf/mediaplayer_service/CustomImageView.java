package com.example.qf.mediaplayer_service;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by qf on 2016/9/24.
 */
public class CustomImageView extends ImageView {
    private Paint paint,paint2,paint3;
    private int shape;
    private final static int CIRCLE=1;
    public CustomImageView(Context context) {
        this(context,null);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta=context.obtainStyledAttributes(attrs,R.styleable.CustomImageView);
        shape=ta.getInt(R.styleable.CustomImageView_shape,CIRCLE);
        ta.recycle();
        paint=new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint2=new Paint();
        paint2.setAntiAlias(true);
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(180);
        paint2.setColor(getResources().getColor(R.color.colorjz1));

        paint3=new Paint();
        paint3.setAntiAlias(true);
        paint3.setStyle(Paint.Style.STROKE);
        paint3.setStrokeWidth(20);
        paint3.setColor(getResources().getColor(R.color.colorjz2));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        Drawable drawable=getDrawable();
        if (drawable==null){return;}
        paint.reset();
        Bitmap srcBitmap=((BitmapDrawable)drawable).getBitmap();
        int measureWidth=getMeasuredWidth();
        int measureHeight=getMeasuredHeight();
        int width=srcBitmap.getWidth();
        int height=srcBitmap.getHeight();
        Matrix matrix=new Matrix();
        float scale=Math.max(measureWidth*1f/width,measureHeight*1f/height);
        matrix.postScale(scale,scale);
        Bitmap bitmap=Bitmap.createBitmap(srcBitmap,0,0,width,height,matrix,true);
        Bitmap blankBitmap=Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas mCanvas=new Canvas(blankBitmap);

        canvas.drawCircle(getWidth()/2,getHeight()/2,getWidth()/2-125,paint2);
        canvas.drawCircle(getWidth()/2,getHeight()/2,getWidth()/2-30,paint3);
        mCanvas.drawCircle(measureWidth/2,measureHeight/2,measureWidth/2-135,paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        mCanvas.drawBitmap(bitmap,0,0,paint);
        canvas.drawBitmap(blankBitmap,0,0,null);
        if (blankBitmap!=null&&!blankBitmap.isRecycled()){
            blankBitmap.recycle();
        }
        if (bitmap!=null&&!bitmap.isRecycled()){
            bitmap.recycle();
        }
    }
}

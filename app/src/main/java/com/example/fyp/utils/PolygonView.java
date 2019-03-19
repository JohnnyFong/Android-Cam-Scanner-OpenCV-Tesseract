package com.example.fyp.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.fyp.R;

public class PolygonView extends FrameLayout{

    protected Context context;
    private Paint paint;
    private ImageView ptr1, ptr2, ptr3, ptr4; //pointers for all 4 corners
    private ImageView mPtr13, mPtr12, mPtr34, mPtr24; // pointers between the corner
    private PolygonView polygonView;
    private boolean validation = true;//validate shape

    public PolygonView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public PolygonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public PolygonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        polygonView = this;
        ptr1 = getImageView(0, 0);
        ptr2 = getImageView(getWidth(), 0);
        ptr3 = getImageView(0, getHeight());
        ptr4 = getImageView(getWidth(), getHeight());

        mPtr13 = getImageView(0, getHeight() / 2);
        mPtr13.setOnTouchListener(new MidPointTouchListenerImpl(ptr1, ptr3));

        mPtr12 = getImageView(0, getWidth() / 2);
        mPtr12.setOnTouchListener(new MidPointTouchListenerImpl(ptr1, ptr2));

        mPtr34 = getImageView(0, getHeight() / 2);
        mPtr34.setOnTouchListener(new MidPointTouchListenerImpl(ptr3, ptr4));

        mPtr24 = getImageView(0, getHeight() / 2);
        mPtr24.setOnTouchListener(new MidPointTouchListenerImpl(ptr2, ptr4));

        addView(ptr1);
        addView(ptr2);
        addView(mPtr13);
        addView(mPtr12);
        addView(mPtr34);
        addView(mPtr24);
        addView(ptr3);
        addView(ptr4);
        initPaint();
    }

    @Override
    protected void attachViewToParent(View child, int index, ViewGroup.LayoutParams params) {
        super.attachViewToParent(child, index, params);
    }

    private void initPaint() {
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.blue));
        paint.setStrokeWidth(2);
        paint.setAntiAlias(true);
    }

    public Map<Integer, PointF> getPoints() {

        List<PointF> points = new ArrayList<PointF>();
        points.add(new PointF(ptr1.getX(), ptr1.getY()));
        points.add(new PointF(ptr2.getX(), ptr2.getY()));
        points.add(new PointF(ptr3.getX(), ptr3.getY()));
        points.add(new PointF(ptr4.getX(), ptr4.getY()));

        return getOrderedPoints(points);
    }

    public Map<Integer, PointF> getOrderedPoints(List<PointF> points) {

        PointF centerPoint = new PointF();
        int size = points.size();
        for (PointF pointF : points) {
            centerPoint.x += pointF.x / size;
            centerPoint.y += pointF.y / size;
        }
        Map<Integer, PointF> orderedPoints = new HashMap<>();
        for (PointF pointF : points) {
            int index = -1;
            if (pointF.x < centerPoint.x && pointF.y < centerPoint.y) {
                index = 0;
            } else if (pointF.x > centerPoint.x && pointF.y < centerPoint.y) {
                index = 1;
            } else if (pointF.x < centerPoint.x && pointF.y > centerPoint.y) {
                index = 2;
            } else if (pointF.x > centerPoint.x && pointF.y > centerPoint.y) {
                index = 3;
            }
            orderedPoints.put(index, pointF);
        }
        return orderedPoints;
    }

    public void setPoints(Map<Integer, PointF> pointFMap) {
        if (pointFMap.size() == 4) {
            setPointsCoordinates(pointFMap);
        }
    }

    private void setPointsCoordinates(Map<Integer, PointF> pointFMap) {
        ptr1.setX(pointFMap.get(0).x);
        ptr1.setY(pointFMap.get(0).y);

        ptr2.setX(pointFMap.get(1).x);
        ptr2.setY(pointFMap.get(1).y);

        ptr3.setX(pointFMap.get(2).x);
        ptr3.setY(pointFMap.get(2).y);

        ptr4.setX(pointFMap.get(3).x);
        ptr4.setY(pointFMap.get(3).y);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.drawLine(ptr1.getX() + (ptr1.getWidth() / 1), ptr1.getY() + (ptr1.getHeight() / 2), ptr3.getX() + (ptr3.getWidth() / 2), ptr3.getY() + (ptr3.getHeight() / 2), paint);
        canvas.drawLine(ptr1.getX() + (ptr1.getWidth() / 2), ptr1.getY() + (ptr1.getHeight() / 2), ptr2.getX() + (ptr2.getWidth() / 2), ptr2.getY() + (ptr2.getHeight() / 2), paint);
        canvas.drawLine(ptr2.getX() + (ptr2.getWidth() / 2), ptr2.getY() + (ptr2.getHeight() / 2), ptr4.getX() + (ptr4.getWidth() / 2), ptr4.getY() + (ptr4.getHeight() / 2), paint);
        canvas.drawLine(ptr3.getX() + (ptr3.getWidth() / 2), ptr3.getY() + (ptr3.getHeight() / 2), ptr4.getX() + (ptr4.getWidth() / 2), ptr4.getY() + (ptr4.getHeight() / 2), paint);
        mPtr13.setX(ptr3.getX() - ((ptr3.getX() - ptr1.getX()) / 2));
        mPtr13.setY(ptr3.getY() - ((ptr3.getY() - ptr1.getY()) / 2));
        mPtr24.setX(ptr4.getX() - ((ptr4.getX() - ptr2.getX()) / 2));
        mPtr24.setY(ptr4.getY() - ((ptr4.getY() - ptr2.getY()) / 2));
        mPtr34.setX(ptr4.getX() - ((ptr4.getX() - ptr3.getX()) / 2));
        mPtr34.setY(ptr4.getY() - ((ptr4.getY() - ptr3.getY()) / 2));
        mPtr12.setX(ptr2.getX() - ((ptr2.getX() - ptr1.getX()) / 2));
        mPtr12.setY(ptr2.getY() - ((ptr2.getY() - ptr1.getY()) / 2));
    }

    private ImageView getImageView(int x, int y) {
        ImageView imageView = new ImageView(context);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(layoutParams);
        imageView.setImageResource(R.drawable.circle);
        imageView.setX(x);
        imageView.setY(y);
        imageView.setOnTouchListener(new TouchListenerImpl());
        return imageView;
    }

    private class MidPointTouchListenerImpl implements OnTouchListener {

        PointF DownPT = new PointF(); // Record Mouse Position When Pressed Down
        PointF StartPT = new PointF(); // Record Start Position of 'img'

        private ImageView mainPointer1;
        private ImageView mainPointer2;

        public MidPointTouchListenerImpl(ImageView mainPointer1, ImageView mainPointer2) {
            this.mainPointer1 = mainPointer1;
            this.mainPointer2 = mainPointer2;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int eid = event.getAction();
            switch (eid) {
                case MotionEvent.ACTION_MOVE:
                    PointF mv = new PointF(event.getX() - DownPT.x, event.getY() - DownPT.y);

                    if (Math.abs(mainPointer1.getX() - mainPointer2.getX()) > Math.abs(mainPointer1.getY() - mainPointer2.getY())) {
                        if (((mainPointer2.getY() + mv.y + v.getHeight() < polygonView.getHeight()) && (mainPointer2.getY() + mv.y > 0))) {
                            v.setX((int) (StartPT.y + mv.y));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer2.setY((int) (mainPointer2.getY() + mv.y));
                        }
                        if (((mainPointer1.getY() + mv.y + v.getHeight() < polygonView.getHeight()) && (mainPointer1.getY() + mv.y > 0))) {
                            v.setX((int) (StartPT.y + mv.y));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer1.setY((int) (mainPointer1.getY() + mv.y));
                        }
                    } else {
                        if ((mainPointer2.getX() + mv.x + v.getWidth() < polygonView.getWidth()) && (mainPointer2.getX() + mv.x > 0)) {
                            v.setX((int) (StartPT.x + mv.x));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer2.setX((int) (mainPointer2.getX() + mv.x));
                        }
                        if ((mainPointer1.getX() + mv.x + v.getWidth() < polygonView.getWidth()) && (mainPointer1.getX() + mv.x > 0)) {
                            v.setX((int) (StartPT.x + mv.x));
                            StartPT = new PointF(v.getX(), v.getY());
                            mainPointer1.setX((int) (mainPointer1.getX() + mv.x));
                        }
                    }

                    break;
                case MotionEvent.ACTION_DOWN:
                    DownPT.x = event.getX();
                    DownPT.y = event.getY();
                    StartPT = new PointF(v.getX(), v.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    int color = 0;
                    if (isValidShape(getPoints())) {
                        color = getResources().getColor(R.color.blue);
                        validation = true;
                        //btn.setEnabled(true);
                    } else {
                        color = getResources().getColor(R.color.red);
                        validation = false;
                        //btn.setEnabled(false);
                    }
                    paint.setColor(color);
                    break;
                default:
                    break;
            }
            polygonView.invalidate();
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public boolean isValidShape(Map<Integer, PointF> pointFMap) {
        return pointFMap.size() == 4;
    }

    private class TouchListenerImpl implements OnTouchListener {

        PointF DownPT = new PointF(); // Record Mouse Position When Pressed Down
        PointF StartPT = new PointF(); // Record Start Position of 'img'

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int eid = event.getAction();
            switch (eid) {
                case MotionEvent.ACTION_MOVE:
                    PointF mv = new PointF(event.getX() - DownPT.x, event.getY() - DownPT.y);
                    if (((StartPT.x + mv.x + v.getWidth()) < polygonView.getWidth() && (StartPT.y + mv.y + v.getHeight() < polygonView.getHeight())) && ((StartPT.x + mv.x) > 0 && StartPT.y + mv.y > 0)) {
                        v.setX((int) (StartPT.x + mv.x));
                        v.setY((int) (StartPT.y + mv.y));
                        StartPT = new PointF(v.getX(), v.getY());
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    DownPT.x = event.getX();
                    DownPT.y = event.getY();
                    StartPT = new PointF(v.getX(), v.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    int color = 0;
                    if (isValidShape(getPoints())) {
                        color = getResources().getColor(R.color.blue);
                        validation = true;
                    } else {
                        color = getResources().getColor(R.color.red);
                        validation = false;
                    }
                    paint.setColor(color);
                    break;
                default:
                    break;
            }
            polygonView.invalidate();
            return true;
        }
    }

    public boolean shapeValidate(){
        return validation;
    }


}

package com.example.navigation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class DrawView extends View {
    Boolean isFirst = true;
    int width = 1080;
    int height = 1704;

    int middleX = width / 2;
    int middleY = height / 2;

    /*
    int middleX = width - 100;
    int middleY = height - 200;
    */

    int pointX = middleX;
    int pointY = middleY;

    int preX = pointX;
    int preY = pointY;

    double x = 0.0;
    double y = 0.0;

    Paint blackPaint = new Paint();
    Paint redPaint = new Paint();
    Paint textPaint = new Paint();
    Paint finalPaint = new Paint();

    String result = "";
    Boolean showFlag = true;
    int count = 0;

    private void init() {
        MainActivity.result_list.add("0,0");

        blackPaint.setColor(Color.BLACK);
        blackPaint.setStrokeWidth(5f);

        redPaint.setColor(Color.RED);
        redPaint.setStrokeWidth(5f);

        textPaint.setARGB(255, 0, 0, 0);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(48);
    }

    public DrawView(Context context) {
        super(context);
        init();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (isFirst) {
            Log.e("[Activity]", "DrawView");
            //Log.e("[DrawView]", canvas.getWidth() + " " + canvas.getHeight());
            canvas.drawPoint(middleX, middleY, blackPaint);
            canvas.drawCircle(middleX, middleY, 10, blackPaint);
            isFirst = false;
        }
        else {
            //Log.e("[DrawView]", " ");
            ArrayList<String> result_list = MainActivity.result_list;
            int distance;
            int angular;
            int preDistance = 0;
            int preAngular = 0;
            preX = pointX = middleX;
            preY = pointY = middleY;
            //Log.e("[DrawView]", result_list + " ");

            for (int i = 0; i < result_list.size(); i++) {
                result = result_list.get(i);
                //Log.e("[DrawView]", result + " ");

                try {
                    String[] point = result.split(",");

                     distance = Integer.parseInt(point[0]);
                     angular = Integer.parseInt(point[1]);
                    //Log.d("[DrawView]", distance + " ----- " + angular);
                } catch (NumberFormatException e) {
                    distance = preDistance;
                    angular = preAngular;
                }

                angular += preAngular;
                preAngular = angular;
                preDistance = distance;

                x = distance * Math.sin(Math.toRadians(angular));
                y = distance * Math.cos(Math.toRadians(angular));
                //Log.d("[DrawView]", x + " ----- " + y);

                pointX -= x;
                pointY -= y;
                //Log.d("[DrawView]", pointX + " ----- " + pointY);

                if (i != result_list.size() - 1) {
                    finalPaint = blackPaint;
                } else {
                    finalPaint = redPaint;
                }

                canvas.drawCircle(preX, preY, 10, blackPaint);
                canvas.drawCircle(pointX, pointY, 10, finalPaint);
                canvas.drawLine(preX, preY, pointX, pointY, finalPaint);

                preX = pointX;
                preY = pointY;
            }
        }
        String time = MainActivity.getTimeValue();
        if (!time.equals("")) {
            canvas.drawText(time, width / 3 - 70, 100, textPaint);
            if (showFlag) {
                if (!MainActivity.getCondition()) {
                    canvas.drawText("Connecting...", width / 2 - 70, 200, textPaint);
                } else {
                    count += 1;
                    if (count == 20)
                        showFlag = false;
                    else if (count >= 10)
                        canvas.drawText("Start !!!", width / 2 - 70, 200, textPaint);
                    else
                        canvas.drawText("Connecting...", width / 2 - 70, 200, textPaint);
                }
            }
        }
    }

    public void clear() {
        MainActivity.result_list.clear();
    }
}
package com.example.navigation;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends Activity implements SensorEventListener, View.OnClickListener {
    DrawView drawView;
    LinearLayout drawCanvas;
    Button clear, confirm;

    //static String serverIp = "140.134.26.135";
    static String serverIp = "140.134.26.137";
    //static String serverIp = "140.134.133.152";
    //static String serverIp = "192.168.51.215";
    static int serverPort = 5050;
    Socket clientSocket;	// 客戶端socket
    String bufRecv = "";
    String result = "";

    private SensorManager sensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    // 紀錄回應參數
    static String myResponse = "";

    // 記數參數
    private int countA = 0;
    private int countG = 0;

    // 時間參數
    int TimeA;
    int TimeG;
    int startTimeA;
    int startTimeG;
    int endTimeA;
    int endTimeG;
    String preTimeA = "";
    String preTimeG = "";
    String timerA = "";
    String timerG = "";

    // 其他時間參數
    //DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    long offset = TestActivity.offset;

    // 數據參數
    String strAx = "";
    String strAy = "";
    String strAz = "";
    String strGx = "";
    String strGy = "";
    String strGz = "";

    // 上傳參數
    String uploadText = "";
    String uploadTime = "";
    String uploadAx = "";
    String uploadAy = "";
    String uploadAz = "";
    String uploadGx = "";
    String uploadGy = "";
    String uploadGz = "";

    // 上傳權限參數
    int uploadA = 0;
    int uploadG = 0;

    //-------啟動權限--------//
    static Boolean startFlag = false;
    Boolean errorFlag = false;

    public static Handler handler = new Handler();
    int speed = 100;

    Queue<String> uploadTime_queue = new LinkedList<>();
    Queue<String> uploadAx_queue = new LinkedList<>();
    Queue<String> uploadAy_queue = new LinkedList<>();
    Queue<String> uploadAz_queue = new LinkedList<>();
    Queue<String> uploadGx_queue = new LinkedList<>();
    Queue<String> uploadGy_queue = new LinkedList<>();
    Queue<String> uploadGz_queue = new LinkedList<>();
    private static final Queue<String> time_queue = new LinkedList<>();
    static ArrayList<String> result_list = new ArrayList<>();

    static Boolean getCondition() {
        return startFlag;
    }

    static String getTimeValue() {
        if (!time_queue.isEmpty())
            return  time_queue.poll();
        else
            return "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("[Activity]", "MainActivity");
        super.onCreate(savedInstanceState);


        drawView = new DrawView(this);
        drawView.setBackgroundColor(Color.WHITE);
        //setContentView(drawView);
        setContentView(R.layout.activity_draw);

        clear = findViewById(R.id.clear);
        confirm = findViewById(R.id.confirm);
        drawCanvas = findViewById(R.id.draw_layout);

        clear.setOnClickListener(this);
        confirm.setOnClickListener(this);

        drawCanvas.addView(drawView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        //設定定時要執行的方法
        handler.removeCallbacks(updateTimer);
        //設定Delay的時間
        handler.postDelayed(updateTimer, speed);

        //執行連線
        new Thread(connect).start();
        //執行上傳
        new Thread(uploadData).start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.equals(mAccelerometer) && startFlag) {

            // 取得時間數據
            long timeA = System.currentTimeMillis() + offset;
            timerA = dfm.format(new Timestamp(timeA));
            //Log.d("[Time_Acc]", "EveryTimeA: " + timerA + "." + (timeA % 1000));

            if (!preTimeA.equals(timerA) && countA == 100) {
                if (startFlag) {
                    uploadTime_queue.offer(preTimeA);
                    uploadAx_queue.offer(strAx);
                    uploadAy_queue.offer(strAy);
                    uploadAz_queue.offer(strAz);
                }
                Log.d("[Time_upload]", "[uploadTime]: " + preTimeA);

                endTimeA = (int) (System.currentTimeMillis() + offset);
                TimeA = endTimeA - startTimeA;
                //Log.d("[Time_Acc]", "\t\tendTimeA: " + endTimeA);
                //Log.d("[Time_Acc]", "\t\tTimeA: " + TimeA);
                //Log.d("[Count_Acc]", "\tcountA All: " + countA);
                //Log.d("[Value_Acc]", "Acc - \nX: " + strAx + "\n" + "Y: " + strAy + "\n" + "Z: " + strAz + "\n");

                countA = 0;
                uploadA = 1;
                preTimeA = timerA;
            }

            if (countA == 0) {
                strAx = "";
                strAy = "";
                strAz = "";
                startTimeA = (int) (System.currentTimeMillis() + offset);
                //Log.d("[Time_Acc]", "\tstartTimeA: " + startTimeA);
            }

            countA++;
            //Log.d("[Count_Acc]", "\tcountA: " + countA);

            // 新方法
            strAx += event.values[0] + TestActivity.calibrationA[0] + ",";;
            strAy += event.values[1] + TestActivity.calibrationA[1] + ",";;
            strAz += event.values[2] + TestActivity.calibrationA[2] + ",";;
        }

        if (event.sensor.equals(mGyroscope) && startFlag) {
            // 取得時間數據
            long timeG = System.currentTimeMillis() + offset;
            timerG = dfm.format(new Timestamp(timeG));
            //Log.d("[Time_Gyr]", "EveryTimeG: " + timerG + "." + (timeG % 1000));

            if (!preTimeG.equals(timerG) && countG == 100) {
                if (startFlag) {
                    uploadGx_queue.offer(strGx);
                    uploadGy_queue.offer(strGy);
                    uploadGz_queue.offer(strGz);
                }

                endTimeG = (int) (System.currentTimeMillis() + offset);
                TimeG = endTimeG - startTimeG;
                //Log.d("[Time_Gyr]", "\t\tendTimeG: " + endTimeG);
                //Log.d("[Time_Gyr]", "\t\tTimeG: " + TimeG);
                //Log.d("[Count_Gyr]", "\tcountG All: " + countG);
                //Log.d("[Value_Gyr]", "Gyr - \nX: " + strGx + "\n" + "Y: " + strGy + "\n" + "Z: " + strGz + "\n");

                countG = 0;
                uploadG = 1;
                preTimeG = timerG;
            }

            if (countG == 0) {
                strGx = "";
                strGy = "";
                strGz = "";
                startTimeG = (int) (System.currentTimeMillis() + offset);
                //Log.d("[Time_Gyr]", "\tstartTimeG: " + startTimeG);
            }

            countG++;
            //Log.d("[Count_Gyr]", "\tcountG: " + countG);

            // 新方法
            strGx += event.values[0] + TestActivity.calibrationG[0] + ",";;
            strGy += event.values[1] + TestActivity.calibrationG[1] + ",";;
            strGz += event.values[2] + TestActivity.calibrationG[2] + ",";;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.confirm:
                startFlag = !startFlag;
                break;

            case R.id.clear:
                drawView.clear();
                break;
        }
    }

    private final Runnable connect = new Runnable() {
        public void run() {
            try {
                clientSocket = new Socket(serverIp, serverPort);
                Thread.sleep(1000);

                BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                if (clientSocket.isConnected()) {
                    String Test = "First";
                    clientSocket.getOutputStream().write(Test.getBytes());
                    bufRecv = br.readLine();
                    Log.e("[Test OK]", bufRecv);

                    if (bufRecv.equals("First OK"))
                        startFlag = true;


                    while (true) {
                        bufRecv = br.readLine();
                        //Log.e("[Test Recv]", bufRecv);

                        if (bufRecv.equals("Result") || bufRecv.contains("Result")) {
                            bufRecv = br.readLine();
                            result = bufRecv.split("Result")[0];
                            result_list.add(result);

                            Log.e("[Result]", result);
                        }
                        else
                            continue;
                    }

                }
            }
            catch (IOException ioe) {
                Log.e("[Exception]", "IOException");
                ioe.printStackTrace();
            }
            catch (Exception e) {
                Log.e("[Exception]", "Exception");
                e.printStackTrace();
            }
        }
    };

    private final Runnable updateTimer = new Runnable() {
        public void run() {
            // 取得時間
            long getTime = System.currentTimeMillis();

            // 校正時間點對齊
            getTime += offset;

            // 取得時間數據
            String showTime = dfm.format(new Timestamp(getTime));
            time_queue.offer(showTime);
            drawView.postInvalidate();
            Log.d("[Time_show]", "<showTime>" + showTime);

            //queue.setText("Queue size: " + uploadTime_queue.size());

            handler.postDelayed(this, speed);
        }
    };

    private final Runnable uploadData = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (uploadA == 1 && uploadG == 1 && startFlag) {
                    uploadA = 0;
                    uploadG = 0;
                    Log.d("[Upload]", "<SEND!!!!!>");

                    // 取出佇列數據
                    uploadTime = uploadTime_queue.poll();
                    uploadAx = uploadAx_queue.poll();
                    uploadAy = uploadAy_queue.poll();
                    uploadAz = uploadAz_queue.poll();
                    uploadGx = uploadGx_queue.poll();
                    uploadGy = uploadGy_queue.poll();
                    uploadGz = uploadGz_queue.poll();

                    if (!uploadTime.equals("") && uploadAx != null && uploadGx != null) {
                        uploadText = uploadTime + ';' + uploadAx + ';' + uploadAy + ';' + uploadAz +
                                ';' + uploadGx + ';' + uploadGy + ';' + uploadGz + ';';

                        Log.d("[Upload]", "<Time> " + uploadTime);
                        //Log.d("[Upload]", "<URL> " + uploadAll);

                        try {
                            if (clientSocket.isConnected()) {
                                String Send = "Send";
                                clientSocket.getOutputStream().write(Send.getBytes());
                                /*
                                bufRecv = br.readLine();
                                Log.e("[Test OK]", bufRecv);
                                */

                                clientSocket.getOutputStream().write(uploadText.getBytes());

                                /*
                                //bufRecv = br.readLine();
                                Log.e("[Test OK]", bufRecv);
                                */
                            }
                        } catch (SocketException e) {
                            System.exit(0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        /*
                        Request request = new Request.Builder()
                                .url(url)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                errorFlag = true;
                                myResponse = "<html><i>" + e.getMessage() + "</i></html>";
                                Log.d("[Upload]", "<Error> " + e.getMessage());
                                Log.e("[ERROR]!!!!!", "Failure", e);
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                myResponse = response.body().string();
                                if (response.isSuccessful()) {
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            MainActivity.this.response.setText("uploading...");
                                            upload.setText(myResponse);
                                        }
                                    });
                                    Log.d("[Upload]", "<Success> " + myResponse);
                                } else {
                                    errorFlag = true;
                                    Log.d("[Upload]", "<NotSuccess>");
                                    Log.e("[ERROR]!!!!!", "NotSuccess");
                                }
                            }
                        });

                         */

                        // 當傳送錯誤時轉跳頁面
                        if (errorFlag) {
                            errorFlag = false;
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this, TestActivity.class);
                            startActivity(intent);
                        }
                    }
                }
            }
        }
    };
}
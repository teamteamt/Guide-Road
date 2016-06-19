package com.example.guiderd;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

public class CameraOverlayview extends View implements SensorEventListener {

    SensorManager sensorManager;

    private String TAG = "PAAR";

    int orientationSensor;
    float headingAngle;
    float pitchAngle;
    float rollAnlge;

    int accelerometerSensor;
    float xAxis;//x축
    float yAxis;//y축
    float zAxis;//z축

    float a = 0.8f;
    float mLowPassHeading;
    float mLowPassPitch;
    float mLowPassRoll;
    float mLowPassX;
    float mLowPassY;
    float mLowPassZ;

    public static double sta_latitude;
    public static double sta_longitude;
    public static double des_latitude;
    public static double des_longitude;
    Bitmap mPalaceIconBitmap;
    int mWidth;
    int mHeight;
    Paint mPaint;
    int mShadowXMargin;
    int mShadowYMargin;
    Paint mShadowPaint;
    int mVisibleDistance=10;
    float mXCompassDegree;
    float mYCompassDegree;
    float mRCompassDegree;

    CameraActivity mContext;



    public CameraOverlayview(Context context) {
        super(context);
        mContext = (CameraActivity) context;
        initBitamaps();
        initSensor(context);
        initPaints();
    }

    private void initBitamaps() {
        // TODO Auto-generated method stub
        mPalaceIconBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.place);
        mPalaceIconBitmap = Bitmap.createScaledBitmap(mPalaceIconBitmap, 100,
                100, true);

    }

    private void drawGrid(double tAx, double tAy, double tBx, double tBy,
                            Canvas pCanvas, Paint pPaint) {

        // TODO Auto-generated method stub

        // 현재 위치와 랜드마크의 위치를 계산하는 공식
        double mXDegree = (double) (Math.atan((double) (tBy - tAy)
                / (double) (tBx - tAx)) * 180.0 / Math.PI);
        float mYDegree = mYCompassDegree; // 기기의 기울임각도
        float mRDegree = mRCompassDegree;


        // 4/4분면을 고려하여 0~360도가 나오게 설정
        if (tBx > tAx && tBy > tAy) {
            ;
        } else if (tBx < tAx && tBy > tAy) {
            mXDegree += 180;
        } else if (tBx < tAx && tBy < tAy) {
            mXDegree += 180;
        } else if (tBx > tAx && tBy < tAy) {
            mXDegree += 360;
        }

        // 두 위치간의 각도에 현재 스마트폰이 동쪽기준 바라보고 있는 방향 만큼 더해줌
        // 360도(한바퀴)가 넘었으면 한바퀴 회전한것이기에 360를 빼줌
        if (mXDegree + mXCompassDegree < 360) {
            mXDegree += mXCompassDegree;
        } else if (mXDegree + mXCompassDegree >= 360) {
            mXDegree = mXDegree + mXCompassDegree - 360;
        }

        Log.d(TAG, "mXDegree=" + String.valueOf(mXDegree));  // 동일

        // 계산된 각도 만큼 기기 정중앙 화면 기준 어디에 나타날지 계산함
        // 정중앙은 90도, 시야각은 30도로 75 ~ 105 사이일때만 화면에 나타남
        float mX = 0;
        float mY = 0;
        float IN = 0;

        if (mXDegree > 175 && mXDegree < 205) {
            if (mYDegree > -180 && mYDegree < 5) {
                if (mRDegree > -90 && mRDegree < -70) {

                    mX = (float) mWidth
                            - (float) ((mXDegree - 165) * ((float) mWidth / 40));

                    // icon의  핸드폰 디스플레이 위치값(값이 변경될때마다 흔들림)

                    if(mYDegree < 0)
                    {mYDegree = -(mYDegree);} // mY의 계산값이 -가 되지 않게 하기 위함(-가 될시 아이콘이 디스플레이를 벗어남)

                    mY = 240;   // 핸드폰디스플레이에 보여지는 아이콘과 거리값을 위한 세로값 고정.

                    // icon의  핸드폰 디스플레이 위치값(값이 변경될때마다 흔들림)
                    // 두 위치간의 거리를 계산함
                    Location locationA = new Location("Point A");
                    Location locationB = new Location("Point B");

                    locationA.setLongitude(tAx);
                    locationA.setLatitude(tAy);

                    locationB.setLongitude(tBx);
                    locationB.setLatitude(tBy);


                    int distance = (int) locationA.distanceTo(locationB);

                    Bitmap tIconBitmap = mPalaceIconBitmap;
                    int iconWidth, iconHeight;
                    iconWidth = tIconBitmap.getWidth();
                    iconHeight = tIconBitmap.getHeight();

                    pCanvas.drawBitmap(tIconBitmap, mX - (iconWidth / 2), mY - (iconHeight / 2), pPaint);
                    // 거리는 1000미터 이하와 초과로 나누어 m, Km로 출력
                    if (distance <= mVisibleDistance * 1000) {
                        if (distance < 1000) {
                            pCanvas.drawBitmap(tIconBitmap, mX - (iconWidth / 2), mY
                                    - (iconHeight / 2), pPaint);

                            pCanvas.drawText(distance + "m",
                                    mX - pPaint.measureText(distance + "m") / 2
                                            + mShadowXMargin, mY + iconHeight / 2 + 60
                                            + mShadowYMargin, mShadowPaint);

                            pCanvas.drawText(distance + "m",
                                    mX - pPaint.measureText(distance + "m") / 2, mY
                                            + iconHeight / 2 + 60, pPaint);

                        } else if (distance >= 1000) {
                            float fDistance = (float) distance / 1000;
                            fDistance = (float) Math.round(fDistance * 10) / 10;

                            pCanvas.drawBitmap(tIconBitmap, mX - (iconWidth / 2), mY
                                    - (iconHeight / 2), pPaint);

                            pCanvas.drawText(fDistance + "Km",
                                    mX - pPaint.measureText(fDistance + "Km") / 2
                                            + mShadowXMargin, mY + iconHeight / 2 + 60
                                            + mShadowYMargin, mShadowPaint);

                            pCanvas.drawText(fDistance + "Km",
                                    mX - pPaint.measureText(fDistance + "Km") / 2, mY
                                            + iconHeight / 2 + 60, pPaint);

                        }
                    }
                }
            }
        }
        mHandler.sendEmptyMessageDelayed(0, 10);
    }

    //로우패스 필터
    float lowPass(float current, float last) {
        return last * (1.0f - a) + current * a;//새로운 값 = 이전값*(1-가중치) + 현재값 * 가중치
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            headingAngle = sensorEvent.values[0];
            pitchAngle = sensorEvent.values[1];
            rollAnlge = sensorEvent.values[2];

            mXCompassDegree = lowPass(headingAngle,  mXCompassDegree );
            mYCompassDegree = lowPass(pitchAngle, mYCompassDegree);
            mRCompassDegree = lowPass(rollAnlge, mRCompassDegree);

            Log.d(TAG, "Heading=" + String.valueOf(mXCompassDegree));
            Log.d(TAG, "pitch=" + String.valueOf(mYCompassDegree));
            Log.d(TAG, "roll=" + String.valueOf( mRCompassDegree));

            //this.invalidate();

        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            xAxis = sensorEvent.values[0];
            yAxis = sensorEvent.values[1];
            zAxis = sensorEvent.values[2];


            mLowPassX = lowPass(xAxis, mLowPassX);
            mLowPassY = lowPass(yAxis, mLowPassY);
            mLowPassZ = lowPass(zAxis, mLowPassZ);

            Log.d(TAG, "xAxis=" + String.valueOf(mLowPassX));
            Log.d(TAG, "yAxis=" + String.valueOf(mLowPassY));
            Log.d(TAG, "zAxis=" + String.valueOf(mLowPassZ));

            //this.invalidate();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void initSensor(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        orientationSensor = Sensor.TYPE_ORIENTATION;
        accelerometerSensor = Sensor.TYPE_ACCELEROMETER;
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(orientationSensor), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(accelerometerSensor), SensorManager.SENSOR_DELAY_NORMAL);

    }
    public void onDraw(Canvas canvas) {

        canvas.save();
        canvas.rotate(180, mWidth/2 , mHeight/2 );//화면 돌리기

        getLocation(canvas);

        canvas.restore();
    }

    Handler mHandler=new Handler(){
        public void handleMessage(Message msg){
            invalidate();
            mHandler.sendEmptyMessageDelayed(0, 10);
        }
    };
    private void initPaints() {
        // TODO Auto-generated method stub
        mShadowXMargin = 2;
        mShadowYMargin = 2;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.rgb(238, 229, 222));
        mPaint.setTextSize(25);

        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setColor(Color.BLACK);
        mShadowPaint.setTextSize(25);
    }

    public void getLocation(Canvas canvas)
    {
        double tAx,tAy,tBx,tBy;
        tAx=sta_longitude;//현위치 경도좌표
        tAy=sta_latitude;//현위치 위도좌표
        tBx= des_longitude;//임의 경도좌표
        tBy= des_latitude;//임의 위도 좌표

        Log.d(TAG, "sta_ly=" + String.valueOf(tAx));  // 값이 들어가있나 확인용
        //Log.d(TAG, "sta_lx=" + String.valueOf(tAy));  // 동일
        Log.d(TAG, "des_ly=" + String.valueOf(tBx));  // 값이 들어가있나 확인용
        //Log.d(TAG, "des_lx=" + String.valueOf(tBy));  // 동일

        drawGrid(tAx,tAy,tBx,tBy,canvas,mPaint);//이미지 그려주기

    }
    public void setCurrentPoint(double latitude_st, double longitude_st)//현위치 좌표 정보 얻기
    {
        this.sta_latitude=latitude_st;
        this.sta_longitude=longitude_st;
        //Log.d(TAG, "sta_la=" + String.valueOf(sta_latitude));  // 값이 들어가있나 확인용
        //Log.d(TAG, "sta_lo=" + String.valueOf(sta_longitude));  // 동일
    }

    public void setDestinationPoint(double latitude_ds, double longitude_ds)//도착지 좌표 정보 얻기
    {
        this.des_latitude=latitude_ds;
        this.des_longitude=longitude_ds;
        //Log.d(TAG, "des_la=" + String.valueOf(des_latitude));  // 값이 들어가있나 확인용
        //Log.d(TAG, "des_lo=" + String.valueOf(des_longitude));  // 동일
    }

    // 카메라 액티비티에서 오버레이 화면 크기를 설정함
    public void setOverlaySize(int width, int height) {
        // TODO Auto-generated method stub
        mWidth = width;
        mHeight = height;

    }


}

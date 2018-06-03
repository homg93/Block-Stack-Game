package kr.ac.hallym.opengl3dblockgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLSurfaceView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainGLSurfaceView2 extends GLSurfaceView {
    private  MainGLRenderer mainRenderer;
    private MainGLActivity mainGLActivity;
    protected int canvasWidth, canvasHeight;
    public boolean soundCtrl;

    public MainGLSurfaceView2(Context context){
        super(context);
        soundCtrl = true;

        setEGLContextClientVersion(2);//openGLES의 버전설정
        mainRenderer = new MainGLRenderer(context);//렌더러가 있어야함
        setRenderer(mainRenderer);//glSurfaceView는 setRenderer가 필요
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
    public void init(int width, int height, MainGLActivity mainGLActivity){
        canvasWidth = width;
        canvasHeight = height;
        this.mainGLActivity = mainGLActivity;

    }



    @Override
    public void onPause() {
        super.onPause();
        mainRenderer.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mainRenderer.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        final int x = (int)event.getX();
        final int y = (int)event.getY();
        float xPos = x/ (float)mainRenderer.screenWidth * 2.0f - 1.0f;
        float yPos = 1.0f - y / (float)mainRenderer.screenHeight * 2.0f;
        switch(action) {
            case MotionEvent.ACTION_DOWN:
                //return super.onTouchEvent(event);
                if ( xPos > -1.0f && xPos < -0.8f && yPos > 0.8f && yPos < 1.0f) {
                    if (soundCtrl) {
                        soundCtrl = false;

                        mainGLActivity.onResume();

                    } else {
                        soundCtrl = true;
                        mainGLActivity.onResume();
                    }
                }
        }
        return mainRenderer.onTouchEvent(event);
    }
}
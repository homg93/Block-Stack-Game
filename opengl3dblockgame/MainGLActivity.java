package kr.ac.hallym.opengl3dblockgame;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainGLActivity extends Activity {

    private MainGLSurfaceView2 mainGLSurfaceView;
    private MediaPlayer mediaPlayer;
    public TextView mTextView;
    private MainGLRenderer mainGLRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main_gl);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mainGLSurfaceView = new MainGLSurfaceView2(this);
        mainGLRenderer = new MainGLRenderer(this);
        setContentView(mainGLSurfaceView);

        mTextView = new TextView(this);
        mTextView.setText("Block Stack Game");
        mTextView.setTextColor(Color.WHITE);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        addContentView(mTextView, params);


        Point size=new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        mainGLSurfaceView.init(size.x, size.y, this);

        mediaPlayer = new MediaPlayer();
        try{
            AssetFileDescriptor descriptor = getAssets().openFd("MyBlockGameMusic.mp3");
            mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
        }catch (Exception ex){
            Log.e("MainGLActivity", "Error in mediaPlayer:"+ex.toString());
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mainGLSurfaceView.onPause();

        if(mediaPlayer != null)
        {
            mediaPlayer.pause();
            if(isFinishing()){
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mainGLSurfaceView.onResume();

        if(mediaPlayer != null)
            mediaPlayer.start();
        if(mainGLSurfaceView.soundCtrl)
            mediaPlayer.start();
        else
            mediaPlayer.pause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        /*final int x = (int)event.getX();
        final int y = (int)event.getY();*/
        switch(action) {
            case MotionEvent.ACTION_DOWN:
                mTextView.setText("Oh! It is Change");
                break;
        }
        return true;
    }

}

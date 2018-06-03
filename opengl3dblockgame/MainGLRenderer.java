package kr.ac.hallym.opengl3dblockgame;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.SoundPool;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import java.io.BufferedInputStream;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class MainGLRenderer implements GLSurfaceView.Renderer {

    private MyCube myCube;
    private CutBlock cutBlock;
    private Ground MyGround;
    private Context myContext;
    private boolean isTouchedDown;
    private TexGround TexGround;
    private TexRect soundON;
    private TexRect soundOFF;
    private TexRect num0;
    private TexRect num1;
    private TexRect num2;
    private TexRect num3;
    private TexRect num4;
    private TexRect num5;
    private TexRect num6;
    private TexRect num7;
    private TexRect num8;
    private TexRect num9;
    private TexRect gameOver;
    private TexRect gameStart;

    private  float[] mtxProj = new float[16];
    private  float[] mtxView = new float[16];

    private ArrayList<MyCube> blockList; // 움직이는 블록
    private ArrayList<CutBlock> cutBlockList; //잘라진 블록 그려주기

    public int screenWidth, screenHeight;
    public int blockCnt;

    long lastTime;
    float rotAngle, transSpeed;
    boolean positiveT;
    boolean isStopped;
    boolean isGameOver;
    boolean isGameOverRender;
    private float cameraY;
    public boolean soundCtrl;

    float[] lightPos = { 1.0f, 1.0f, 1.0f, 0.0f };
    float[] ambientLight = {0.2f,0.2f,0.2f,1.0f};
    float[] diffuseLight = { 1.0f, 1.0f, 1.0f, 1.0f };
    float[] specularLight = { 0.0f, 1.0f, 1.0f, 1.0f };
    float shininess = 10.0f;

    private SoundPool soundPool;
    private int soundID;
    boolean isCollision= false;

    float[] preMin = {-0.5f,-0.5f};
    float[] preMax = {0.5f,0.5f};

    public MainGLRenderer(Context context){
        myContext = context;

    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        myCube = new MyCube(this,preMin[0],preMin[1],
                preMax[0],preMax[1]);
        MyGround  = new Ground(this);
        blockList = new ArrayList<MyCube>();
        cutBlockList = new ArrayList<CutBlock>();
        cutBlock = new CutBlock(this,preMin[0],preMin[1],
                preMax[0],preMax[1]);
        TexGround = new TexGround(this, loadBitmap("cross.png"));

        soundON = new TexRect(this, loadBitmap("soundON.png"));
        soundOFF = new TexRect(this, loadBitmap("soundOFF.png"));
        num0 = new TexRect(this, loadBitmap("number0.png"));
        num1 = new TexRect(this, loadBitmap("number1.png"));
        num2 = new TexRect(this, loadBitmap("number2.png"));
        num3 = new TexRect(this, loadBitmap("number3.png"));
        num4 = new TexRect(this, loadBitmap("number4.png"));
        num5 = new TexRect(this, loadBitmap("number5.png"));
        num6 = new TexRect(this, loadBitmap("number6.png"));
        num7 = new TexRect(this, loadBitmap("number7.png"));
        num8 = new TexRect(this, loadBitmap("number8.png"));
        num9 = new TexRect(this, loadBitmap("number9.png"));
        gameOver = new TexRect(this, loadBitmap("GameOver.png"));
        gameStart = new TexRect(this, loadBitmap("Start.png"));

        blockCnt = 0;

        soundCtrl = true;
        isGameOver = false;
        isGameOverRender = false;
        cameraY = 0.0f;
        isStopped = false;
        positiveT = true;
        transSpeed = -2.9f;
        lastTime =  System.currentTimeMillis();
        rotAngle = 0.0f;
        isTouchedDown = false;
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        GLES20.glPolygonOffset(1.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_POLYGON_OFFSET_FILL);

        soundPool = new SoundPool.Builder().build();
        try{
            AssetFileDescriptor descriptor = myContext.getAssets().openFd("explosion.ogg");
            soundID = soundPool.load(descriptor,1);
        }catch (Exception ex){
            Log.e("MainGLRenderer", "Error in loading sound: "+ ex.toString());
        }

    }
    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        //(0,0) 은 윈쪽 하단
        GLES20.glViewport(0, 0, i, i1);//GLES20.glViewport(xPosition,yPosition,w,h);
        Matrix.setIdentityM(mtxProj,0);//항등행렬로 셋팅
        Matrix.perspectiveM(mtxProj, 0, 90.0f, i/(float)i1, 0.001f, 1000.0f);
        /*Matrix.setIdentityM(mtxView,0);
        Matrix.setLookAtM(mtxView,0, 0.0f,2.0f,3.0f,0.0f,0.0f,0.0f,0.0f,1.0f,0.0f);*/
        //카메라 위치 목표지점 업벡터
        screenWidth = i;
        screenHeight = i1;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        //배경색 설정. 현재 색은 cyan
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        //GLES20.GL_DEPTH_BUFFER_BIT); 버퍼를 지울때 깊이버퍼도 같이 지워라
        //배경색 버퍼 지우기(컬러버퍼 = 프레임버퍼)

        long currentTime = System.currentTimeMillis();
        if(currentTime < lastTime)
            return;
        long elapsedTime = currentTime - lastTime;
        lastTime = currentTime;

        Matrix.setIdentityM(mtxView,0);
        Matrix.setLookAtM(mtxView,0, 2.0f,1.5f+cameraY,2.0f,0.0f,0.5f + cameraY,0.0f,0.0f,1.0f,0.0f); // 게임 시점
        //Matrix.setLookAtM(mtxView,0, 2.0f,0.0f+cameraY,0.0f,0.0f,0.0f + cameraY,0.0f,0.0f,1.0f,0.0f); // y축 높이 맞추기

        float[] mtxModel1 = new float[16];
        Matrix.setIdentityM(mtxModel1,0);
        Matrix.scaleM(mtxModel1,0,3.0f,3.0f,3.0f);
        float[] mtxTrans1 = new float[16];
        Matrix.setIdentityM(mtxTrans1,0);
        Matrix.translateM(mtxTrans1,0,-0.17f,-0.2f,-0.17f);
        Matrix.multiplyMM(mtxModel1,0,mtxTrans1,0,mtxModel1,0);
        //MyGround.draw(mtxProj, mtxView,mtxModel1);
        TexGround.draw(mtxProj, mtxView,mtxModel1);
        //삼각형 그리기
//        myCube.draw(mtxProj, mtxView,myTrackBall.roataionMatrix);

        Matrix.setIdentityM(mtxModel1,0);
        Matrix.scaleM(mtxModel1,0,0.09f,0.08f,1.0f);
        Matrix.setIdentityM(mtxTrans1,0);
        Matrix.translateM(mtxTrans1,0,-0.85f,0.9f,-0.0f);
        Matrix.multiplyMM(mtxModel1,0,mtxTrans1,0,mtxModel1,0);

        if(soundCtrl){
            soundON.draw(mtxModel1);
        }
        else
            soundOFF.draw(mtxModel1);

        if(blockCnt == 0){
            float[] mtxModelBlock = new float[16];
            Matrix.setIdentityM(mtxModelBlock, 0);
            Matrix.scaleM(mtxModelBlock, 0, 1.0f, 0.2f, 1.0f);

            cutBlock.draw2(mtxProj, mtxView, mtxModelBlock);

            float[] mtxStartModel = new float[16];
            float[] mtxStartTrans = new float[16];
            Matrix.setIdentityM(mtxStartModel, 0);
            Matrix.scaleM(mtxStartModel, 0, 1.0f, 0.3f, 1.0f);
            Matrix.setIdentityM(mtxStartTrans, 0);
            Matrix.translateM(mtxStartTrans, 0, 0.0f, 0.5f, 0.0f);
            Matrix.multiplyMM(mtxStartModel, 0, mtxStartTrans, 0, mtxStartModel, 0);
            gameStart.draw(mtxStartModel);

            if(isGameOverRender){
                Matrix.setIdentityM(mtxStartModel, 0);
                Matrix.scaleM(mtxStartModel, 0, 0.5f, 0.2f, 1.0f);
                /*Matrix.setIdentityM(mtxStartTrans, 0);
                Matrix.translateM(mtxStartTrans, 0, 0.0f, 0.0f, 0.0f);
                Matrix.multiplyMM(mtxStartModel, 0, mtxStartTrans, 0, mtxStartModel, 0);*/
                GLES20.glEnable(GLES20.GL_BLEND);
                GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
                gameOver.draw((mtxStartModel));
                GLES20.glDisable(GLES20.GL_BLEND);
            }

        }
        else//게임 진행중일때 점수표시
        {
            if(blockCnt < 10)
                numberDraw(blockCnt, 0.0f);
            else if (blockCnt < 100) {
                int num10cal = 0;
                num10cal = blockCnt - blockCnt %10;
                numberDraw(blockCnt% 10, 0.1f);
                num10cal = (int)(num10cal * 0.1);
                numberDraw(num10cal, -0.15f);

            }
        }

        for(int i= 0; i < blockList.size(); i++) {
            //Log.i("Renderer", "cnt block"+ blockList.get(i));
            float levelSpeed = 0.003f;
            if(blockCnt >20){ //점수에 따른 난이도 조절
                levelSpeed = 0.004f;

            }
            /*else if(blockCnt > 20){
                levelSpeed = 0.005f;

            }*/
            if (blockList.get(i).positiveT) {
                blockList.get(i).bTrans += elapsedTime * levelSpeed;
                if (blockList.get(i).bTrans  >= 3.0f)
                    blockList.get(i).positiveT = false;
            } else {
                blockList.get(i).bTrans -= elapsedTime * levelSpeed;
                if (blockList.get(i).bTrans <= -3.0f)
                    blockList.get(i).positiveT = true;
            }

            if((blockList.get(i).isAdd)&& i != 0) {
                blockList.get(i - 1).isAdd = false;
            }

            if(isTouchedDown) {

                if(blockCnt != 1) {
                    cutBlockList.add(new CutBlock(this,preMin[0],preMin[1],
                            preMax[0],preMax[1]));
                    blockList.add(new MyCube(this, preMin[0], preMin[1],
                            preMax[0], preMax[1]));
                }
                isTouchedDown = false;
                isGameOverRender = false;
            }

            if(blockList.get(i).isAdd || blockCnt ==1) {
                Matrix.setIdentityM(blockList.get(i).mtxModelBlock, 0);
                Matrix.scaleM(blockList.get(i).mtxModelBlock, 0, 1.0f, 0.2f, 1.0f);
                Matrix.setIdentityM(blockList.get(i).mtxTransBlock, 0);
                if (i % 2 == 0)
                    Matrix.translateM(blockList.get(i).mtxTransBlock, 0, blockList.get(i).bTrans, i * 0.2f, 0.0f);
                else
                    Matrix.translateM(blockList.get(i).mtxTransBlock, 0, 0.0f, i * 0.2f, blockList.get(i).bTrans);

                //Log.i("Min X !", "bTrans :" + blockList.get(i).bTrans);
                Matrix.multiplyMM(blockList.get(i).mtxModelBlock, 0, blockList.get(i).mtxTransBlock, 0, blockList.get(i).mtxModelBlock, 0);
                if(i == 0) {
                    myCube.draw(mtxProj, mtxView, blockList.get(i).mtxModelBlock);
                }
                blockList.get(i).draw(mtxProj, mtxView, blockList.get(i).mtxModelBlock);

            }
            else if(i == blockList.size() && i >4) {
                blockList.remove(0);
                blockList.remove(1);
                Log.i("Remove", "Remove");
            }
        }


        for(int i= 0; i < cutBlockList.size(); i++) {
/*            cutBlock = (new CutBlock(this,cutPreMin[i][0],cutPreMin[i][1],
                    cutPreMax[i][0],cutPreMax[i][1]));*/

            Matrix.setIdentityM(cutBlockList.get(i).mtxModelBlock, 0);
            Matrix.scaleM(cutBlockList.get(i).mtxModelBlock, 0, 1.0f, 0.2f, 1.0f);
            Matrix.setIdentityM(cutBlockList.get(i).mtxTransBlock, 0);
            Matrix.translateM(cutBlockList.get(i).mtxTransBlock, 0, 0.0f, i * 0.2f, 0.0f);
            Matrix.multiplyMM(cutBlockList.get(i).mtxModelBlock, 0, cutBlockList.get(i).mtxTransBlock, 0, cutBlockList.get(i).mtxModelBlock, 0);
            cutBlockList.get(i).draw2(mtxProj, mtxView, cutBlockList.get(i).mtxModelBlock);
        }

    }

    public static int loadShader(int type, String shaderCode) {//2.0서부터 꼭써야함 gpu 에서 실행
        int shader = GLES20.glCreateShader(type);//shader생성!! (type2개 vertexShader Fragment Shader
        //쉐이더 생성(type : vertexshader, fragmentshader)
        GLES20.glShaderSource(shader, shaderCode); //Shader 소수코드지정 Text Type
        //쉐이더 소스코드 지정(텍스트 타입) -> 그래서 MyTriangle에서 string형식으로 선언 오류메세지 보는법이 있지만 자세하게 안나옴
        GLES20.glCompileShader(shader);//소스코드 컴파일
        //쉐이더 소스코드 컴파일
        int compiled[] = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if(compiled[0] <= 0){
            Log.e("MainGLRenderer", GLES20.glGetShaderInfoLog(shader));
            return 0;
        }
        return shader;
    }

    public void onPause() {

    }
    public void onResume() {

    }

    public void checkCollision(float newMin[], float newMax[]){
//blockList.get(i).maxBoundingBox[0]
//        newMin[0]

        if(blockCnt % 2 ==0)//x축이동 일때
        {
            if(newMax[0] > preMax[0]&& preMax[0]> newMin[0]  && newMin[0] >= preMin[0]) {
                preMin[0] = newMin[0];
                playSound();
                isCollision = true;
            }
            else if(newMax[0] < preMax[0]&& newMax[0]> preMin[0]  && newMin[0] <= preMin[0]) {
                preMax[0] = newMax[0];
                playSound();
                isCollision = true;
            }
            else if(newMax[0] >= preMax[0] && preMin[0] > newMin[0]){
                playSound();
                isCollision = true;
            }
            else{
                isGameOver = true;
                GameOver();

            }
            Log.i("Min X !", "X축 이동이야:");

        }
        else {
            if(newMax[1] > preMax[1]&& preMax[1]> newMin[1]  && newMin[1] >= preMin[1]) {
                preMin[1] = newMin[1];
                playSound();
                isCollision = true;
            }
            else if(newMax[1] < preMax[1]&& newMax[1]> preMin[1]  && newMin[1] <= preMin[1]) {
                preMax[1] = newMax[1];
                playSound();
                isCollision = true;
            }
            else if(newMax[1] >= preMax[1] && preMin[1] > newMin[1]){
                playSound();
                isCollision = true;
            }
            else{
                GameOver();
            }
        }
        if(isCollision){//충동했다면 크기조정해서 다시그려줌

            Log.i("Min X !", "preMin :" + preMin[0] + ", " + preMin[1]);
            Log.i("Max X !", "preMax :" + preMax[0] + ", " + preMax[1]);
            isCollision = false;
        }
    }

    public boolean onTouchEvent(MotionEvent event){
        final int action = event.getActionMasked();
        final int x = (int)event.getX();
        final int y = (int)event.getY();
        float xPos = x/ (float)screenWidth * 2.0f - 1.0f;
        float yPos = 1.0f - y / (float)screenHeight * 2.0f;
        switch(action){
            case MotionEvent.ACTION_DOWN:
                Log.i("Renderer", "New block");
                //mainGLActivity.mTextView.setText(String.valueOf(blockCnt));
                if ( !(xPos > -1.0f && xPos < -0.8f && yPos > 0.8f && yPos < 1.0f)) {
                    blockCnt++;
                    if (blockCnt % 5 == 0) {
                        cameraY += 1.0f;
                    }


                    if (blockCnt >= 2) {
                        float newBlockMin[] = blockList.get(blockList.size() - 1).minBoundingBox;
                        float newBlockMax[] = blockList.get(blockList.size() - 1).maxBoundingBox;

                        blockList.get(blockList.size() - 1).stopBlockPos = blockList.get(blockList.size() - 1).bTrans;

                        newBlockMin[0] = newBlockMin[0] + blockList.get(blockList.size() - 1).stopBlockPos;
                        newBlockMin[1] = newBlockMin[1] + blockList.get(blockList.size() - 1).stopBlockPos;
                        newBlockMax[0] = newBlockMax[0] + blockList.get(blockList.size() - 1).stopBlockPos;
                        newBlockMax[1] = newBlockMax[1] + blockList.get(blockList.size() - 1).stopBlockPos;

                        checkCollision(newBlockMin, newBlockMax);
                        isTouchedDown = true;

/*                    if(!isGameOver) {
                        cutPreMin[blockCnt - 1] = preMin;
                        cutPreMax[blockCnt - 1] = preMax;
                    }*/
                    /*blockList.get(blockList.size() - 2).resultMin = preMin;
                    blockList.get(blockList.size() - 2).resultMax = preMax;
*/
                        Log.i("Min X !", "preMin :" + preMin[0] + ", " + preMin[1]);
                        Log.i("Max X !", "preMax :" + preMax[0] + ", " + preMax[1]);


                    } else if (blockCnt == 1) {
                        blockList.add(new MyCube(this, preMin[0], preMin[1],
                                preMax[0], preMax[1]));
                    }
//                Log.i("pre & now :", "min" + newBlockMin[0]);
//                Log.i("pre & now :", "max" + newBlockMax[0]);
                }
                else{
                    if (soundCtrl) {
                        soundCtrl = false;

                    } else {
                        soundCtrl = true;
                    }
                }
                break;

        }
        return true;
    }
    public void playSound(){
        try{
            soundPool.play(soundID, 1.0f,1.0f,0,0,1.0f);
        } catch (Exception ex){
            Log.e("MainGLRenderer", "Error in playing sound: "+ ex.toString());
        }
    }
    public Bitmap loadBitmap(String filename){
        Bitmap bitmap = null;
        try{
            AssetManager manager = myContext.getAssets();
            BufferedInputStream inputStream = new BufferedInputStream(manager.open(filename));
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Exception ex){
            Log.e("MainGLRenderer", "Error in loading a bitmap:" + ex.toString());
        }
        return  bitmap;
    }
    public void GameOver(){
        isGameOverRender = true;
        isGameOver = false;
        isTouchedDown = false;
        cameraY = 0.0f;
        isStopped = false;
        positiveT = true;
        blockCnt = 0;
        preMin[0] = -0.5f;
        preMin[1] = -0.5f;
        preMax[0] = 0.5f;
        preMax[1] = 0.5f;
/*        for(int i= 0; i < blockList.size(); i++) {
            blockList.remove(i);
        }
        for(int i= 0; i < cutBlockList.size(); i++) {
            cutBlockList.remove(i);
        }*/
        blockList.clear();
        cutBlockList.clear();
    }

    public void numberDraw(int blockCnt, float transX){
        float[] mtxNumModel = new float[16];
        float[] mtxNumTrans = new float[16];
        Matrix.setIdentityM(mtxNumModel, 0);
        Matrix.scaleM(mtxNumModel, 0, 0.2f, 0.3f, 1.0f);
        Matrix.setIdentityM(mtxNumTrans, 0);
        Matrix.translateM(mtxNumTrans, 0, transX, 0.5f, 0.0f);
        Matrix.multiplyMM(mtxNumModel, 0, mtxNumTrans, 0, mtxNumModel, 0);

        //1의 자리 출력
        if (blockCnt == 0)
            num0.draw(mtxNumModel);
        else if (blockCnt == 1)
            num1.draw(mtxNumModel);
        else if (blockCnt == 2)
            num2.draw(mtxNumModel);
        else if (blockCnt == 3)
            num3.draw(mtxNumModel);
        else if (blockCnt == 4)
            num4.draw(mtxNumModel);
        else if (blockCnt == 5)
            num5.draw(mtxNumModel);
        else if (blockCnt == 6)
            num6.draw(mtxNumModel);
        else if (blockCnt == 7)
            num7.draw(mtxNumModel);
        else if (blockCnt == 8)
            num8.draw(mtxNumModel);
        else if (blockCnt == 9)
            num9.draw(mtxNumModel);
    }
}
package kr.ac.hallym.opengl3dblockgame;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Ground{
    private MainGLRenderer mainRenderer;
    //그림그리기위해서 렌더러 선언(loadShader을 불러오기 위해서 선언)
    private FloatBuffer vertexBuffer;
    //vertex정보(ex> 좌표, 색상, 법선벡터, 텍스쳐 좌표)를 저장하기 위한 버퍼 -> Attribute
    //Attribute : ALU유닛 마다 배열처럼 반응함 (VS Uniform)
    //gpu에 넘기기 위해 FloatButter를 사용한다.
    private ShortBuffer indexBuffer;

    static final int COORDS_PER_VERTEX = 3; //3차원(x, y, z)
    static float vertexCoords[] = { // vertex좌표 3X3 (오른손 좌표계, 반시계방향으로)

            // {0,1,2,0,2,3};
            -1.0f,0.0f,-1.0f,
            -1.0f,0.0f,1.0f,
            1.0f, 0.0f, 1.0f,//2
            1.0f, 0.0f, -1.0f,//3

            -1.0f,0.0f,-1.0f,
            -1.0f,0.0f,1.0f,
            -0.6f,0.0f,-1.0f,
            -0.6f,0.0f,1.0f,
            -0.2f,0.0f,-1.0f,
            -0.2f,0.0f,1.0f,
            0.2f,0.0f,-1.0f,
            0.2f,0.0f,1.0f,
            0.6f,0.0f,-1.0f,
            0.6f,0.0f,1.0f,
            1.0f,0.0f,-1.0f,
            1.0f,0.0f,1.0f,


            -1.0f,0.0f,-1.0f,
            1.0f,0.0f,-1.0f,
            -1.0f,0.0f,-0.6f,
            1.0f,0.0f,-0.6f,
            -1.0f,0.0f, -0.2f,
            1.0f,0.0f, -0.2f,
            -1.0f,0.0f, 0.2f,
            1.0f,0.0f, 0.2f,
            -1.0f,0.0f, 0.6f,
            1.0f,0.0f, 0.6f,
            -1.0f,0.0f,1.0f,
            1.0f,0.0f,1.0f
    };

    //private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
    // 중요!!!!!!
    // vertex가 몇개 있다!! 그려라!! 라고 만들 수 있게 하는 의미
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    //하나의 vertex가 차지하는 용량(건너뛰기 할 크기) 12byte

    static short groundIndex[] = {
            0,1,2,0,2,3 //back

    };

    static float color[] = {0.8f,0.8f,0.8f,1.0f};

    //색상 = 노란색

    float theta = 0.0f;
    boolean ccwDirection = true;

    //    private final String vertexShaderCode =             //위치 vertex 마다 실행
//            "attribute vec4 vPosition;" +               //attribute는 vertex 마다 전달 돠는(좌표,색상,법선백터,택스트 좌표)
//                    "uniform vec4 vNormal;"+
//                    "uniform mat4 MVP;"+
//                    "varying vec4 fColor;"+
//                    "void main() {" +                   //entry point function (제일 먼저 찾는 함수)
//                    "   gl_Position = MVP * vPosition;" + // 계속 바뀌는건 vPosition이다. 계산량 줄이기 위해 MVP한번에 묶어서 쉐이더에 보냄
//                    "   fColor = vNormal;"+
//                    "}";
    private final String vertexShaderCode =             //위치 vertex 마다 실행
            "attribute vec4 vPosition;" +               //attribute는 vertex 마다 전달 돠는(좌표,색상,법선백터,택스트 좌표)
                    "uniform vec4 vNormal;" +
                    "uniform mat4 MVP, MV;"+
                    "uniform vec4 lightPos, ambientLight, diffuseLight, specularLight;"+
                    "uniform float shininess;"+
                    "varying vec4 fColor;"+
                    "void main() {" +                   //entry point function (제일 먼저 찾는 함수)
                    "   gl_Position = MVP * vPosition;" + // 계속 바뀌는건 vPosition이다. 계산량 줄이기 위해 MVP한번에 묶어서 쉐이더에 보냄
                    "   vec3 L = normalize(lightPos.xyz);"+
                    "   vec3 N = normalize(MV * vec4(vNormal.xyz, 0.0)).xyz;"+
                    "   float kd = max(dot(L,N), 0.0);"+
                    "   vec3 V = normalize(-(MV * vPosition).xyz);"+
                    "   vec3 H = normalize(L + V);"+
                    "   float ks = pow(max(dot(N,H),0.0), shininess);"+
                    "   fColor = ambientLight+ kd*diffuseLight+ ks*specularLight;"+
                    "}";

    private final String fragmentShaderCode =           //pixel 마다 실행
            "precision mediump float;" +                //정확도(highp, lowp)정확도에따라 실행속도 달라질수 있음
                    //"uniform vec4 fColor;"+//칼라핸들 유니폼으로 바뀜
                    "varying vec4 fColor;"+
                    "void main() {" +                   //entry point position
                    "   gl_FragColor = fColor;" +       //fs는 반드시 color를 계산해야 한다(frame Buffer 안에 있는 pixel)
                    "}";

    private final String vertexShaderCodePhong =             //위치 vertex 마다 실행
            "attribute vec4 vPosition;" +               //attribute는 vertex 마다 전달 돠는(좌표,색상,법선백터,택스트 좌표)
                    "uniform mat4 MVP;"+
                    "varying vec4 fPosition;"+
                    "void main() {" +                   //entry point function (제일 먼저 찾는 함수)
                    "   gl_Position = MVP * vPosition;" + // 계속 바뀌는건 vPosition이다. 계산량 줄이기 위해 MVP한번에 묶어서 쉐이더에 보냄
                    "   fPosition = vPosition;"+
                    "}";

    private final String fragmentShaderCodePhong =           //pixel 마다 실행
            "precision mediump float;" +                //정확도(highp, lowp)정확도에따라 실행속도 달라질수 있음
                    "uniform vec4 fNormal;"+
                    "uniform mat4 MV;"+
                    "uniform vec4 lightPos, ambientLight, diffuseLight, specularLight;"+
                    "uniform float shininess;"+
                    "varying vec4 fPosition;"+
                    "void main() {" +                   //entry point position
                    "   vec3 L = normalize(lightPos.xyz);"+
                    "   vec3 N = normalize(MV * vec4(fNormal.xyz, 0.0)).xyz;"+
                    "   float kd = max(dot(L,N), 0.0);"+
                    "   vec3 V = normalize(-(MV * fPosition).xyz);"+
                    "   vec3 H = normalize(L + V);"+
                    "   float ks = pow(max(dot(N,H),0.0), shininess);"+
                    "   gl_FragColor = ambientLight+ kd*diffuseLight+ ks*specularLight;"+
                    "}";


    private int programID, positionHandle, colorHandle,mvpHandle, normalHandle; //program = vs + fs Handle은 shader의 uniform,attribute를 가리키는 포인터
    private int mvHandle, ambientHandle, lightPosHandle, diffuseHandle, specularHandle, shininessHandle;
    //핸들 연결고리
    //cpu에 있는 쉐이더를 GPU에 전달하기 위해서
    // varying은 fragmentShader로 보간되어 넘어감
    public Ground(MainGLRenderer renderer) {
        mainRenderer = renderer;

        //Vertex Buffer Object : GPU에 전달할 vertex 정보(Attribute)
        ByteBuffer buffer = ByteBuffer.allocateDirect(vertexCoords.length * 4);   //크기
        buffer.order(ByteOrder.nativeOrder());                                      // c++ -> java 로 바꾸기
        vertexBuffer = buffer.asFloatBuffer();
        vertexBuffer.put(vertexCoords);
        vertexBuffer.position(0);

        buffer = ByteBuffer.allocateDirect(groundIndex.length *2);//2Byte short size
        buffer.order(ByteOrder.nativeOrder());
        indexBuffer = buffer.asShortBuffer();
        indexBuffer.put(groundIndex);
        indexBuffer.position(0);



        int vertexShader = mainRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCodePhong);      //vs 생성
        int fragmentShader = mainRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCodePhong);//fs 생성
        programID = GLES20.glCreateProgram();
        GLES20.glAttachShader(programID, vertexShader);  //링킹
        GLES20.glAttachShader(programID, fragmentShader);//링킹
        GLES20.glLinkProgram(programID);

        positionHandle = GLES20.glGetAttribLocation(programID, "vPosition");//안에 attribute형 vPosition이있는거 찾아오기

        normalHandle = GLES20.glGetUniformLocation(programID, "fNormal");//안에 attribute형 vPosition이있는거 찾아오기
        colorHandle = GLES20.glGetUniformLocation(programID, "fColor");
        //thetaHandle = GLES20.glGetUniformLocation(programID, "theta");
        mvpHandle = GLES20.glGetUniformLocation(programID, "MVP");
        mvHandle = GLES20.glGetUniformLocation(programID, "MV");
        lightPosHandle = GLES20.glGetUniformLocation(programID, "lightPos");
        ambientHandle = GLES20.glGetUniformLocation(programID, "ambientLight");
        diffuseHandle= GLES20.glGetUniformLocation(programID, "diffuseLight");
        specularHandle = GLES20.glGetUniformLocation(programID, "specularLight");
        shininessHandle = GLES20.glGetUniformLocation(programID, "shininess");
    }

    public void draw(float[] mtxProj, float[] mtxView, float[] mtxModel) {
        GLES20.glUseProgram(programID);                                 //프로그램 불러오기, 여러개일경우 쓸때마다

        GLES20.glEnableVertexAttribArray(positionHandle);               //attribute 활성화
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);    //attribute 정보 할당

        GLES20.glUniform4f(normalHandle, 0.0f, 1.0f, 0.0f, 0.0f);
        GLES20.glUniform4fv(lightPosHandle, 1, mainRenderer.lightPos, 0);
        GLES20.glUniform4fv(ambientHandle, 1, mainRenderer.ambientLight, 0);
        GLES20.glUniform4fv(diffuseHandle, 1, mainRenderer.diffuseLight, 0);
        GLES20.glUniform4fv(specularHandle, 1, mainRenderer.specularLight, 0);
        GLES20.glUniform1f(shininessHandle, mainRenderer.shininess);
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        float[] mtxMVP = new float[16];
        Matrix.multiplyMM(mtxMVP,0,mtxView,0,mtxModel,0);
        GLES20.glUniformMatrix4fv(mvHandle, 1,false,mtxMVP,0);
        Matrix.multiplyMM(mtxMVP,0,mtxProj,0, mtxMVP,0);
        GLES20.glUniformMatrix4fv(mvpHandle, 1,false,mtxMVP,0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, groundIndex.length, GLES20.GL_UNSIGNED_SHORT
                , indexBuffer);//index 순서대로 gpu에 넘겨줌

        GLES20.glUniform4f(normalHandle,0.0f,0.0f,0.0f,0.0f);
        GLES20.glUniform4f(ambientHandle,0.0f,0.0f,0.0f,1.0f);
        GLES20.glUniform4f(colorHandle,0.0f,0.0f,0.0f,1.0f);
        GLES20.glLineWidth(2.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES,4,24);

        GLES20.glDisableVertexAttribArray(positionHandle);              //attribute 비활성화

    }

    public void changeDirection(){
        ccwDirection = !ccwDirection;
    }
}

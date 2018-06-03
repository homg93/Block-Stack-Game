package kr.ac.hallym.opengl3dblockgame;

/**
 * Created by KHG_WN on 2017-12-11.
 */

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class TexRect {

    private MainGLRenderer mainRenderer;
    private FloatBuffer vertexBuffer, uvBuffer;
    private ShortBuffer indexBuffer;

    static final int COORDS_PER_VERTEX = 3;     // (x, y, z)
    static float vertexCoords[] = {             // in counter-clockwise order
            -1.0f,  1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f,
            1.0f,  1.0f, 0.1f
    };
    private final int vertexStride = COORDS_PER_VERTEX * 4;     // 4 bytes for a vertex

    static float vertexUVs[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };

    static short rectIndex[] = {
            0, 1, 2, 0, 2, 3
    };

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec2 vTexCoord;" +
                    "uniform mat4 MVP;" +
                    "varying vec2 fTexCoord;" +
                    "void main() {" +
                    "   gl_Position = MVP * vPosition;" +
                    "   fTexCoord = vTexCoord;" +
                    "}";
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec2 fTexCoord;" +
                    "uniform sampler2D sTexture;" +
                    "void main() {" +
                    "   gl_FragColor = texture2D(sTexture, fTexCoord);" +
                    "}";

    private int programID, positionHandle, uvHandle, mvpHandle;
    private int[] textureID = new int[1];

    public TexRect(MainGLRenderer renderer, Bitmap bitmap) {
        mainRenderer = renderer;

        ByteBuffer buffer = ByteBuffer.allocateDirect(vertexCoords.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        vertexBuffer = buffer.asFloatBuffer();
        vertexBuffer.put(vertexCoords);
        vertexBuffer.position(0);

        buffer = ByteBuffer.allocateDirect(vertexUVs.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        uvBuffer = buffer.asFloatBuffer();
        uvBuffer.put(vertexUVs);
        uvBuffer.position(0);

        buffer = ByteBuffer.allocateDirect(rectIndex.length * 2);
        buffer.order(ByteOrder.nativeOrder());
        indexBuffer = buffer.asShortBuffer();
        indexBuffer.put(rectIndex);
        indexBuffer.position(0);

        int vertexShader = mainRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = mainRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        programID = GLES20.glCreateProgram();
        GLES20.glAttachShader(programID, vertexShader);
        GLES20.glAttachShader(programID, fragmentShader);
        GLES20.glLinkProgram(programID);

        positionHandle = GLES20.glGetAttribLocation(programID, "vPosition");
        uvHandle = GLES20.glGetAttribLocation(programID, "vTexCoord");
        mvpHandle = GLES20.glGetUniformLocation(programID, "MVP");

        GLES20.glGenTextures(1, textureID, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
    }

    public void draw(float [] mtxModel) {
        GLES20.glUseProgram(programID);

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, vertexStride, vertexBuffer);

        GLES20.glEnableVertexAttribArray(uvHandle);
        GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, 0, uvBuffer);

        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mtxModel, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID[0]);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, rectIndex.length, GLES20.GL_UNSIGNED_SHORT,
                indexBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(uvHandle);
    }
}

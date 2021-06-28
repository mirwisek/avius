package com.google.mediapipe.glutil;


import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class ExternalTextureRenderer {
    private static final FloatBuffer TEXTURE_VERTICES = ShaderUtil.floatBuffer(new float[]{0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F});
    private static final FloatBuffer FLIPPED_TEXTURE_VERTICES = ShaderUtil.floatBuffer(new float[]{0.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 0.0F});
    private static final ExternalTextureRenderer.Vertex BOTTOM_LEFT = new ExternalTextureRenderer.Vertex(-1.0F, -1.0F);
    private static final ExternalTextureRenderer.Vertex BOTTOM_RIGHT = new ExternalTextureRenderer.Vertex(1.0F, -1.0F);
    private static final ExternalTextureRenderer.Vertex TOP_LEFT = new ExternalTextureRenderer.Vertex(-1.0F, 1.0F);
    private static final ExternalTextureRenderer.Vertex TOP_RIGHT = new ExternalTextureRenderer.Vertex(1.0F, 1.0F);
    private static final ExternalTextureRenderer.Vertex[] POSITION_VERTICIES;
    private static final FloatBuffer POSITION_VERTICIES_0;
    private static final FloatBuffer POSITION_VERTICIES_90;
    private static final FloatBuffer POSITION_VERTICIES_180;
    private static final FloatBuffer POSITION_VERTICIES_270;
    private static final String TAG = "ExternalTextureRend";
    private static final int ATTRIB_POSITION = 1;
    private static final int ATTRIB_TEXTURE_COORDINATE = 2;
    private int program = 0;
    private int frameUniform;
    private int textureTransformUniform;
    private float[] textureTransformMatrix = new float[16];
    private boolean flipY;
    private int rotation = 0;

    public ExternalTextureRenderer() {
    }

    public void setup() {
        Map<String, Integer> attributeLocations = new HashMap();
        attributeLocations.put("position", 1);
        attributeLocations.put("texture_coordinate", 2);
        this.program = ShaderUtil.createProgram("uniform mat4 texture_transform;\nattribute vec4 position;\nattribute mediump vec4 texture_coordinate;\nvarying mediump vec2 sample_coordinate;\n\nvoid main() {\n  gl_Position = position;\n  sample_coordinate = (texture_transform * texture_coordinate).xy;\n}", "#extension GL_OES_EGL_image_external : require\nvarying mediump vec2 sample_coordinate;\nuniform samplerExternalOES video_frame;\n\nvoid main() {\n  gl_FragColor = texture2D(video_frame, sample_coordinate);\n}", attributeLocations);
        this.frameUniform = GLES20.glGetUniformLocation(this.program, "video_frame");
        this.textureTransformUniform = GLES20.glGetUniformLocation(this.program, "texture_transform");
        ShaderUtil.checkGlError("glGetUniformLocation");
    }

    public void setFlipY(boolean flip) {
        this.flipY = flip;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public void render(SurfaceTexture surfaceTexture) {
        GLES20.glClear(16384);
        GLES20.glActiveTexture(33984);
        ShaderUtil.checkGlError("glActiveTexture");
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(this.textureTransformMatrix);

        // Set a fixed orientation as landscape
        // Added by MIR
        Matrix.rotateM(textureTransformMatrix, 0, 270, 0, 0, 1);
        Matrix.translateM(textureTransformMatrix, 0, -1, 0, 0);
//        switch (rotation) {
//            case 0:
//                break;
//            case 90:
//                Matrix.rotateM(textureTransformMatrix, 0, 90, 0, 0, 1);
//                Matrix.translateM(textureTransformMatrix, 0, 0, -1, 0);
//                break;
//            case 180:
//                Matrix.rotateM(textureTransformMatrix, 0, 180, 0, 0, 1);
//                Matrix.translateM(textureTransformMatrix, 0, -1, -1, 0);
//                break;
//            case 270:
//                Matrix.rotateM(textureTransformMatrix, 0, 270, 0, 0, 1);
//                Matrix.translateM(textureTransformMatrix, 0, -1, 0, 0);
//                break;
//            default:
//                //unknown
//        }

        GLES20.glTexParameteri(36197, 10241, 9729);
        GLES20.glTexParameteri(36197, 10240, 9729);
        GLES20.glTexParameteri(36197, 10242, 33071);
        GLES20.glTexParameteri(36197, 10243, 33071);
        ShaderUtil.checkGlError("glTexParameteri");
        GLES20.glUseProgram(this.program);
        ShaderUtil.checkGlError("glUseProgram");
        GLES20.glUniform1i(this.frameUniform, 0);
        ShaderUtil.checkGlError("glUniform1i");
        GLES20.glUniformMatrix4fv(this.textureTransformUniform, 1, false, this.textureTransformMatrix, 0);
        ShaderUtil.checkGlError("glUniformMatrix4fv");
        GLES20.glEnableVertexAttribArray(1);
        GLES20.glVertexAttribPointer(1, 2, 5126, false, 0, this.getPositionVerticies());
        GLES20.glEnableVertexAttribArray(2);
        GLES20.glVertexAttribPointer(2, 2, 5126, false, 0, this.flipY ? FLIPPED_TEXTURE_VERTICES : TEXTURE_VERTICES);
        ShaderUtil.checkGlError("program setup");
        GLES20.glDrawArrays(5, 0, 4);
        ShaderUtil.checkGlError("glDrawArrays");
        GLES20.glBindTexture(36197, 0);
        ShaderUtil.checkGlError("glBindTexture");
        GLES20.glFinish();
    }

    public void release() {
        GLES20.glDeleteProgram(this.program);
    }

    private FloatBuffer getPositionVerticies() {
        switch(this.rotation) {
            case 0:
            default:
                return POSITION_VERTICIES_0;
            case 1:
                return POSITION_VERTICIES_90;
            case 2:
                return POSITION_VERTICIES_180;
            case 3:
                return POSITION_VERTICIES_270;
        }
    }

    private static FloatBuffer fb(ExternalTextureRenderer.Vertex[] v, int i0, int i1, int i2, int i3) {
        return ShaderUtil.floatBuffer(new float[]{v[i0].x, v[i0].y, v[i1].x, v[i1].y, v[i2].x, v[i2].y, v[i3].x, v[i3].y});
    }

    static {
        POSITION_VERTICIES = new ExternalTextureRenderer.Vertex[]{BOTTOM_LEFT, BOTTOM_RIGHT, TOP_LEFT, TOP_RIGHT};
        POSITION_VERTICIES_0 = fb(POSITION_VERTICIES, 0, 1, 2, 3);
        POSITION_VERTICIES_90 = fb(POSITION_VERTICIES, 2, 0, 3, 1);
        POSITION_VERTICIES_180 = fb(POSITION_VERTICIES, 3, 2, 1, 0);
        POSITION_VERTICIES_270 = fb(POSITION_VERTICIES, 1, 3, 0, 2);
    }

    private static class Vertex {
        float x;
        float y;

        Vertex(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}

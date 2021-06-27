package com.google.mediapipe.glutil;

import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import javax.annotation.Nullable;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLSurface;

public class GlThread extends Thread {
    private static final String TAG = "GlThread";
    private static final String THREAD_NAME = "mediapipe.glutil.GlThread";
    private boolean doneStarting;
    private boolean startedSuccessfully;
    private final Object startLock;
    protected volatile EglManager eglManager;
    protected EGLSurface eglSurface;
    protected Handler handler;
    protected Looper looper;
    protected int framebuffer;

    public GlThread(@Nullable Object parentContext) {
        this(parentContext, (int[])null);
    }

    public GlThread(@Nullable Object parentContext, @Nullable int[] additionalConfigAttributes) {
        this.startLock = new Object();
        this.eglSurface = null;
        this.handler = null;
        this.looper = null;
        this.framebuffer = 0;
        this.eglManager = new EglManager(parentContext, additionalConfigAttributes);
        this.setName("mediapipe.glutil.GlThread");
    }

    public Handler getHandler() {
        return this.handler;
    }

    public Looper getLooper() {
        return this.looper;
    }

    public EglManager getEglManager() {
        return this.eglManager;
    }

    public EGLContext getEGLContext() {
        return this.eglManager.getContext();
    }

    public int getFramebuffer() {
        return this.framebuffer;
    }

    public void bindFramebuffer(int texture, int width, int height) {
        GLES20.glBindFramebuffer(36160, this.framebuffer);
        GLES20.glFramebufferTexture2D(36160, 36064, 3553, texture, 0);
        int status = GLES20.glCheckFramebufferStatus(36160);
        if (status != 36053) {
            throw new RuntimeException("Framebuffer not complete, status=" + status);
        } else {
            GLES20.glViewport(0, 0, width, height);
            ShaderUtil.checkGlError("glViewport");
        }
    }

    public void run() {
        boolean var15 = false;

        try {
            var15 = true;
            Looper.prepare();
            this.handler = this.createHandler();
            this.looper = Looper.myLooper();
            Log.d("GlThread", String.format("Starting GL thread %s", this.getName()));
            this.prepareGl();
            this.startedSuccessfully = true;
            var15 = false;
        } finally {
            if (var15) {
                synchronized(this.startLock) {
                    this.doneStarting = true;
                    this.startLock.notify();
                }
            }
        }

        synchronized(this.startLock) {
            this.doneStarting = true;
            this.startLock.notify();
        }

        try {
            Looper.loop();
        } finally {
            this.looper = null;
            this.releaseGl();
            this.eglManager.release();
            Log.d("GlThread", String.format("Stopping GL thread %s", this.getName()));
        }

    }

    public boolean quitSafely() {
        if (this.looper == null) {
            return false;
        } else {
            this.looper.quitSafely();
            return true;
        }
    }

    public boolean waitUntilReady() throws InterruptedException {
        synchronized(this.startLock) {
            while(!this.doneStarting) {
                this.startLock.wait();
            }
        }

        return this.startedSuccessfully;
    }

    public void prepareGl() {
        this.eglSurface = this.createEglSurface();
        this.eglManager.makeCurrent(this.eglSurface, this.eglSurface);
        GLES20.glDisable(2929);
        GLES20.glDisable(2884);
        int[] values = new int[1];
        GLES20.glGenFramebuffers(1, values, 0);
        this.framebuffer = values[0];
    }

    public void releaseGl() {
        if (this.framebuffer != 0) {
            int[] values = new int[]{this.framebuffer};
            GLES20.glDeleteFramebuffers(1, values, 0);
            this.framebuffer = 0;
        }

        this.eglManager.makeNothingCurrent();
        if (this.eglSurface != null) {
            this.eglManager.releaseSurface(this.eglSurface);
            this.eglSurface = null;
        }

    }

    protected Handler createHandler() {
        return new Handler();
    }

    protected EGLSurface createEglSurface() {
        return this.eglManager.createOffscreenSurface(1, 1);
    }
}

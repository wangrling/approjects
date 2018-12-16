package com.android.approjects.cameraraw;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

/**
 * 1. 打开相机，获取流，
 * 2. 对流进行处理，显示到屏幕上。
 * 2. 进行拍照保存。
 * 3. 进行录像保存。
 * 4. 增加解码器。
 * 3. 将流发送到服务器。
 */

/**
 * 原文地址https://source.android.com/devices/graphics
 * 1. 可以使用Canvas或者OpenGL进行图形绘制，Canvas的底层也是使用OpenGL实现硬件加速。
 *
 * 2. 理解Surface很重要：
 * No matter what rendering API developers use, everything is rendered onto a "surface."
 * The surface represents the producer side of a buffer queue that is often
 * consumed by SurfaceFlinger.
 * 所有的窗口创建都是通过Surface支持。
 * All of the visible surfaces rendered are composited onto the display by SurfaceFlinger.
 * SurfaceFlinger负责把Surface里面的内容显示到屏幕上。
 *
 * 3. 图形生产者生产graphic buffers供消费者。
 * SurfaceFlinger根据WindowManager的信息将生产出来的图形显示在屏幕上。
 * 其它的程序也可以充当消费者，比如ImageReader和CameraPreview等。
 *
 * 4. Window代表View的容器，WindowManager对窗口进行管理。
 * WindowManager将输入事件，平移，动画，位置等很多信息输入到SurfaceFlinger，
 * 因此SurfaceFinger可以绘制图形。
 *
 * 5. Hardware Composer代表硬件，完成最终的合成。
 *
 * 6. Gralloc为图形生产者分配内存空间。
 *
 * 7. 理解BufferQueue的作用，充当生产者消费者之间的桥梁，生产者可以是
 * camera previews produces by the camera HAL or OpenGL ES games.
 * 有点不能理解app充当消费者。
 *
 * 8. The producer interface, or what you pass to somebody who wants to generate
 * graphic buffers, is IGraphicBufferProducer (part of SurfaceTexture).
 *
 * 9. 每个开发者应该知道 Surface, SurfaceHolder, EGLSurface,
 * SurfaceView, GLSurfaceView, SurfaceTexture, TextureView, SurfaceFlinger, and Vulkan.
 *
 * 10. 低层次的组件
 * BufferQueue提供生产者消费者沟通的桥梁，其中Buffer是通过gralloc生成的。
 * SurfaceFlinger接收buffers，并把它们送到display中，Hardware Composer决定使用
 * 最有效的方式合成buffers, virtual displays make composited
 * output available within the system.
 *
 * 11. Surface相当于一张画布，提供buffer queue, Canvas APIs相当于画笔，OpenGL ES
 * 也可以充当画笔，如果需要显示，则要包括SurfaceHolder，它的作用是获取和
 * 修改Surface的参数，比如大小、格式等。
 *
 * 12. OpenGL ES定义了一组渲染的API接口，绘制图形使用GLES调用，将它们渲染在屏幕上则使用EGL调用。
 * ANativeWindow充当Java层的Surface类，创建native层的EGL window surface界面。
 *
 * 13. Vulkan新一代的OpenGL绘图接口。
 *
 * 14. 高层次组件
 * SurfaceView和GLSurfaceView，SurfaceView是Surface和View的组合，View是通过SurfaceFlinger
 * 合成的。
 *
 * 15. SurfaceTexture (关键)
 * combines a Surface and GLES texture to create a BufferQueue for which you app
 * is the consumer.
 * When a producer queues a new buffer, it notifies your app, which in turn releases
 * the previously-held buffer, acquires the new buffer from the queue, and
 * makes EGL calls to make the buffer available to GLES as an external texture.
 *
 * 16. TextureView 是View和SurfaceTexture的组合。
 * TextureView wraps a SurfaceTexture and takes responsibility for responding
 * to callbacks an acquiring new buffers.
 *
 * 17. SurfaceView的消费者是SurfaceFlinger，SurfaceTexture的消费者是app程序，TextureView是最复杂的。
 *
 * 18. 各组件详细介绍
 * BufferQueue: The producer requests a free buffer (dequeueBuffer()), specifying a
 * set of characteristics including width, height, pixel format, and usage flags.
 * The producer populates the buffer and returns it to the queue(queueBuffer()).
 * Later, the consumer acquires the buffer(acquireBuffer()) and makes use of the buffer
 * contents.
 * When the consumer is done, it returns the buffer to the queue(releaseBuffer()).
 * 目前总是消费者创建和拥有BufferQueue结构体。
 *
 * 19. 当app处于要处于可见状态时，WindowManager服务要求SurfaceFlinger进行窗口绘制。
 * SurfaceFlinger充当消费者，创建BufferQueue，一个Binder会通过WindowManager传递给app，它
 * 可以将frames送入到SurfaceFlinger中。
 *
 * 大部分的UI有三层: status bar, navigation bar, application ui，
 * 设备显示通常是60 frames/second, After SurfaceFlinger has collected all buffers for
 * visible layers, it asks the Hardware Composer how composition should be performed.
 *
 * 20. Surface
 * The Surface represents the producer side of a buffer queue that is often (but not always!) consumed by SurfaceFlinger.
 * When you render onto a Surface, the result ends up in a buffer that gets shipped to the consumer.
 * A Surface is not simply a raw chunk of memory you can scribble on.
 * 通过dumpsys SurfaceFlinger查看。
 *
 * 21. Surface Holder
 * APIs to get and set Surface parameters, such as the size and format, are implemented through SurfaceHolder.
 *
 * 22. EGLSurfaces
 * EGL window surfaces are created with the eglCreateWindowSurface() call.
 * It takes a "window object" as an argument, which on Android can be a SurfaceView,
 * a SurfaceTexture, a SurfaceHolder, or a Surface -- all of which have a BufferQueue underneath.
 * 使用OpenGL ES 将图片绘制到EGLSurface上面，eglCreateWindowSurface会将Surface传递进来，而这里的Surface正好是
 * 通过{@MediaCodec#createInputSurface();}获取到。
 *
 * 23. ANativeWindow
 * To create an EGL window surface from native code, you pass an instance of EGLNativeWindowType
 * to eglCreateWindowSurface(). EGLNativeWindowType is just a synonym for ANativeWindow,
 * so you can freely cast one to the other.
 *
 * 24. SurfaceView
 * 拥有独立的绘图表面，不会占用主线程资源，实现复杂而高效的UI界面。
 * Whatever you render onto this Surface will be composited by SurfaceFlinger, not by the app.
 * This is the real power of SurfaceView: The Surface you get can be rendered by a separate
 * thread or a separate process, isolated from any rendering performed by the app UI,
 * and the buffers go directly to SurfaceFlinger.
 *
 * 25. SurfaceTexture
 * When you create a SurfaceTexture, you are creating a BufferQueue for which your
 * app is the consumer.
 * When a new buffer is queued by the producer, you app is notified via
 * callback(onFrameAvailable()).
 * You app calls updateTexImage(), which releases the previously-held buffer,
 * acquires the new buffer from the queue, and makes some EGL calls to make the buffer
 * available to GLES an an external texture.
 *
 * 26. 时间戳和转换
 * Each buffer is accompanied by a timestamp and transformation parameters.
 * 通过setPreviewTexture()设置相机的输出。
 * To create a video, you need to set the presentation timestamp for each frame.
 * The timestamp provided with the buffer is set by the camera code, resulting
 * in a more consistent series of timestamps.
 *
 * 27. SurfaceTexture和Surface
 * Surface只能通过Surface(SurfaceTexture surfaceTexture)创建。
 * Under the hood, SurfaceTexture is called GLConsumer, which more accurately reflects its
 * role as the owner and consumer of a BufferQueue.
 *
 * 28. TextureView
 * TextureView和SurfaceView有相同点和不同点，终极目标是弄清楚它们。
 * 加油，往多媒体方向发展。
 *
 *
 *
 */

/**
 * 范例：录制屏幕
 * https://android.googlesource.com/platform/frameworks/av/cmds/screenrecord/
 * The screenrecord command allows you to record everything that appears on the screen as an .mp4 file on disk. To implement, we have to receive composited frames from SurfaceFlinger, write them to the video encoder, and then write the encoded video data to a file. The video codecs are managed by a separate process (mediaserver) so we have to move large graphics buffers around the system. To make it more challenging, we're trying to record 60fps video at full resolution. The key to making this work efficiently is BufferQueue.
 * The MediaCodec class allows an app to provide data as raw bytes in buffers, or through a Surface. When screenrecord requests access to a video encoder, mediaserver creates a BufferQueue, connects itself to the consumer side, then passes the producer side back to screenrecord as a Surface.
 * The screenrecord command then asks SurfaceFlinger to create a virtual display that mirrors the main display (i.e. it has all of the same layers), and directs it to send output to the Surface that came from mediaserver. In this case, SurfaceFlinger is the producer of buffers rather than the consumer.
 * After the configuration is complete, screenrecord waits for encoded data to appear. As apps draw, their buffers travel to SurfaceFlinger, which composites them into a single buffer that gets sent directly to the video encoder in mediaserver. The full frames are never even seen by the screenrecord process. Internally, mediaserver has its own way of moving buffers around that also passes data by handle, minimizing overhead.
 *
 * 范例：Grafika's continuous capture
 * The camera can provide a stream of frames suitable for recording as a movie. To display it on screen, you create a SurfaceView, pass the Surface to setPreviewDisplay(), and let the producer (camera) and consumer (SurfaceFlinger) do all the work. To record the video, you create a Surface with MediaCodec's createInputSurface(), pass that to the camera, and again sit back and relax. To show and record the it at the same time, you have to get more involved.
 * The continuous capture activity displays video from the camera as the video is being recorded. In this case, encoded video is written to a circular buffer in memory that can be saved to disk at any time. It's straightforward to implement so long as you keep track of where everything is.
 *
 */

public class CameraRawActivity extends Activity {

    SurfaceTexture mSurfaceTexture;
    Surface mSurface;

    SurfaceView mSurfaceView;

    public static final String TAG = "CameraRawActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

package com.android.approjects.permissions.camera;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.provider.SyncStateContract;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.android.approjects.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CameraView extends FrameLayout {

    /**
     * The camera device faces the opposite direction as the device's screen.
     */
    public static final int FACING_BACK = Constants.FACING_BACK;

    /** The camera device faces the same direction as the device's screen. */
    public static final int FACING_FRONT = Constants.FACING_FRONT;

    /** Direction the camera faces relative to device screen. */
    @IntDef({FACING_BACK, FACING_FRONT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Facing {
    }

    /** Flash will not be fired. */
    public static final int FLASH_OFF = Constants.FLASH_OFF;

    /** Flash will always be fired during snapshot. */
    public static final int FLASH_ON = Constants.FLASH_ON;

    /** Constant emission of light during preview, auto-focus and snapshot. */
    public static final int FLASH_TORCH = Constants.FLASH_TORCH;

    /** Flash will be fired automatically when required. */
    public static final int FLASH_AUTO = Constants.FLASH_AUTO;

    /** Flash will be fired in red-eye reduction mode. */
    public static final int FLASH_RED_EYE = Constants.FLASH_RED_EYE;

    /** The mode for for the camera device's flash control */
    @IntDef({FLASH_OFF, FLASH_ON, FLASH_TORCH, FLASH_AUTO, FLASH_RED_EYE})
    public @interface Flash {
    }

    CameraViewImpl mImpl;

    private final CallbackBridge mCallbacks;

    private boolean mAdjustViewBounds;

    private final DisplayOrientationDetector mDisplayOrientationDetector;

    public CameraView(@NonNull Context context) {
        this(context, null);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (isInEditMode()) {
            mCallbacks = null;
            mDisplayOrientationDetector = null;
            return ;
        }

        // Internal setup
        final PreviewImpl preview = createPreviewImpl(context);
        mCallbacks = new CallbackBridge();

        // 分几种实现
        mImpl = new Camera2Api(mCallbacks, preview, context);

        // Attributes
        // 前面定义，后面赋值。
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.CameraView, defStyleAttr, R.style.Widget_CameraView);

        // Display orientation detector.
        mDisplayOrientationDetector = new DisplayOrientationDetector(context) {

            @Override
            public void onDisplayOrientationChanged(int displayOrientation) {

            }
        };
    }

    private PreviewImpl createPreviewImpl(Context context) {
        PreviewImpl preview;

        preview = new TextureViewPreview(context, this);

        return preview;
    }

    private class CallbackBridge implements CameraViewImpl.Callback {

        @Override
        public void onCameraOpened() {

        }

        @Override
        public void onCameraClosed() {

        }

        @Override
        public void onPictureTaken() {

        }
    }
}

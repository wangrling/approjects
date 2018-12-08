package com.android.approjects.permissions.camera;

import android.content.Context;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.android.approjects.R;

class TextureViewPreview extends PreviewImpl {

    private final TextureView mTextureView;

    private int mDisplayOrientation;

    public TextureViewPreview(Context context, ViewGroup parent) {
        final View view = View.inflate(context, R.layout.texture_view, parent);
        mTextureView = view.findViewById(R.id.texture_view);
    }
}

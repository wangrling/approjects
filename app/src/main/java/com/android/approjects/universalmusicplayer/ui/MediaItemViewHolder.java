package com.android.approjects.universalmusicplayer.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.approjects.R;
import com.android.approjects.universalmusicplayer.utils.MediaIDHelper;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class MediaItemViewHolder {

    public static final int STATE_INVALID = -1;
    public static final int STATE_NONE = 0;
    public static final int STATE_PLAYABLE = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_PLAYING = 3;

    private static ColorStateList sColorStatePlaying;
    private static ColorStateList sColorStateNotPlaying;

    private ImageView mImageView;
    private TextView mTitleView;
    private TextView mDescriptionView;

    // Returns a view for use in media item list.
    static View setupListView(Activity activity, View convertView, ViewGroup parent,
                              MediaBrowserCompat.MediaItem item) {

        if (sColorStateNotPlaying == null || sColorStatePlaying == null) {
            initializeColorStateLists(activity);
        }

        MediaItemViewHolder holder;

        Integer cachedState = STATE_INVALID;

        if (convertView == null) {
            convertView = LayoutInflater.from(activity)
                    .inflate(R.layout.media_list_item, parent, false);
            holder = new MediaItemViewHolder();
            holder.mImageView = convertView.findViewById(R.id.play_eq);
            holder.mTitleView = convertView.findViewById(R.id.title);
            holder.mDescriptionView = convertView.findViewById(R.id.description);
            convertView.setTag(holder);
        } else {
            holder = (MediaItemViewHolder) convertView.getTag();
            cachedState = (Integer) convertView.getTag(R.id.tag_mediaitem_state_cache);
        }

        MediaDescriptionCompat description = item.getDescription();
        holder.mTitleView.setText(description.getTitle());
        holder.mDescriptionView.setText(description.getSubtitle());

        // If the state of convertView is different, we need to adapt the view to the
        // new state.
        // 监听状态的变化，在播放的时候会显示动态的播放界面。
        int state = getMediaItemState(activity, item);

        if (cachedState == null || cachedState != state) {
            Drawable drawable = getDrawableByState(activity, state);
            if (drawable != null) {
                holder.mImageView.setImageDrawable(drawable);
                holder.mImageView.setVisibility(View.VISIBLE);
            } else {
                holder.mImageView.setVisibility(View.GONE);
            }
            convertView.setTag(R.id.tag_mediaitem_state_cache, state);
        }

        return convertView;
    }

    private static void initializeColorStateLists(Context context) {
        sColorStateNotPlaying = ColorStateList.valueOf(context.getResources().getColor(
                R.color.colorPrimary, null));
        sColorStatePlaying = ColorStateList.valueOf(context.getResources().getColor(
                R.color.colorAccent, null));
    }

    private static Drawable getDrawableByState(Context context, int state) {
        if (sColorStateNotPlaying == null || sColorStatePlaying == null) {
            initializeColorStateLists(context);
        }

        switch (state) {
            case STATE_PLAYABLE:
                Drawable pauseDrawable = ContextCompat.getDrawable(context,
                        R.drawable.ic_play_arrow_black_36dp);
                DrawableCompat.setTintList(pauseDrawable, sColorStateNotPlaying);
                return pauseDrawable;
            // 动画效果
            case STATE_PLAYING:
                AnimationDrawable animation = (AnimationDrawable)
                        ContextCompat.getDrawable(context, R.drawable.ic_equalizer_white_36dp);
                DrawableCompat.setTintList(animation, sColorStatePlaying);
                animation.start();
                return animation;
            case STATE_PAUSED:
                Drawable playDrawable = ContextCompat.getDrawable(context,
                        R.drawable.ic_equalizer1_white_36dp);
                DrawableCompat.setTintList(playDrawable, sColorStatePlaying);
                return playDrawable;
            default:
                return null;
        }
    }


    public static int getMediaItemState(Activity context, MediaBrowserCompat.MediaItem mediaItem) {
        int state = STATE_NONE;

        // Set state to playable first, then override to playing or paused state if needed.
        if (mediaItem.isPlayable()) {
            state = STATE_PLAYABLE;

            // 奇怪的写法
            if (MediaIDHelper.isMediaItemPlaying(context, mediaItem)) {
                state = getStateFromController(context);
            }
        }

        return state;
    }

    public static int getStateFromController(Activity context) {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(context);
        PlaybackStateCompat pbState = controller.getPlaybackState();
        if (pbState == null ||
                pbState.getState() == PlaybackStateCompat.STATE_ERROR) {
            return MediaItemViewHolder.STATE_NONE;
        } else if (pbState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            return MediaItemViewHolder.STATE_PLAYING;
        } else {
            return MediaItemViewHolder.STATE_PAUSED;
        }
    }
}

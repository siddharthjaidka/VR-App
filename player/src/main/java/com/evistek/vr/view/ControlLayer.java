package com.evistek.vr.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.evistek.vr.R;
import org.rajawali3d.vr.renderer.GvrVideoRenderer;


/**
 * Created by evis on 2016/9/5.
 */
public class ControlLayer {

    private static final String DEFAULT_TIME = "00:00:00";

    private Context mContext;
    private LayoutInflater mInflater;
    private View mLayoutView;
    private RelativeLayout mRelativeLayoutLeft, mRelativeLayoutRight;
    private ImageButton mModeButtonLeft, mModeButtonRight;
    private SeekBar mSeekBarLeft, mSeekBarRight;
    private ProgressBar mProgressBarLeft, mProgressBarRight;
    private TextView mElapsedTimeLeft, mElapsedTimeRight;
    private TextView mTotalTimeLeft, mTotalTimeRight;
    private ImageView mPauseViewLeft, mPauseViewRight;

    public ControlLayer(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        initView();
    }

    public View getView() {
        return mLayoutView;
    }

    public void setSeekBarEnable(boolean enable) {
        mSeekBarLeft.setEnabled(enable);
        mSeekBarRight.setEnabled(enable);
    }

    public void setSeekBarMax(int max) {
        mSeekBarLeft.setMax(max);
        mSeekBarRight.setMax(max);
    }

    public void setSeekBarProgress(int progress) {
        mSeekBarLeft.setProgress(progress);
        mSeekBarRight.setProgress(progress);
    }

    public void setSeekBarSecondaryProgress(int progress) {
        mSeekBarLeft.setSecondaryProgress(progress);
        mSeekBarRight.setSecondaryProgress(progress);
    }

    public void setElapsedTime(String elapsedTime) {
        mElapsedTimeLeft.setText(elapsedTime);
        mElapsedTimeRight.setText(elapsedTime);
    }

    public void setTotalTime(String totalTime) {
        mTotalTimeLeft.setText(totalTime);
        mTotalTimeRight.setText(totalTime);
    }

    public void setProgressBarVisibility(int visibility) {
        mProgressBarLeft.setVisibility(visibility);
        mProgressBarRight.setVisibility(visibility);
    }

    public void setPauseViewVisibility(int visibility) {
        if (visibility == View.VISIBLE && mLayoutView.getVisibility() == View.INVISIBLE) {
            mLayoutView.setVisibility(View.VISIBLE);
        }
        mPauseViewLeft.setVisibility(visibility);
        mPauseViewRight.setVisibility(visibility);
    }

    public void setVisibility(int visibility) {
        mLayoutView.setVisibility(visibility);
    }

    public int getVisibility() {
        return mLayoutView.getVisibility();
    }

    public void setModeButtonClickListener(View.OnClickListener listener) {
        mModeButtonLeft.setOnClickListener(listener);
        mModeButtonRight.setOnClickListener(listener);
    }

    public void setSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
        mSeekBarLeft.setOnSeekBarChangeListener(listener);
        mSeekBarRight.setOnSeekBarChangeListener(listener);
    }

    public void updateLayout(int playMode) {
        switch (playMode) {
            case GvrVideoRenderer.PLAY_MODE_VR:
            case GvrVideoRenderer.PLAY_MODE_3D:
                if (!mRelativeLayoutRight.isShown()) {
                    mRelativeLayoutRight.setVisibility(View.VISIBLE);
                }
                break;
            case GvrVideoRenderer.PLAY_MODE_2D:
            case GvrVideoRenderer.PLAY_MODE_FULLSCREEN:
                if (mRelativeLayoutRight.isShown()) {
                    mRelativeLayoutRight.setVisibility(View.GONE);
                }
                break;
        }

        updateButtonView(playMode);
    }

    public void updateButtonView(int playMode) {
        switch (playMode) {
            case GvrVideoRenderer.PLAY_MODE_FULLSCREEN:
                mModeButtonLeft.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_fullscreen));
                mModeButtonRight.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_fullscreen));
                break;
            case GvrVideoRenderer.PLAY_MODE_3D:
                mModeButtonLeft.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_3d));
                mModeButtonRight.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_3d));
                break;
            case GvrVideoRenderer.PLAY_MODE_2D:
                mModeButtonLeft.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_2d));
                mModeButtonRight.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_2d));
                break;
            case GvrVideoRenderer.PLAY_MODE_VR:
                mModeButtonLeft.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_vr));
                mModeButtonRight.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_vr));
                break;
            default:
                break;
        }
    }

    private void initView() {
        mLayoutView = mInflater.inflate(R.layout.video_controller_vr, null);

        mRelativeLayoutLeft = (RelativeLayout) mLayoutView.findViewById(R.id.relativeLayout_l);
        mRelativeLayoutRight = (RelativeLayout) mLayoutView.findViewById(R.id.relativeLayout_r);
        mModeButtonLeft = (ImageButton) mLayoutView.findViewById(R.id.vrButton_l);
        mModeButtonRight = (ImageButton) mLayoutView.findViewById(R.id.vrButton_r);
        mSeekBarLeft = (SeekBar) mLayoutView.findViewById(R.id.videoSeekBar_l);
        mSeekBarRight = (SeekBar) mLayoutView.findViewById(R.id.videoSeekBar_r);
        mProgressBarLeft = (ProgressBar) mLayoutView.findViewById(R.id.loadingProgressbar_l);
        mProgressBarRight = (ProgressBar) mLayoutView.findViewById(R.id.loadingProgressbar_r);
        mElapsedTimeLeft = (TextView) mLayoutView.findViewById(R.id.elapsedTime_l);
        mElapsedTimeRight = (TextView) mLayoutView.findViewById(R.id.elapsedTime_r);
        mTotalTimeLeft = (TextView) mLayoutView.findViewById(R.id.totalTime_l);
        mTotalTimeRight = (TextView) mLayoutView.findViewById(R.id.totalTime_r);
        mPauseViewLeft = (ImageView) mLayoutView.findViewById(R.id.pause_l);
        mPauseViewRight = (ImageView) mLayoutView.findViewById(R.id.pause_r);

        mElapsedTimeLeft.setText(DEFAULT_TIME);
        mElapsedTimeRight.setText(DEFAULT_TIME);
        mTotalTimeLeft.setText(DEFAULT_TIME);
        mTotalTimeRight.setText(DEFAULT_TIME);
        mSeekBarLeft.setEnabled(false);
        mSeekBarRight.setEnabled(false);
        mPauseViewLeft.setVisibility(View.GONE);
        mPauseViewRight.setVisibility(View.GONE);
    }

}

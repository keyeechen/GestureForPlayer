package com.example.gesturedemoforplayer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.gesturedemoforplayer.util.DensityUtil;

/**
 * 实现播放界面的横向调节进度，纵向调节音量（注：未添加播放功能）
 * 
 * @author keyeechen
 * 
 */
public class MainActivity extends Activity implements OnGestureListener, OnTouchListener {
	private RelativeLayout root_layout;// 根布局
	private RelativeLayout gesture_volume_layout;// 音量控制布局
	private TextView geture_tv_volume_percentage;// 音量百分比
	private ImageView gesture_iv_player_volume;// 音量图标
	private RelativeLayout gesture_progress_layout;// 进度图标
	private TextView geture_tv_progress_time;// 播放时间进度
	private ImageView gesture_iv_progress;// 快进或快退标志
	private GestureDetector gestureDetector;
	private AudioManager audiomanager;
	private int maxVolume, currentVolume;
	private static final float STEP_PROGRESS = 2f;// 设定进度滑动时的步长，避免每次滑动都改变，导致改变过快
	private static final float STEP_VOLUME = 2f;// 协调音量滑动时的步长，避免每次滑动都改变，导致改变过快
	private boolean firstScroll = false;// 每次触摸屏幕后，第一次scroll的标志
	private int GESTURE_FLAG = 0;// 1,调节进度，2，调节音量
	private static final int GESTURE_MODIFY_PROGRESS = 1;
	private static final int GESTURE_MODIFY_VOLUME = 2;
	private long palyerCurrentPosition = 200 * 1000;// 模拟进度播放的当前标志，毫秒
	private long playerDuration = 4500 * 1000;// 模拟播放资源的时长，毫秒

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		root_layout = (RelativeLayout) findViewById(R.id.root_layout);
		gesture_volume_layout = (RelativeLayout) findViewById(R.id.gesture_volume_layout);
		gesture_progress_layout = (RelativeLayout) findViewById(R.id.gesture_progress_layout);
		geture_tv_progress_time = (TextView) findViewById(R.id.geture_tv_progress_time);
		geture_tv_volume_percentage = (TextView) findViewById(R.id.geture_tv_volume_percentage);
		gesture_iv_progress = (ImageView) findViewById(R.id.gesture_iv_progress);
		gesture_iv_player_volume = (ImageView) findViewById(R.id.gesture_iv_player_volume);
		gestureDetector = new GestureDetector(this, this);
		root_layout.setLongClickable(true);
		gestureDetector.setIsLongpressEnabled(true);
		root_layout.setOnTouchListener(this);
		audiomanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		maxVolume = audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
		currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		// 手势里除了singleTapUp，没有其他检测up的方法
		if (event.getAction() == MotionEvent.ACTION_UP) {
			GESTURE_FLAG = 0;// 手指离开屏幕后，重置调节音量或进度的标志
			gesture_volume_layout.setVisibility(View.INVISIBLE);
			gesture_progress_layout.setVisibility(View.INVISIBLE);
		}
		return gestureDetector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		firstScroll = true;// 设定是触摸屏幕后第一次scroll的标志
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		if (firstScroll) {// 以触摸屏幕后第一次滑动为标准，避免在屏幕上操作切换混乱
			// 横向的距离变化大则调整进度，纵向的变化大则调整音量
			if (Math.abs(distanceX) >= Math.abs(distanceY)) {
				gesture_volume_layout.setVisibility(View.INVISIBLE);
				gesture_progress_layout.setVisibility(View.VISIBLE);
				GESTURE_FLAG = GESTURE_MODIFY_PROGRESS;
			} else {
				gesture_volume_layout.setVisibility(View.VISIBLE);
				gesture_progress_layout.setVisibility(View.INVISIBLE);
				GESTURE_FLAG = GESTURE_MODIFY_VOLUME;
			}
		}
		// 如果每次触摸屏幕后第一次scroll是调节进度，那之后的scroll事件都处理音量进度，直到离开屏幕执行下一次操作
		if (GESTURE_FLAG == GESTURE_MODIFY_PROGRESS) {
			// distanceX=lastScrollPositionX-currentScrollPositionX，因此为正时是快进
			if (Math.abs(distanceX) > Math.abs(distanceY)) {// 横向移动大于纵向移动
				if (distanceX >= DensityUtil.dip2px(this, STEP_PROGRESS)) {// 快退，用步长控制改变速度，可微调
					gesture_iv_progress.setImageResource(R.drawable.souhu_player_backward);
					if (palyerCurrentPosition > 3 * 1000) {// 避免为负
						palyerCurrentPosition -= 3 * 1000;// scroll方法执行一次快退3秒
					} else {
						palyerCurrentPosition = 3 * 1000;
					}
				} else if (distanceX <= -DensityUtil.dip2px(this, STEP_PROGRESS)) {// 快进
					gesture_iv_progress.setImageResource(R.drawable.souhu_player_forward);
					if (palyerCurrentPosition < playerDuration - 16 * 1000) {// 避免超过总时长
						palyerCurrentPosition += 3 * 1000;// scroll执行一次快进3秒
					} else {
						palyerCurrentPosition = playerDuration - 10 * 1000;
					}
				}
			}

			geture_tv_progress_time.setText(converLongTimeToStr(palyerCurrentPosition) + "/"
					+ converLongTimeToStr(playerDuration));

		}
		// 如果每次触摸屏幕后第一次scroll是调节音量，那之后的scroll事件都处理音量调节，直到离开屏幕执行下一次操作
		else if (GESTURE_FLAG == GESTURE_MODIFY_VOLUME) {
			currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
			if (Math.abs(distanceY) > Math.abs(distanceX)) {// 纵向移动大于横向移动
				if (distanceY >= DensityUtil.dip2px(this, STEP_VOLUME)) {// 音量调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
					if (currentVolume < maxVolume) {// 为避免调节过快，distanceY应大于一个设定值
						currentVolume++;
					}
					gesture_iv_player_volume.setImageResource(R.drawable.souhu_player_volume);
				} else if (distanceY <= -DensityUtil.dip2px(this, STEP_VOLUME)) {// 音量调小
					if (currentVolume > 0) {
						currentVolume--;
						if (currentVolume == 0) {// 静音，设定静音独有的图片
							gesture_iv_player_volume.setImageResource(R.drawable.souhu_player_silence);
						}
					}
				}
				int percentage = (currentVolume * 100) / maxVolume;
				geture_tv_volume_percentage.setText(percentage + "%");
				audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
			}

		}

		firstScroll = false;// 第一次scroll执行完成，修改标志
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 转换毫秒数成“分、秒”，如“01:53”。若超过60分钟则显示“时、分、秒”，如“01:01:30
	 * 
	 * @param 待转换的毫秒数
	 * */
	private String converLongTimeToStr(long time) {
		int ss = 1000;
		int mi = ss * 60;
		int hh = mi * 60;

		long hour = (time) / hh;
		long minute = (time - hour * hh) / mi;
		long second = (time - hour * hh - minute * mi) / ss;

		String strHour = hour < 10 ? "0" + hour : "" + hour;
		String strMinute = minute < 10 ? "0" + minute : "" + minute;
		String strSecond = second < 10 ? "0" + second : "" + second;
		if (hour > 0) {
			return strHour + ":" + strMinute + ":" + strSecond;
		} else {
			return strMinute + ":" + strSecond;
		}
	}

}

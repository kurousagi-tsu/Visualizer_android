package com.example.fft_test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.media.*;
import android.media.MediaPlayer.OnCompletionListener;
import java.util.*;

import android.media.audiofx.Visualizer;
import com.physicaloid.lib.Boards;
import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.Physicaloid.UploadCallBack;
import com.physicaloid.lib.programmer.avr.UploadErrors;
import com.physicaloid.lib.usb.driver.uart.ReadLisener;
//import com.physicaloid.lib.usb.driver.uart.UartConfig;

import com.example.fft_test.DrawColumns;


public class MainActivity extends Activity
	implements OnClickListener, OnCompletionListener {
	
	private static final String TAG = MainActivity.class.getSimpleName();

	private static final byte COMMAND_FIRST1 = (byte)((short)114 & 0xff);
	private static final byte COMMAND_FIRST2 = (byte)((short)35 & 0xff);
	private static final byte COMMAND_FIRST3 = (byte)((short)237 & 0xff);
	private static final byte COMMAND_END = (byte)((short)0xCB & 0xff);
	
	public static final int SAMPLE_RATE = 44100;
	
	
	public MediaPlayer _mp;
	//User Interface
	private Button _play_btn = null;
	private Button _send_btn = null;
	private Button _prog_btn = null;
	private Button _red_btn = null;
	private Button _green_btn = null;
	private Button _blue_btn = null;
	private SeekBar mSeek = null;
	public TextView mTextView;
	//Column Draw
	private DrawColumns mDrawColumns = null;
	//Timer
	private Timer   mTimer   = null;
	public Handler mHandler = new Handler();
	//Physicaloid
	private Physicaloid mPhysicaloid;
	//Visualizer
	public Visualizer mVisualizer;
	//LogTextView
	public LogTextView _ltv;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"onCreate Start");
		
		setContentView(R.layout.activity_main);
		
		mTextView = (TextView) findViewById(R.id.textView1);
		setupSeekbar();
		setupButton();
		setupMediaPlayer();
		setupVisualizer();
		_ltv = new LogTextView();
		mDrawColumns = (com.example.fft_test.DrawColumns) findViewById(R.id.view1);
		mPhysicaloid = new Physicaloid(this);

		String tmp = _ltv.Get();
		mTextView.setText(tmp);
		
		Log.d(TAG,"onCreate Finish");
	}
	
	public void setupMediaPlayer()
	{
		if(_mp!=null) _mp.release();
		
		_mp = MediaPlayer.create(getApplicationContext(), R.raw.music);
		_mp.setOnCompletionListener(this);
	}
	public void onStart() {
		super.onStart();

	}
	
	public void onStop() {
		super.onStop();
		if (_mp != null) {
			mVisualizer.release();
			if (_mp.isPlaying()) {
				_mp.stop();
			}
			_mp.release();
		}
	}
	
	public void setupSeekbar()
	{
		mSeek = (SeekBar) findViewById(R.id.seekBar1);
		mSeek.setMax(255);
		mSeek.setProgress(255);
		mSeek.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser) {
				//動かすと呼ばれる
				if (_mp.isPlaying()) return;
				for(int i=0; i<mDrawColumns.num; i++){
					mDrawColumns.setValue(i, progress);
				}
				SendPacket();
				mDrawColumns.invalidate();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				// 触れると呼ばれる
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				// 離すと呼ばれる
				seekBar.setSecondaryProgress(seekBar.getProgress());
			}
		});
	}
	
	public void setupVisualizer()
	{
		Log.d(TAG,"setupVisualizer Start");
		Log.d(TAG,"Object generate");
		Log.d(TAG, "ID : "+_mp.getAudioSessionId());

		try{
			mVisualizer = new Visualizer(_mp.getAudioSessionId());
			mVisualizer.setEnabled(false);
			
			Log.d(TAG,"set capture size");
			mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

			Log.d(TAG,"resist function");
			mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {

				@Override
				public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
						int samplingRate) {
					// Auto-generated method stub
				}

				@Override
				public void onFftDataCapture(Visualizer visualizer, byte[] fft,
						int samplingRate) {
					//Auto-generated method stub
					//Log.d(TAG, "data_num : "+fft.length);
					//Log.d(TAG, "sampling_rate : "+samplingRate);
					
					int fft_amp [];
					fft_amp = new int[513];
					
					for(int i=0; i<512; i++){
						if(i==0) fft_amp[0] = (fft[i]<0) ? -1*fft[i] : fft[i];
						if(i==1) fft_amp[512] = (fft[i]<0) ? -1*fft[i] : fft[i];
						else if(i%2 == 0){
							int tmp = fft[i]*fft[i] + fft[i+1]*fft[i+1];
							fft_amp[i/2 + 1] = (int)(Math.sqrt(tmp));
						}
					}
					
					for(int i=0; i<mDrawColumns.num; i++){
						//Log.d(TAG, "["+i+"] : "+fft_amp[i]);
						mDrawColumns.setValue(i, (int)(fft_amp[i]*1.6));						
					}
					mDrawColumns.invalidate();
					
					SendPacket();
				}
			}, Visualizer.getMaxCaptureRate()/2, false,true);

		}
		catch(Exception ex)
		{
			Log.e("Visual Ex", ex.getMessage());
		}
		
		Log.d(TAG,"setupVisualizer Finish");
	}

	public void setupTimer()
	{
		if(mTimer == null){
			mTimer = new Timer(true);
			
			mTimer.schedule( new TimerTask(){
				@Override
				public void run() {
					// mHandlerを通じてUI Threadへ処理をキューイング
					mHandler.post( new Runnable() {
						public void run() {
							//SendPacket();

						}
					});
				}
			}, 100, 100);
		}
	}
	
	public void stopTimer(){
		if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
	}
	
	public void setupButton()
	{
		_play_btn = (Button) findViewById(R.id.play_btn);
		_play_btn.setOnClickListener(this);
		
		_send_btn = (Button) findViewById(R.id.send_btn);
		_send_btn.setOnClickListener(this);
		
		_prog_btn = (Button) findViewById(R.id.prog_btn);
		_prog_btn.setOnClickListener(this);
		
		_red_btn = (Button) findViewById(R.id.red_btn);
		_red_btn.setOnClickListener(this);
		
		_green_btn = (Button) findViewById(R.id.green_btn);
		_green_btn.setOnClickListener(this);
		
		_blue_btn = (Button) findViewById(R.id.blue_btn);
		_blue_btn.setOnClickListener(this);
	}
	
	public void onClick(View v) {
		Button btn = (Button) v;
		if(btn.getId()==R.id.play_btn){
			PlayBtnClick();
		}
		else if(btn.getId()==R.id.send_btn){
			SendBtnClick();
		}
		else if(btn.getId()==R.id.prog_btn){
			ProgBtnClick();
		}
		else if(btn.getId()==R.id.red_btn){
			RedBtnClick();
		}
		else if(btn.getId()==R.id.green_btn){
			GreenBtnClick();
		}
		else if(btn.getId()==R.id.blue_btn){
			BlueBtnClick();
		}
		
	}
	
	public void PlayBtnClick(){
		if (_mp.isPlaying()) {
			_mp.pause();
			mVisualizer.setEnabled(false);
			_play_btn.setText("Play");
			
		} else {
			_mp.seekTo(0);
			_mp.start();
			mVisualizer.setEnabled(true);
			_play_btn.setText("Stop");
			
		}
	}
	
	public void SendBtnClick(){
		if (mPhysicaloid.isOpened()) {
			stopTimer();
			
			if(mPhysicaloid.close()){
				_send_btn.setText("Connect");
				SetText(mTextView, "the device is closed.");
			}
		}
		else{
			if(mPhysicaloid.open()){// default 9600bps
				mPhysicaloid.setBaudrate(38400);
				_send_btn.setText("DisConn");
				SetText(mTextView, "An FTDI device is opened.");		
				
				mPhysicaloid.addReadListener(new ReadLisener() {
	                String readStr;

	                // callback when reading one or more size buffer
	                @Override
	                public void onRead(int size) {
	                    byte[] buf = new byte[size];

	                    mPhysicaloid.read(buf, size);
	                    try {
	                        readStr = new String(buf, "UTF-8");
	                    } catch (UnsupportedEncodingException e) {
	                        Log.e(TAG,e.toString());
	                        return;
	                    }
	                }
	            });
				
				setupTimer();
			}	
		}
	}
	
	private void SetText(TextView tv, String text){
		_ltv.Add(text);
		String tmp = _ltv.Get();
		tvAppend(tv, tmp);
	}
	
	Handler mHandler2 = new Handler();
	private void tvAppend(TextView tv, CharSequence text){
		final TextView ftv = tv;
		final CharSequence ftext = text;
		mHandler2.post(new Runnable() {
			@Override
			public void run() {
				ftv.setText(ftext);
			}
		});
	}

	public void RedBtnClick(){
		if (_mp.isPlaying()) return;
		for(int i=0; i<mDrawColumns.num; i++){
			mDrawColumns.setValue(i, 255);
		}
		SendPacket();
		mDrawColumns.invalidate();
		mSeek.setProgress(255);
	}
	public void GreenBtnClick(){
		if (_mp.isPlaying()) return;
		for(int i=0; i<mDrawColumns.num; i++){
			mDrawColumns.setValue(i, 127);
		}
		SendPacket();
		mDrawColumns.invalidate();
		mSeek.setProgress(127);
	}
	public void BlueBtnClick(){
		if (_mp.isPlaying()) return;
		for(int i=0; i<mDrawColumns.num; i++){
			mDrawColumns.setValue(i, 0);
		}
		SendPacket();
		mDrawColumns.invalidate();
		mSeek.setProgress(0);
	}
	

	//Physicaloid Upload
    private UploadCallBack mUploadCallback = new UploadCallBack() {
        @Override
        public void onPreUpload() {
            //tvAppend(tvRead, "Upload : Start\n");
        	Log.d(TAG,"Upload : Start");
        }

        @Override
        public void onUploading(int value) {
            //tvAppend(tvRead, "Upload : "+value+" %\n");
        	Log.d(TAG,"Upload : "+value+" %");
        	SetText(mTextView, "Upload : "+value+" %");
        }

        @Override
        public void onPostUpload(boolean success) {
            if(success) {
                //tvAppend(tvRead, "Upload : Successful\n");
            	Log.d(TAG,"Upload : Successful");
            	SetText(mTextView, "Upload : Successful");
            } else {
                //tvAppend(tvRead, "Upload fail\n");
            	Log.d(TAG,"Upload : fail");
            	SetText(mTextView, "Upload : fail\n");
            }
        }

        @Override
        public void onCancel() {
            //tvAppend(tvRead, "Cancel uploading\n");
        	Log.d(TAG,"Cancel uploading");
        	SetText(mTextView, "Cancel : uploading");
        }

        @Override
        public void onError(UploadErrors err) {
            //tvAppend(tvRead, "Error  : "+err.toString()+"\n");
        	Log.d(TAG,"Error  : "+err.toString());
        	SetText(mTextView, "Error  : "+err.toString());
        }
    };

	
	public void ProgBtnClick(){
		try {
			SetText(mTextView, "Upload : Start");
            mPhysicaloid.upload(Boards.POCKETDUINO, 
            	getResources().getAssets().open("neopixel.hex"), 
            	mUploadCallback
            );
        } catch (RuntimeException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
	}

	public void onCompletion(MediaPlayer arg0) {
		_play_btn.setText("Play");
		mVisualizer.setEnabled(false);
	}
	
	public byte short2byte(int x){
		
		x = (x > 255) ? 255 : x;
		x = (x < 0) ? 0 : x;
		return (byte)(x & 0xff);
	}
	
	public void SendPacket(){
		
		if(!mPhysicaloid.isOpened())return;
		
		//Log.d(TAG,"Send Packet");
		
		byte [] buf;
		int last_id = mDrawColumns.num + 3;
		buf = new byte[last_id+1];
		
		//first byte
		buf[0] = COMMAND_FIRST1;
		buf[1] = COMMAND_FIRST2;
		buf[2] = COMMAND_FIRST3;
		//information
		//buf[3] = short2byte(0x80 + mDrawColumns.num);
		
		for(int i=0; i<mDrawColumns.num; i++){
			buf[i+3] = short2byte(mDrawColumns.getValue(i));
		}
		buf[last_id] = COMMAND_END;
        mPhysicaloid.write(buf, buf.length);
 

	}

}

package com.example.fft_test;

import android.util.Log;

public class LogTextView{
	private static final String TAG = LogTextView.class.getSimpleName();
	
	public String buf[];
	int cur_buf_num;
	private static final int MAX_BUF_NUM = 7;

	
	public LogTextView(){
		
		cur_buf_num = MAX_BUF_NUM-1;
		buf = new String[MAX_BUF_NUM];
		for(int i=0; i<MAX_BUF_NUM; i++){
			buf[i] = "";
		}
		buf[0] = "Hello! If you can't see the description below this sentence,";
		buf[1] = "please change to the wide display tablet!";
		buf[2] = "<Usage>  (pocketduino : neopixel) => (VCC:5V),(GND:GND),(6:DI) You must connect three wires ";
		buf[3] = "First, Connect a pocketduino to the micro USB connecter of this tablet.";
		buf[4] = "Second, Click Connect button. You can see the message [An FTDI device is opened].";
		buf[5] = "Third, Click Load button. Please wait display [Upload : Sucessful].";
		buf[6] = "Click another button. Equalizer starts when the Play button is pushed.";
	}

	public void Add(String data){
		
		//Log.d(TAG, "Add function");
		//Log.d(TAG, "current buf num : "+cur_buf_num);
		//Log.d(TAG, "Data : "+data);
		if(cur_buf_num==MAX_BUF_NUM-1){
			for(int i=0; i<MAX_BUF_NUM-1; i++){
				buf[i] = "";
				buf[i] = buf[i+1];
			}
		}
		
		buf[cur_buf_num] = data;
		
		if(cur_buf_num<MAX_BUF_NUM-1) cur_buf_num++;
	}
	
	public String Get(){
		StringBuilder data = new StringBuilder();
		for(int i=0; i<MAX_BUF_NUM; i++){
			data.append(buf[i]);
			data.append("\n");
		}
		
		return String.valueOf(data);
	}

}

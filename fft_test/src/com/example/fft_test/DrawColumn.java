package com.example.fft_test;

//import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
//import android.view.View;
import android.util.Log;

public class DrawColumn{
	
	public int freq;
	public int amp;
	public int x_pos;
	public int rect_width;
	
	public int window_width;
	public int window_height;
	
	public static final int step_val = 11;
	public static final int step_num = 24;
	public static final int rect_height = 5;
	public static final int x_space = 5;
	public static final int y_space = 5;
	
	public DrawColumn(int t_freq, int t_amp) {
        freq = t_freq;		//1~20
        amp = t_amp;		//0~255
        window_width = 100;
        window_height = 100;
        
    }
	
	public void draw(Canvas canvas, int t_width, int t_height, int num){
		Paint paint = new Paint();
		paint.setStrokeWidth(2);//太さを2に
		
		window_width = t_width;
        window_height = t_height;
        rect_width = (window_width - x_space*(num+1)) / num;
        x_pos = (freq-1) * (rect_width + x_space) + x_space;
		
		if(canvas != null){
			for(int i=1; i<step_num; i++){
				int r, g, b;
				if(i==1){
					r = 0; g = 0; b = 255;
				}
				else if(i<12){
					r = 0; g = 23*i; b = 255-23*i;
				}
				else if(i==12){
					r = 0; g = 255; b = 0;
				}
				else if(i<23){
					r = 23*i; g = 255-23*i; b = 0;
				}
				else if(i==23){
					r = 255; g = 0; b = 0;
				}
				else{
					r = 0; g = 0; b = 0;
				}
				
				if(amp >= (i-1)*step_val){
					int y_pos = window_height - i*(rect_height + y_space);
					paint.setColor(Color.argb(255, r, g, b));//赤セット
                    canvas.drawRect(x_pos, y_pos, x_pos+rect_width, y_pos+rect_height, paint);//四角形描画
                    //Log.d("column", "x=" + x_pos + " y=" + y_pos ) ;
                    //Log.d("column", "w=" + rect_width + " h=" + rect_height ) ;
				}
			}
		}
	}

}


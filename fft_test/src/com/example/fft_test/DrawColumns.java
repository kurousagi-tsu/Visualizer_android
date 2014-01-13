package com.example.fft_test;

import android.content.*;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.*;
import android.util.*;

public class DrawColumns  extends View {
	
	public int num;
	public DrawColumn dcs[];
	
	public DrawColumns(Context context, AttributeSet attrs) {
        super(context, attrs);
        num = 20;
        InitColumns();
    }
	
	public void InitColumns(){
		dcs = new DrawColumn[num];
		for(int i=0; i< num; i++){
			dcs[i] = new DrawColumn(i+1, 255);
		}
	}
	
	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);
		Paint paint = new Paint();
		paint.setColor(Color.argb(255, 0, 0, 0)); //black
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);//ŽlŠpŒ`•`‰æ
		for(int i=0; i<num; i++){
			dcs[i].draw(canvas, getWidth(), getHeight(), num);
		}
	}
	
	public void setValue(int num, int value){
		dcs[num].amp = value;
	}
	
	public int getValue(int num){
		return dcs[num].amp;
	}

}

package com.ilearnrw.reader.types;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

public class ColorSquare extends View {

	private Paint paint;
	private Shader lg, lg2; 
	private ComposeShader shader;
	private final float[] color = { 1.0f, 1.0f, 1.0f };
	boolean updateColor = true;
	
	public ColorSquare(Context context){
		super(context);
	}
	
	public ColorSquare(Context context, AttributeSet attrs){
		super(context, attrs);
	}
	
	public ColorSquare(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if(paint==null)
		{
			paint = new Paint();
			lg = new LinearGradient(0.0f, 0.0f, 0.0f, this.getMeasuredHeight(), 0xffffffff, 0xff000000,  TileMode.CLAMP);
		}
		
		if(updateColor){
			int rgb = Color.HSVToColor(color);
			lg2 = new LinearGradient(0.0f, 0.0f, this.getMeasuredWidth(), 0.0f, 0xffffffff, rgb, TileMode.CLAMP);
			shader = new ComposeShader(lg, lg2, PorterDuff.Mode.MULTIPLY);
			updateColor = false;
		}
		
		paint.setShader(shader);
		canvas.drawRect(0.0f, 0.0f, this.getMeasuredWidth(), this.getMeasuredHeight(), paint);
	}
	
	public void setHue(float hue){
		color[0] = hue;
		updateColor=true;
		this.invalidate();
	}
}

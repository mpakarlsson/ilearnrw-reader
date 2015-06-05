package com.ilearnrw.reader.types;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class ColorPickerPreferenceWidget extends View{
	private Paint paint;
	private float rectSize;
	private float strokeWidth;
	
	public ColorPickerPreferenceWidget(Context context) {
		super(context);
		init(context);
	}
	
	public ColorPickerPreferenceWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ColorPickerPreferenceWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context){
		float density = context.getResources().getDisplayMetrics().density;
		rectSize 	= (float) Math.floor(24.0f * density + 0.5f);
		strokeWidth = (float) Math.floor(1.0f * density + 0.5f);
		
		paint = new Paint();
		paint.setColor(0xff000000);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(strokeWidth);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawRect(strokeWidth, strokeWidth, rectSize, rectSize , paint);
	}
}

package com.example.reader.types;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.reader.R;
import com.example.reader.interfaces.ColorPickerListener;

public class ColorPickerDialog  {
	public final AlertDialog dialog;
	public final ColorPickerListener listener;
	public final ColorSquare main;
	public final View oldColor, newColor, alphaOverlay;
	public final ImageView hue, cursor, alphaCursor, target, alphaCheckered;
	public final ViewGroup container;
	public final float[] currentColorHSV = new float[3];
	public int alpha;
	
	private final boolean supportsAlpha;
	
	
	public ColorPickerDialog(Context context, int color, ColorPickerListener listener){
		this(context, color, false, listener);
	}
	
	public ColorPickerDialog(Context context, int color, boolean supportsAlpha, ColorPickerListener listener){
		this.supportsAlpha 	= supportsAlpha;
		this.listener 		= listener;
	
		if(!supportsAlpha)
			color = color | 0xff000000; 
		
		Color.colorToHSV(color, currentColorHSV);
		alpha = Color.alpha(color);
		
		final View v 		= LayoutInflater.from(context).inflate(R.layout.dialog_color_picker, null);
		main 		= (ColorSquare) v.findViewById(R.id.color_picker_main);
		hue 		= (ImageView) v.findViewById(R.id.color_picker_hue);
		cursor 		= (ImageView) v.findViewById(R.id.color_picker_cursor);
		target 		= (ImageView) v.findViewById(R.id.color_picker_target);
		oldColor 	= v.findViewById(R.id.color_picker_old_color);
		newColor 	= v.findViewById(R.id.color_picker_new_color);
		container	= (ViewGroup) v.findViewById(R.id.color_picker_container);
		
		alphaOverlay 	= v.findViewById(R.id.color_picker_overlay);
		alphaCursor 	= (ImageView) v.findViewById(R.id.color_picker_alpha_cursor);
		alphaCheckered 	= (ImageView) v.findViewById(R.id.color_picker_alpha_checkered);
		
		alphaOverlay.setVisibility(supportsAlpha ? View.VISIBLE : View.GONE);
		alphaCursor.setVisibility(supportsAlpha ? View.VISIBLE : View.GONE);
		alphaCheckered.setVisibility(supportsAlpha ? View.VISIBLE : View.GONE);
		
		
		main.setHue(getHue());
		oldColor.setBackgroundColor(color);
		newColor.setBackgroundColor(color);
		
		hue.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_UP:
					
					float y = event.getY();
					
					if(y < 0.0f)
						y = 0.0f;
					
					if(y > hue.getMeasuredHeight())
						y = hue.getMeasuredHeight() - 0.001f;
					
					float hueVal =  360.0f - 360.0f / hue.getMeasuredHeight() * y;
					if(hueVal == 360.0f)
						hueVal = 0.0f;
					setHue(hueVal);
					
					main.setHue(getHue());
					moveCursor();
					newColor.setBackgroundColor(getColor());
					updateAlphaView();
					return true;
				
				}
				return false;
			}
		});
		
		if(supportsAlpha){
			alphaCheckered.setOnTouchListener(new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch(event.getAction()){
					case MotionEvent.ACTION_MOVE:
					case MotionEvent.ACTION_DOWN:
					case MotionEvent.ACTION_UP:
						
						float y = event.getY();
						if(y<0.0f)
							y=0.0f;
						
						if(y > alphaCheckered.getMeasuredHeight())
							y = alphaCheckered.getMeasuredHeight()-0.001f;
						
						final int alpha = Math.round(255.0f - ((255.0f / alphaCheckered.getMeasuredHeight()) * y));
						ColorPickerDialog.this.setAlpha(alpha);
						
						moveAlphaCursor();
						int col = ColorPickerDialog.this.getColor();
						int c = alpha << 24 | col & 0x00ffffff;
						newColor.setBackgroundColor(c);
						return true;					
					}
					return false;
				}
			});
		}
		
		main.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()){
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_UP:
				
					float x = event.getX();
					float y = event.getY();
					
					if(x < 0.0f) x = 0.0f;
					if(x > main.getMeasuredWidth()) x = main.getMeasuredWidth();
					if(y < 0.0f) y = 0.0f;
					if(y > main.getMeasuredHeight()) y = main.getMeasuredHeight();
					
					setSat(1.0f / main.getMeasuredWidth() * x);
					setVal(1.0f - (1.0f / main.getMeasuredHeight() * y));
					
					moveTarget();
					newColor.setBackgroundColor(getColor());
					return true;
				}
				
				return false;
			}
		});
		
		dialog = new AlertDialog.Builder(context)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(ColorPickerDialog.this.listener != null)
					ColorPickerDialog.this.listener.onOk(ColorPickerDialog.this, getColor());
			}
		})
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(ColorPickerDialog.this.listener != null)
					ColorPickerDialog.this.listener.onCancel(ColorPickerDialog.this);
			}
		})
		.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if(ColorPickerDialog.this.listener != null)
					ColorPickerDialog.this.listener.onCancel(ColorPickerDialog.this);
			}
		})
		.create();
		
		dialog.setView(v);
		
		ViewTreeObserver vto = v.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				moveCursor();
				if(ColorPickerDialog.this.supportsAlpha) moveAlphaCursor();
				moveTarget();
				if(ColorPickerDialog.this.supportsAlpha) updateAlphaView();
				v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});
	}
	
	protected void moveCursor(){
		float y = hue.getMeasuredHeight() - (getHue() * hue.getMeasuredHeight() / 360.0f);
		if(y == hue.getMeasuredHeight()) y = 0.0f;
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) cursor.getLayoutParams();
		params.leftMargin 	= (int) (hue.getLeft() - Math.floor(cursor.getMeasuredWidth() * 0.5) - container.getPaddingLeft());
		params.topMargin 	= (int) (hue.getTop() + y - Math.floor(cursor.getMeasuredHeight() * 0.5) - container.getPaddingTop());
		cursor.setLayoutParams(params);
	}

	protected void moveAlphaCursor(){
		float y = alphaCheckered.getMeasuredHeight() -((this.getAlpha() * alphaCheckered.getMeasuredHeight()) / 255.0f);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) alphaCursor.getLayoutParams();
		params.leftMargin	= (int) (alphaCheckered.getLeft() - Math.floor(alphaCursor.getMeasuredWidth() * 0.5) - container.getPaddingLeft());
		params.topMargin 	= (int) (alphaCheckered.getTop() + y - Math.floor(alphaCursor.getMeasuredHeight() * 0.5) - container.getPaddingTop());
		alphaCursor.setLayoutParams(params);
	}
	
	protected void moveTarget(){
		float x = getSat() * main.getMeasuredWidth();
		float y = (1.0f - getVal()) * main.getMeasuredHeight();
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) target.getLayoutParams();
		params.leftMargin 	= (int) (main.getLeft() + x - Math.floor(target.getMeasuredWidth() * 0.5) - container.getPaddingLeft());
		params.topMargin	= (int) (main.getTop() + y - Math.floor(target.getMeasuredHeight() * 0.5) - container.getPaddingTop());
		target.setLayoutParams(params);
	}
	
	private int getColor(){
		final int argb = Color.HSVToColor(currentColorHSV);
		return alpha << 24 | (argb & 0x00ffffff);
	}
	
	private float getAlpha(){
		return this.alpha;
	}
	
	private float getHue() {
		return currentColorHSV[0];
	}
	
	private float getSat(){
		return currentColorHSV[1];
	}
	
	private float getVal(){
		return currentColorHSV[2];
	}
	
	private void setAlpha(int alpha){
		this.alpha = alpha;
	}
	
	private void setHue(float hue){
		this.currentColorHSV[0] = hue;
	}
	
	private void setSat(float sat){
		this.currentColorHSV[1] = sat;
	}
	
	private void setVal(float val){
		this.currentColorHSV[2] = val;
	}

	public void show(){
		dialog.show();
	}
	
	public AlertDialog getDialog(){
		return dialog;
	}
	
	private void updateAlphaView(){
		GradientDrawable gd = new GradientDrawable(
				GradientDrawable.Orientation.TOP_BOTTOM, 
				new int[] {
						Color.HSVToColor(currentColorHSV), 0x0
						}
				);
		alphaOverlay.setBackground(gd);
	}
}

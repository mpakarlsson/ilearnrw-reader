package com.example.reader.types;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.PaintDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SectionIndexer;

public class SideSelector extends View {
	public static final int BOTTOM_PADDING = 10;
	
	private SectionIndexer selectionIndexer = null;
	private ListView list;
	private Paint paint;
	private PaintDrawable drawable;
	private String[] sections;
	private Rect fontRectangle;
	
	public SideSelector(Context context){
		super(context);
		init();
	}
	
	public SideSelector(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}
	
	public SideSelector(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		init();	
	}
	
	public void init(){
		drawable = new PaintDrawable();
		paint = drawable.getPaint();
		paint.setTextSize(30);
		paint.setTextAlign(Paint.Align.CENTER);
		drawable.setCornerRadius(15);
		setBackground(drawable);
		
		fontRectangle = new Rect();
	}
	
	public void setListView(ListView list){
		this.list = list;
		selectionIndexer = (SectionIndexer) list.getAdapter();

		Object[] sectionsArr = selectionIndexer.getSections();
		sections = new String[sectionsArr.length];
		for(int i=0; i<sectionsArr.length; i++){
			sections[i] = sectionsArr[i].toString();
		}
		
		this.invalidate();
	}
	
	
	public boolean onTouchEvent(MotionEvent event){
		super.onTouchEvent(event);
		int y = (int) event.getY();
		float selectedIndex = ((float) y / (float) getPaddedHeight()) * selectionIndexer.getSections().length;
		
		if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() ==  MotionEvent.ACTION_MOVE){
			if(selectionIndexer == null)
				selectionIndexer = (SectionIndexer) list.getAdapter(); 
		
			int position = selectionIndexer.getPositionForSection((int) selectedIndex);
			if(position == -1)
				return true;
			
			list.setSelection(position);
		}
		return true;
	}
	
	protected void onDraw(Canvas canvas){
		int viewHeight = getPaddedHeight();
		float charHeight = ((float) viewHeight / (float) sections.length);
		float widthCenter = getMeasuredWidth() * 0.5f;

		paint.setColor(0xFF959899);
		for(int i=0; i<sections.length; i++){
			paint.getTextBounds(sections[i], 0, sections[i].length(), fontRectangle);
			float height = charHeight + (i*charHeight) - charHeight + fontRectangle.height();
			canvas.drawText(String.valueOf(sections[i]), widthCenter, height, paint);
		//	canvas.drawText(String.valueOf(sections[i]), widthCenter, charHeight + (i*charHeight), paint);
		}
		paint.setColor(0xAAFFFFFF);
		
		super.onDraw(canvas);
	}
	
	private int getPaddedHeight(){
		return getHeight() - BOTTOM_PADDING;
	}
}

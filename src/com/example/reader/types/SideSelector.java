package com.example.reader.types;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.PaintDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.SectionIndexer;

public class SideSelector extends View {
	public static final int BOTTOM_PADDING = 10;
	
	private SectionIndexer sectionIndexer = null;
	private GridView list;
	private Paint paint;
	private PaintDrawable drawable;
	private String[] sections;
	
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
	}
	
	public void setListView(GridView list){
		this.list = list;
		sectionIndexer = (SectionIndexer) list.getAdapter();

		Object[] sectionsArr = sectionIndexer.getSections();
		sections = new String[sectionsArr.length];
		for(int i=0; i<sectionsArr.length; i++){
			sections[i] = sectionsArr[i].toString();
		}
		this.invalidate();
	}
	
	public boolean onTouchEvent(MotionEvent event){
		super.onTouchEvent(event);
		int y = (int) event.getY();
		float selectedIndex = ((float) y / (float) getPaddedHeight()) * sectionIndexer.getSections().length;
		
		if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() ==  MotionEvent.ACTION_MOVE){
			if(sectionIndexer == null)
				sectionIndexer = (SectionIndexer) list.getAdapter(); 
		
			int position = sectionIndexer.getPositionForSection((int) selectedIndex);
			if(position == -1)
				return true;
			
			list.setSelection(position);
		}
		return true;
	}
	
	protected void onDraw(Canvas canvas){
		int viewHeight = getPaddedHeight();
		float charHeight = 1;
		if (sections == null)
			sections = new String[0];
		if (sections.length >0)
			charHeight = ((float) viewHeight / (float) (sections.length+1));
		float widthCenter = getMeasuredWidth() * 0.5f;

		paint.setColor(0xFF959899);
		for(int i=0; i<sections.length; i++){
			canvas.drawText(String.valueOf(sections[i]), widthCenter, charHeight + ((i*charHeight)), paint);
		}
		paint.setColor(0xAAFFFFFF);
		
		super.onDraw(canvas);
	}
	
	private int getPaddedHeight(){
		return getHeight() - BOTTOM_PADDING;
	}
	
	public int getNumSections(){
		return sections.length;
	}
}

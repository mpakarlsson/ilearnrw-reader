package com.example.reader.types;

import com.example.reader.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class ExpandableLayout extends LinearLayout{

	private final int handleId;
	private final int contentId;
	
	private View handle;
	private View content;
	
	private boolean expanded		= false;
	private int collapsedHeight		= 0;
	private int contentHeight 		= 0;
	private int animationDuration	= 500;
	
	private OnExpandListener listener;
	
	public ExpandableLayout(Context context) {
		super(context, null);
		handleId 	= 0;
		contentId 	= 0;
	}
	
	public ExpandableLayout(Context context, AttributeSet attrs){
		super(context, attrs);
		listener = new DefaultOnExpandListener();
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableLayout, 0, 0);
		
		collapsedHeight 	= (int) a.getDimension(R.styleable.ExpandableLayout_collapsedHeight, 0.0f);
		animationDuration 	= (int) a.getInteger(R.styleable.ExpandableLayout_animationDuration, 500);
		
		int hId 			= a.getResourceId(R.styleable.ExpandableLayout_handle, 0);
		if(hId == 0)
			throw new IllegalArgumentException("The handle attribute is required and must refer to a valid child.");

		int cId 			= a.getResourceId(R.styleable.ExpandableLayout_content, 0);
		if(cId == 0)
			throw new IllegalArgumentException("The content attribute is required and must refer to a valid child.");
		
		handleId 	= hId;
		contentId 	= cId;
		
		a.recycle();
	}
	
	public void setOnExpandListener(OnExpandListener listener){
		this.listener = listener;
	}
	
	public void setCollapsedHeight(int collapsedHeight){
		this.collapsedHeight = collapsedHeight;
	}
	
	public void setAnimationDuration(int animationDuration){
		this.animationDuration = animationDuration;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		handle = findViewById(handleId);
		if(handle == null)
			throw new IllegalArgumentException("The handle attribute is required and must refer to an existing child.");

		content	= findViewById(contentId);
		if(content == null)
			throw new IllegalArgumentException("The content attribute is required and must refer to an existing child.");
		
		android.view.ViewGroup.LayoutParams lp = content.getLayoutParams();
		lp.height = collapsedHeight;
		content.setLayoutParams(lp);
		handle.setOnClickListener(new LayoutToggler());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		content.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
		contentHeight = content.getMeasuredHeight();
		
		if(contentHeight < collapsedHeight){
			handle.setVisibility(View.GONE);
			Log.d("onMeasure", "Gone");
		} else {
			handle.setVisibility(View.VISIBLE);
			
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	private class LayoutToggler implements OnClickListener{
		@Override
		public void onClick(View v) {
			Animation a;
			if(expanded){
				a = new ExpandAnimation(contentHeight, collapsedHeight);
				listener.onCollapse(handle, content);
				Log.d("LayoutToggler", "start " + contentHeight + " end " + collapsedHeight);
				Log.d("LayoutToggler", "Collapse");
			} else {
				a = new ExpandAnimation(collapsedHeight, contentHeight);
				listener.onExpand(handle, content);
				Log.d("LayoutToggler", "start " + collapsedHeight + " end " + contentHeight);
				Log.d("LayoutToggler", "Expand");
			}
			a.setDuration(animationDuration);
			content.startAnimation(a);
			expanded = !expanded;
		}
	}
	
	private class ExpandAnimation extends Animation {
		private final int startHeight;
		private final int deltaHeight;
		
		public ExpandAnimation(int startHeight, int endHeight){
			this.startHeight = startHeight;
			this.deltaHeight = endHeight - startHeight;
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
		
			android.view.ViewGroup.LayoutParams lp = content.getLayoutParams();
			lp.height = (int) (startHeight + deltaHeight * interpolatedTime);
			content.setLayoutParams(lp);
		}

		@Override
		public boolean willChangeBounds() {
			return true;
		}
	}
	
	public interface OnExpandListener {
		public void onExpand(View handle, View content);
		public void onCollapse(View handle, View content);
	}
	
	private class DefaultOnExpandListener implements OnExpandListener { 
		@Override
		public void onExpand(View handle, View content) {}

		@Override
		public void onCollapse(View handle, View content) {}
	}
}

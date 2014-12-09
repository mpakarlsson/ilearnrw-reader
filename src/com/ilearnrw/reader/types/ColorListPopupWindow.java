package com.ilearnrw.reader.types;

import android.content.Context;
import android.widget.ListPopupWindow;

public class ColorListPopupWindow extends ListPopupWindow{
	private int parentListPosition;
	public ColorListPopupWindow(Context context) {
		super(context);
		this.parentListPosition = -1;
	}
	public int getParentListPosition() {
		return parentListPosition;
	}
	public void setParentListPosition(int parentListPosition) {
		this.parentListPosition = parentListPosition;
	}
}

package com.ilearnrw.reader.types;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
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

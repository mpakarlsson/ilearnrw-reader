package com.ilearnrw.reader.interfaces;

import com.ilearnrw.reader.types.ColorPickerDialog;

public interface ColorPickerListener {
	void onCancel(ColorPickerDialog dialog);
	void onOk(ColorPickerDialog dialog, int color);
}

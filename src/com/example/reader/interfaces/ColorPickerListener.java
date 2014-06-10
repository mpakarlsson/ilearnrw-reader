package com.example.reader.interfaces;

import com.example.reader.types.ColorPickerDialog;

public interface ColorPickerListener {
	void onCancel(ColorPickerDialog dialog);
	void onOk(ColorPickerDialog dialog, int color);
}

package com.ilearnrw.reader.interfaces;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
import com.ilearnrw.reader.types.ColorPickerDialog;

public interface ColorPickerListener {
	void onCancel(ColorPickerDialog dialog);
	void onOk(ColorPickerDialog dialog, int color);
}

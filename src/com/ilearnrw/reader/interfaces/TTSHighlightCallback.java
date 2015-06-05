package com.ilearnrw.reader.interfaces;
/*
 * Copyright (c) 2015, iLearnRW. Licensed under Modified BSD Licence. See licence.txt for details.
 */
public interface TTSHighlightCallback {
	public void OnHighlight(int id);
	public void OnRemoveHighlight(int id, boolean continueReading);
}

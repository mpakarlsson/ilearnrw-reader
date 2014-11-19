package com.ilearnrw.reader.interfaces;

public interface TTSHighlightCallback {
	public void OnHighlight(int id);
	public void OnRemoveHighlight(int id, boolean continueReading);
}

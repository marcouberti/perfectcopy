package com.invenktion.perfectcopy.beans;

public class Artwork {
	private String originalImagePath,canvasImagePath,originalWithBorderImagePath;
	private String foregroundImagePath;//usato per l'anteprima nei lavori salvati

	public String getForegroundImagePath() {
		return foregroundImagePath;
	}

	public void setForegroundImagePath(String foregroundImagePath) {
		this.foregroundImagePath = foregroundImagePath;
	}

	public String getOriginalWithBorderImagePath() {
		return originalWithBorderImagePath;
	}

	public void setOriginalWithBorderImagePath(String originalWithBorderImagePath) {
		this.originalWithBorderImagePath = originalWithBorderImagePath;
	}

	public String getOriginalImagePath() {
		return originalImagePath;
	}

	public void setOriginalImagePath(String originalImagePath) {
		this.originalImagePath = originalImagePath;
	}

	public String getCanvasImagePath() {
		return canvasImagePath;
	}

	public void setCanvasImagePath(String canvasImagePath) {
		this.canvasImagePath = canvasImagePath;
	}
}

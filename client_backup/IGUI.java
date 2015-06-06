package com.spots.varramie;

public interface IGUI {

	public abstract void print(String str);
	public abstract void println(String str);
	public abstract String getInput();
	public abstract void receiveTouch(int x, int y);
}

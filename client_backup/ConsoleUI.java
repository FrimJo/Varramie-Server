package com.spots.varramie;

import java.sql.Time;
import java.util.Scanner;

public class ConsoleUI implements IGUI {
	
	private final Scanner	systemIn = new Scanner(System.in);
	private final Time time = new Time(System.currentTimeMillis());

	@Override
	public void print(String str) {
		System.out.print(str);

	}

	@Override
	public void println(String str) {
		this.time.setTime(System.currentTimeMillis());
		System.out.println("(" + this.time.toString()+") " + str);
	}
	
	/**
	 * Gets the typed text from the user .
	 * @return The string of text typed by the user.
	 */
	@Override
	public String getInput() {
		return "";
		//return this.systemIn.nextLine();
	}

	@Override
	public void receiveTouch(int x, int y) {
		println("Received touch at x: "+x+" ,y: "+y);
		
	}
}

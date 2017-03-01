package main;

import java.awt.EventQueue;

public class Main {

	public static void main(String[] args) {
		//System.out.println(System.getProperty("java.library.path"));
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new MainFrame();
			}
		});
	}
}

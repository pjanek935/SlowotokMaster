package main;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.ArrayList;

import main.SearchEngine.Word;

public class AutoControl {
	
	public static Point[][] points = new Point[4][4];
	private static int delta = 120; //space between tiles
	
	public static void setStartPoint(Point startPoint){
		for(int x=0; x<4; x++){
			for(int y=0; y<4; y++){
				points[x][y] = new Point(startPoint.x + x*delta, startPoint.y + y*delta);
			}
		}
	}
	
	public static void moveToBeg(){
		class Mover implements Runnable{
			@Override
			public void run() {
				try {
					Robot robot = new Robot();
					robot.mouseMove(points[0][0].x, points[0][0].y);
				} catch (AWTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		Thread moverThread = new Thread(new Mover());
		moverThread.start();
	}
	
	public static void move(ArrayList<Word> words){
		class Mover implements Runnable{
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					Robot robot = new Robot();
					for(Word word: words){
						Point startPoint = points[word.points.get(0).y][word.points.get(0).x];
						robot.mouseMove(startPoint.x, startPoint.y);
						robot.mousePress(InputEvent.BUTTON1_MASK);
						for(int i=1; i<word.points.size(); i++){
							Thread.sleep(20);
							Point nextPoint = points[word.points.get(i).y][word.points.get(i).x];
							robot.mouseMove(nextPoint.x, nextPoint.y);
						}
						robot.mouseRelease(InputEvent.BUTTON1_MASK);
					}
				} catch (AWTException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		Thread moverThread = new Thread(new Mover());
		moverThread.start();
	}

}

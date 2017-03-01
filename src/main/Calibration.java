package main;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JTextField;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class Calibration {

	private static String ssName = "screen.bmp";
	private static int morphSize = 9;
	private static int x0, y0;
	private static Mat imgOrg;
	private static Mat imgProcessed;
	private static int tileDelta = 120;
	private static final int croppedImgSize = 18;
	private static final int tileMinSize = 5000;
	
	public static void takeScreenshot(String name){
		MainFrame.paintGreen();
		try {
			Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			BufferedImage capture = new Robot().createScreenCapture(screenRect);
			ImageIO.write(capture, "bmp", new File(name));
		} catch (IOException | AWTException e) {
			MainFrame.printMessage("B³¹d podczas wykonywania zrzutu ekranu.");
			e.printStackTrace();
		}
		MainFrame.paintDefault();
	}
	
	public static Point calibrate(){
		MainFrame.printMessage("Kalibrujê...");
		
		//Load libs
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		//Take screenshot
		takeScreenshot(ssName);
		
		//Load screenshot
		imgOrg = Highgui.imread(ssName, -1);
		imgProcessed = imgOrg.clone();
		
		//Process screenshot
		Imgproc.cvtColor(imgProcessed, imgProcessed, Imgproc.COLOR_BGR2HSV);
		Core.inRange(imgProcessed, new Scalar(80, 100, 100), new Scalar(100, 180, 200), imgProcessed);
		Imgproc.dilate(imgProcessed, imgProcessed, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(morphSize, morphSize)));
		Imgproc.erode(imgProcessed, imgProcessed, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(morphSize, morphSize)));
		
		Highgui.imwrite("processed.bmp", imgProcessed);
		
		//Find contours and orgin point of board
		Mat imgCont = imgProcessed.clone();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(imgCont, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
		int x = 2000;
		int y = 2000;
		for(int i=0; i< contours.size();i++){
			if (Imgproc.contourArea(contours.get(i)) < tileMinSize ) continue;
			System.out.println("SZIE: " + Imgproc.contourArea(contours.get(i)));
			 Moments m = Imgproc.moments(contours.get(i), false);
			 int mx = (int) (m.get_m10() / m.get_m00());
		     int my = (int) (m.get_m01() / m.get_m00());
		     System.out.println("mx: " + mx + " my: " + my);
		     if(mx <= x || my <= y){
		    	 if(i > contours.size()/2){
		    		 System.out.println("Delta: " + (mx-x));
		    		 tileDelta = Math.abs(mx-x);
		    	 }
		    	 x = mx;
		    	 y = my;
		    	 
		     }
		}
		x0 = x;
		y0 = y;
		
		System.out.println("Skalibrowane punkty: " + "x: " + x + " y: " + y);
		MainFrame.printMessage("Zakoñczono kalibracjê.");
		return new Point(x, y);
	}
	
	public static void searchBoard(){
		char[][] tmpBoard = new char[4][4];
		Point[][] points = new Point[4][4];
		calibrate();
		
		//Cropping letters from image
		for(int i=0; i<4; i++){
			for(int j=0; j<4; j++){
				points[i][j] = new Point(x0 + i*tileDelta, y0 + j*tileDelta);
				Point pt1 = new Point(points[i][j].x - 22, points[i][j].y - 28);
				Point pt2 = new Point(points[i][j].x + 24, points[i][j].y + 24);
				Rect rect = new Rect(pt1, pt2);
				Mat cropped = new Mat(imgOrg, rect);
				Imgproc.cvtColor(cropped, cropped, Imgproc.COLOR_RGB2HSV);
				Size s = new Size(18, 18);
				Imgproc.resize(cropped, cropped, s);
				Scalar low = new Scalar(0, 0, 240);
				Scalar up = new Scalar(179, 255, 255);
				Core.inRange(cropped, low, up, cropped);
				Highgui.imwrite("cropped02/"+ i + "_" + j + ".png", cropped);
			}
		}
		
		//Searching matching characters
		for(int i=0; i<4; i++){
			for(int j=0; j<4; j++){
				Mat letterImg = new Mat();
				letterImg = Highgui.imread("cropped02/"+ j + "_" + i + ".png");
				Float[] tv = new Float [croppedImgSize*croppedImgSize];
				for(int k=0; k<croppedImgSize; k++){
					for(int m=0; m<croppedImgSize; m++){
						tv[k*croppedImgSize + m] = (float) (letterImg.get(k, m)[0]/255.0);
					}
				}
				
				//Load character images data
				File file = new File("data.txt");
			    Scanner in;
			    
			    float val = 0;
			    int size = 0;
			    
			    try {
					in = new Scanner(file);
					while(in.hasNextLine()){
						String[] line = in.nextLine().split(" ");
						Float[] vec = new Float[croppedImgSize*croppedImgSize];
						for(int k=0; k<croppedImgSize*croppedImgSize; k++){
							vec[k] = Float.parseFloat(line[k]);
						}
						
						int tmpSize = 0;
						for(int k=0; k<croppedImgSize*croppedImgSize; k++){
							float t = vec[k] * tv[k];
							if(t!=0){
								tmpSize++;
							}
						}
						if(tmpSize >= size){
							size = tmpSize;
							val = Float.parseFloat(line[line.length-1]);
						}
					}
				} catch (FileNotFoundException e) {
					MainFrame.printMessage("Nie mo¿na odnaleŸæ pliku data.txt");
					e.printStackTrace();
				}
				System.out.print((char)val + " ");
				tmpBoard[i][j] = (char)val;
				
			}
			System.out.println();
		}
		for(int i=0; i<4; i++){
			for(int j=0; j<4; j++){
				JTextField f = (JTextField)MainFrame.boardPanel.getComponent(i*4 + j);
				f.setText(String.valueOf(tmpBoard[i][j]));
			}
		}
		System.out.println();
	}
	
	
	//Not used in actual programme
	@Deprecated
	public static void crop(){
		String imgName = "img.bmp";
		int morphSize = 9;
		
		Point[][] points = new Point[4][4];
		
		try {
			Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			BufferedImage capture = new Robot().createScreenCapture(screenRect);
			ImageIO.write(capture, "bmp", new File(imgName));
		} catch (IOException | AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Load libs
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		Mat img = Highgui.imread(imgName, -1);
		Mat orgImg = img.clone();
		Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2HSV);
		Core.inRange(img, new Scalar(80, 100, 100), new Scalar(100, 180, 200), img);
		Imgproc.dilate(img, img, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(morphSize, morphSize)));
		Imgproc.erode(img, img, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(morphSize, morphSize)));
		
		Mat thresh = img.clone();
		Highgui.imwrite("nowy.png", img);
		
		Mat imgCont = img.clone();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(imgCont, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
		
		int x = 2000;
		int y = 2000;
		for(int i=0; i< contours.size();i++){
			if (Imgproc.contourArea(contours.get(i)) < 150 ) continue;
			 Moments m = Imgproc.moments(contours.get(i), false);
			 int mx = (int) (m.get_m10() / m.get_m00());
		     int my = (int) (m.get_m01() / m.get_m00());
		     //System.out.println(Imgproc.contourArea(contours.get(i)));
		     if(mx <= x && my <= y){
		    	 x = mx;
		    	 y = my;
		     }
		}
		System.out.println("x: " + x + " y: " + y);
		
		File file = new File("cropped/");
		String names[] = file.list();
		int index = names.length + 1;
		
		
		
		for(int i=0; i<4; i++){
			for(int j=0; j<4; j++){
				points[i][j] = new Point(x + i*120, y + j*120);
				Point pt1 = new Point(points[i][j].x - 22, points[i][j].y - 28);
				Point pt2 = new Point(points[i][j].x + 24, points[i][j].y + 24);
				Rect rect = new Rect(pt1, pt2);
				Mat cropped = new Mat(orgImg, rect);
				Imgproc.cvtColor(cropped, cropped, Imgproc.COLOR_RGB2HSV);
				Size s = new Size(18, 18);
				Imgproc.resize(cropped, cropped, s);
				Scalar low = new Scalar(0, 0, 240);
				Scalar up = new Scalar(179, 255, 255);
				Core.inRange(cropped, low, up, cropped);
				
				
				Highgui.imwrite("cropped/" + (int)SearchEngine.board[j][i] + "_" + index + ".png", cropped);
				Core.rectangle(orgImg, pt1, pt2, new Scalar(255, 20, 158));
				index++;
			}
		}
		
		//Highgui.imwrite("rects.png", orgImg);
	}
	
}

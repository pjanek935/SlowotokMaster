package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Dictionary {
	
	private final static String dicName = "slowa.txt";
	private ArrayList<String> dic = new ArrayList<String>();
	private ArrayList<LetterOrder> letterOrder = new ArrayList<LetterOrder>(); //list of pointers where each character index starts and ends in dictionary
	private boolean loadingRunning = false;
	private boolean dicLoaded = false;
	
	public ArrayList<String>[] byLength = new ArrayList[14];
	public ArrayList<LetterOrder>[] byLengthOrder = new ArrayList[14];
	
	public ArrayList<String> getDic(){
		return dic;
	}
	public ArrayList<LetterOrder> getLetterOrder(){
		return letterOrder;
	}
	public boolean loadingRunning(){
		return loadingRunning;
	}
	
	public void loadDic(){
		class Loader implements Runnable{
			@Override
			public void run() {
				
				////
				for(int i=0; i<13; i++){
					byLength[i] = new ArrayList<String>();
					byLengthOrder[i] = new ArrayList<LetterOrder>();
				}
				////
				
				loadingRunning = true;
				File dicFile = new File(dicName);
				try {
					Scanner dicScanner = new Scanner(dicFile);
					dic.clear();
					while(dicScanner.hasNextLine()){
						String word = dicScanner.nextLine().toLowerCase();
						dic.add(word);
						
						/////
						if(word.length() >= 3 && word.length() < 16){
							//System.out.println(word.length());
							if(word.contains("x") || word.contains("q"))continue;
							byLength[word.length()-3].add(word);
						}
						////
						
					}
					System.out.println(dic.size() + " s³ow w s³owniku.");
					
					//////
					for(int i=0; i<13; i++){
						byLengthOrder[i].clear();
						char c = 'a';
						int lower = 0;
						for(int upper=0; upper<byLength[i].size(); upper++){
							if(c != byLength[i].get(upper).charAt(0)){
								byLengthOrder[i].add(new LetterOrder(c, lower, upper));
								System.out.println(c + ": " + lower + "-" + upper);
								lower = upper;
								c = byLength[i].get(upper).charAt(0);
							}
						}
					}
					////
					
					//Divide dictionary by letters
					letterOrder.clear();
					char c = 'a';
					int lower = 0;
					for(int upper=0; upper<dic.size(); upper++){
						if(c != dic.get(upper).charAt(0)){
							letterOrder.add(new LetterOrder(c, lower, upper));
							System.out.println(c + ": " + lower + "-" + upper);
							lower = upper;
							c = dic.get(upper).charAt(0);
						}
					}
					System.out.println(dic.get(10) + " " + dic.get(10).length());
					dicLoaded = true;
					dicScanner.close();
					loadingRunning = false;
				} catch (FileNotFoundException e) {
					dicLoaded = false;
					e.printStackTrace();
				}
			}
		}
		System.out.println("£adujê s³ownik.");
		Thread dicLoaderThread = new Thread(new Loader());
		dicLoaderThread.start();
	}
	
	public boolean dicLoaded(){
		return dicLoaded;
	}
	
	class LetterOrder{
		public char c;
		public int lower, upper;
		public LetterOrder(char c, int lower, int upper){
			this.c = c;
			this.lower = lower;
			this.upper = upper;
		}
	}
}

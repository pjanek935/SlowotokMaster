package main;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JTextField;

import main.Dictionary.LetterOrder;

public class SearchEngine {
	public static char board[][] = new char[4][4];
	public ArrayList<Word> foundWords = new ArrayList<Word>();
	public static Dictionary dictionary = new Dictionary();
	boolean calibrationRunning = false;
	boolean searchingRunning = false;
	Thread searchThread;
	
	public void loadDic(){
		dictionary.loadDic();
	}
	
	public void setBoard(){
		try {
			board = getBoard();
		} catch (fieldNotFilledException e) {
			return;
		}
	}
	
	public void search(){
		if(!dictionary.dicLoaded()){
				MainFrame.printMessage("Brak pliku s³ownika!");
				dictionary.loadDic();
				return;
			
		}
		ArrayList<Word> words = new ArrayList<Word>();
		class Search implements Runnable{
			@Override
			public void run() {
				long t1 = System.currentTimeMillis();
				
				searchingRunning = true;
				MainFrame.searchButton.setText("STOP");
				
				//Getting board from text fields
				try {
					board = getBoard();
				} catch (fieldNotFilledException e) {
					return;
				}
				
				//Printing board
				printBoard();
				
				//Search in all positions
				MainFrame.printMessage("Wyszukujê mo¿liwe kombinacje...");
				for(int i=0; i<4; i++){
					for(int j=0; j<4; j++){
						words.addAll(searchInPosition(i, j));
					}
				}
				
				if(!calibrationRunning)MainFrame.printMessage("Znaleziono " + words.size() + " kombinacji.");
				
				foundWords.clear();
				MainFrame.listModel.clear();
				int counter=0;
				for(Word word: words){
					counter++;
					if(!calibrationRunning)MainFrame.printMessage("Ukoñczono: " + ((float)counter/words.size())*100 + "%");
					int lower = 0;
					int upper = 0;
					
					int length = word.word.length();
					for(LetterOrder order: dictionary.byLengthOrder[length-3]){
						if(order.c == word.word.charAt(0)){
							lower = order.lower;
							upper = order.upper;
							break;
						}
					}
					
					for(int i=lower; i<upper; i++){
						if(word.word.equals(dictionary.byLength[length-3].get(i))){
							if(!wordRepeats(foundWords, word)){
								foundWords.add(word);
								MainFrame.listModel.addElement(word.word);
							}
							break;
						}
					}
					
					/*for(LetterOrder order: dictionary.getLetterOrder()){
						if(order.c == word.word.charAt(0)){
							lower = order.lower;
							upper = order.upper;
							break;
						}
					}
					for(int i=lower; i<upper; i++){
						if(word.word.equals(dictionary.getDic().get(i))){
							if(!wordRepeats(foundWords, word)){
								foundWords.add(word);
								MainFrame.listModel.addElement(word.word);
							}
							break;
						}
					}*/
				}
				searchingRunning = false;
				MainFrame.searchButton.setText("SZUKAJ");
				
				long t2 = System.currentTimeMillis();
				long delta = t2-t1;
				System.out.println(delta/1000);
			}
		}
		
		//Creating new thread
		searchThread = new Thread(new Search());
		searchThread.start();
	}
	
	public void test(){
		ArrayList<Point> p = new ArrayList<Point>();
		for(int i=0; i<6; i++){
			if(i>=4){
				p.add(new Point(4-i, 4-i));
			}else{
				p.add(new Point(i, i));
			}
		}
		Word word = new Word("kakajacv", p);
		int length = word.word.length();
		int lower = 0;
		int upper = 0;
		for(LetterOrder order: dictionary.getLetterOrder()){
			if(order.c == word.word.charAt(0)){
				lower = order.lower;
				upper = order.upper-1;
				break;
			}
		}
		System.out.println("Lower: " + lower + " upper: " + upper);
		
		boolean found = false;
		int cn = 1;
		int k = 0;
		int m =  (lower + upper)/2;
		while(lower <= upper){
			m = (lower + upper)/2;
			System.out.println("M: " + m);
			String w1 = word.word;
			String w2 = dictionary.getDic().get(m);
			if(upper-lower <= 3){
				boolean f = false;
				for(int rest=lower; rest<upper+1; rest++){
					System.out.println("rest: " + dictionary.getDic().get(rest));
					if(w1.equals(dictionary.getDic().get(rest))){
						found = true;
						break;
					}
				}
				if(!f)break;
			}
			
			if(w1.equals(w2)){
				found = true;
				break;
			}else{
				Letter l1 = new Letter(w1.charAt(cn));
				Letter l2 = new Letter(w2.charAt(cn));
				if(l1.val > l2.val){
					lower = m-1;
				}else if(l1.val < l2.val){
					upper = m+1;
				}else{
					for(int i=cn+1; i<w1.length(); i++){
						Letter l11 = new Letter(w1.charAt(i));
						Letter l22 = new Letter(w2.charAt(i));
						if(l11.val > l22.val){
							lower = m-1;
							break;
						}else if(l11.val < l22.val){
							upper = m+1;
							break;
						}
					}
					
					//cn++;
				}
				
			}
			System.out.println(w2);
			System.out.println("L: " + lower + " U: " + upper);
			
		}
		System.out.println(found);
	}
	
	public ArrayList<Word> searchInPosition(int x, int y){
		ArrayList<Word> words = new ArrayList<Word>();
		for(int i=-1; i<2; i++){
			if(x+i < 0 || x+i > 3){
				continue;
			}
			for(int j=-1; j<2; j++){
				if(y+j < 0 || y+j > 3){
					continue;
				}
				
				int x2 = x+i;
				int y2 = y+j;
				for(int i2=-1; i2<2; i2++){
					if(x2+i2 < 0 || x2+i2 > 3){
						continue;
					}
					for(int j2=-1; j2<2; j2++){
						if(y2+j2 < 0 || y2+j2 > 3){
							continue;
						}
						
						String text = "" + board[x][y] + board[x2][y2] + board[x2+i2][y2+j2];
						ArrayList<Point> points = new ArrayList<Point>(Arrays.asList(new Point(x, y),
								new Point(x2, y2), new Point(x2+i2, y2+j2)));
						if(pointRepeats(points)) continue;
						Word word = new Word(text.toLowerCase(), points);
						words.add(word);
						
						if(MainFrame.lengthSelected<4)continue;
						
						int x3 = x2+i2;
						int y3 = y2+j2;
						for(int i3=-1; i3<2; i3++){
							if(x3+i3 < 0 || x3+i3 > 3){
								continue;
							}
							for(int j3=-1; j3<2; j3++){
								if(y3+j3 < 0 || y3+j3 > 3){
									continue;
								}
								
								text = "" + board[x][y] + board[x2][y2] + board[x3][y3] + board[x3+i3][y3+j3];
								points = new ArrayList<Point>(Arrays.asList(new Point(x, y),
										new Point(x2, y2), new Point(x3, y3), new Point(x3+i3, y3+j3)));
								if(pointRepeats(points)) continue;
								word = new Word(text.toLowerCase(), points);
								words.add(word);
								
								if(MainFrame.lengthSelected<5)continue;
								
								int x4 = x3+i3;
								int y4 = y3+j3;
								for(int i4=-1; i4<2; i4++){
									if(x4+i4 < 0 || x4+i4 > 3){
										continue;
									}
									for(int j4=-1; j4<2; j4++){
										if(y4+j4 < 0 || y4+j4 > 3){
											continue;
										}
										
										text = "" + board[x][y] + board[x2][y2] + board[x3][y3] + board[x4][y4] + 
												board[x4+i4][y4+j4];
										points = new ArrayList<Point>(Arrays.asList(new Point(x, y),
												new Point(x2, y2), new Point(x3, y3), new Point(x4, y4),
												new Point(x4+i4, y4+j4)));
										if(pointRepeats(points)) continue;
										word = new Word(text.toLowerCase(), points);
										words.add(word);
										
										if(MainFrame.lengthSelected<6)continue;
										
										int x5 = x4+i4;
										int y5 = y4+j4;
										for(int i5=-1; i5<2; i5++){
											if(x5+i5 < 0 || x5+i5 > 3){
												continue;
											}
											for(int j5=-1; j5<2; j5++){
												if(y5+j5 < 0 || y5+j5 > 3){
													continue;
												}
												
												text = "" + board[x][y] + board[x2][y2] + board[x3][y3] + board[x4][y4] + 
														board[x5][y5] + board[x5+i5][y5+j5];
												points = new ArrayList<Point>(Arrays.asList(new Point(x, y),
														new Point(x2, y2), new Point(x3, y3), new Point(x4, y4),
														new Point(x5, y5), new Point(x5+i5, y5+j5)));
												if(pointRepeats(points)) continue;
												word = new Word(text.toLowerCase(), points);
												words.add(word);
												
												if(MainFrame.lengthSelected<7)continue;
												
												int x6 = x5+i5;
												int y6 = y5+j5;
												for(int i6=-1; i6<2; i6++){
													if(x6+i6 < 0 || x6+i6 > 3){
														continue;
													}
													for(int j6=-1; j6<2; j6++){
														if(y6+j6 < 0 || y6+j6 > 3){
															continue;
														}
														
														text = "" + board[x][y] + board[x2][y2] + board[x3][y3] + board[x4][y4] + 
																board[x5][y5] + board[x6][y6] + board[x6+i6][y6+j6];
														points = new ArrayList<Point>(Arrays.asList(new Point(x, y),
																new Point(x2, y2), new Point(x3, y3), new Point(x4, y4),
																new Point(x5, y5), new Point(x6, y6), new Point(x6+i6, y6+j6)));
														if(pointRepeats(points)) continue;
														word = new Word(text.toLowerCase(), points);
														words.add(word);
														
														if(MainFrame.lengthSelected<8)continue;
														
														int x7 = x6+i6;
														int y7 = y6+j6;
														for(int i7=-1; i7<2; i7++){
															if(x7+i7 < 0 || x7+i7 > 3){
																continue;
															}
															for(int j7=-1; j7<2; j7++){
																if(y7+j7 < 0 || y7+j7 > 3){
																	continue;
																}
																
																text = "" + board[x][y] + board[x2][y2] + board[x3][y3] + board[x4][y4] + 
																		board[x5][y5] + board[x6][y6] + board[x7][y7] + board[x7+i7][y7+j7];
																points = new ArrayList<Point>(Arrays.asList(new Point(x, y),
																		new Point(x2, y2), new Point(x3, y3), new Point(x4, y4),
																		new Point(x5, y5), new Point(x6, y6), new Point(x7, y7),
																		new Point(x7+i7, y7+j7)));
																if(pointRepeats(points)) continue;
																word = new Word(text.toLowerCase(), points);
																words.add(word);
																
																if(MainFrame.lengthSelected<9)continue;
																
																int x8 = x7+i7;
																int y8 = y7+j7;
																for(int i8=-1; i8<2; i8++){
																	if(x8+i8 < 0 || x8+i8 > 3){
																		continue;
																	}
																	for(int j8=-1; j8<2; j8++){
																		if(y8+j8 < 0 || y8+j8 > 3){
																			continue;
																		}
																		
																		text = "" + board[x][y] + board[x2][y2] + board[x3][y3] + board[x4][y4] + 
																				board[x5][y5] + board[x6][y6] + board[x7][y7] + board[x8][y8] + 
																				board[x8+i8][y8+j8];
																		points = new ArrayList<Point>(Arrays.asList(new Point(x, y),
																				new Point(x2, y2), new Point(x3, y3), new Point(x4, y4),
																				new Point(x5, y5), new Point(x6, y6), new Point(x7, y7),
																				new Point(x8, y8), new Point(x8+i8, y8+j8)));
																		if(pointRepeats(points)) continue;
																		word = new Word(text.toLowerCase(), points);
																		words.add(word);
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return words;
	}
	
	public char[][] getBoard() throws fieldNotFilledException{
		MainFrame.listModel.clear();
		char[][] board = new char[4][4];
		for(int i=0; i<4; i++){
			for(int j=0; j<4; j++){
				int index = i*4 + j;
				JTextField field = (JTextField)MainFrame.boardPanel.getComponent(index);
				if(!field.getText().isEmpty()){
					board[i][j] = field.getText().charAt(0);
				}else{
					MainFrame.printMessage("Wszystkie pola musz¹ byæ wype³nione.");
					throw new fieldNotFilledException();
				}
			}
		}
		return board;
	}
	
	public boolean wordRepeats(ArrayList<Word> words, Word newWord){
		for(Word w: words){
			if(w.word.equals(newWord.word)){
				return true;
			}
		}
		return false;
	}
	
	public boolean pointRepeats(ArrayList<Point> points){
		for(int i=0; i<points.size()-1; i++){
			Point pi = points.get(i);
			for(int j=i+1; j<points.size(); j++){
				Point pj = points.get(j);
				if(pi.equals(pj)) return true;
			}
		}
		return false;
	}
	
	public void printBoard(){
		System.out.println("Board: ");
		for(int i=0; i<4; i++){
			for(int j=0; j<4; j++){
				System.out.print(board[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	class Word{
		public String word;
		public ArrayList<Point> points = new ArrayList<Point>();
		public Word(String word, ArrayList<Point> points){
			this.word = word;
			this.points = points;
		}
	}
	
	class fieldNotFilledException extends Exception{
		private static final long serialVersionUID = 1L;
		public fieldNotFilledException(){
			System.out.println("Wyst¹pi³ wyj¹tek. Nie wszystkie pola tablicy s¹ wype³nione");
		}
	}
	
	public void testGraph(){
		ArrayList<String> dic = (ArrayList<String>) dictionary.getDic().clone();
		ArrayList<Node> tree = new ArrayList<Node>();
		Node parent = new Node("aba");
		fg(parent, dic);
		
		for(Node n: parent.children){
			System.out.println(n.word);
			for(Node n2: n.children){
				System.out.println("	" + n2.word);
				for(Node n3: n2.children){
					System.out.println("		" + n3.word);
					for(Node n4: n3.children){
						System.out.println("			" + n4.word);
					}
				}
			}
			System.out.println();
		}
		
	}
	
	public void fg(Node parent, ArrayList<String> dic){
		int length = parent.word.length()+1;
		
		//System.out.println("Jestem w: " + parent.word);
		for(int i=0; i<100; i++){
			if(dic.get(i).length() == length && dic.get(i).contains(parent.word)){
				Node c = new Node(parent, dic.get(i));
				parent.children.add(c);
				fg(c, dic);
				dic.remove(i);
			}
		}
		for(int i=0; i<100; i++){
			if(dic.get(i).length() > length && dic.get(i).contains(parent.word)){
				Node c = new Node(parent, dic.get(i));
				parent.children.add(c);
				fg(c, dic);
				dic.remove(i);
			}
		}
	}
	
	
	
	class Node{
		public boolean root = false;
		public Node parent;
		public ArrayList<Node> children = new ArrayList<Node>();
		public String word;
		public Node(Node parent, String word){
			this.parent = parent;
			this.word = word;
		}
		public Node(String word){
			this.word = word;
			root = true;
		}
	}
	
	class Letter{
		public float val;
		public char c;
		public Letter(char c){
			this.c = c;
			val = c;
			if(c == '¹'){
				val = 'a' + 0.5f;
			}
			if(c == 'æ'){
				val = 'c' + 0.5f;
			}
			if(c == 'ê'){
				val = 'e' + 0.5f;
			}
			if(c == '³'){
				val = 'l' + 0.5f;
			}
			if(c == 'ñ'){
				val = 'n' + 0.5f;
			}
			if(c == 'ó'){
				val = 'o' + 0.5f;
			}
			if(c == 'œ'){
				val = 's' + 0.5f;
			}
			if(c == 'Ÿ'){
				val = 'z' + 0.5f;
			}
			if(c == '¿'){
				val = 'e' + 0.75f;
			}
		}
	}
	
	
}

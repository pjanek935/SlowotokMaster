package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.AbstractBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import main.SearchEngine.Word;

public class MainFrame extends JFrame{
	private static final long serialVersionUID = 1L;
	public static JPanel boardPanel; //contains 4x4 text fields; left half of the window
	public static JPanel controlPanel;//right half of the window
	public static JPanel buttonPanel;//in control panel
	public static JTextField messageBox;
	public static JButton searchButton;
	public static JMenuBar menuBar = new JMenuBar();
	public static DefaultListModel<String> listModel = new DefaultListModel<String>();//list that contains found words
	public static int boardFocus = 0;//pointer showing which textField in boardPanel is currently active
	public static int lengthSelected = 8;
	
	private Color bgColor = new Color(37, 9, 49);
	private static Color tileColor = new Color(77, 179, 184);
	private SearchEngine searchEngine = new SearchEngine();
	
	public MainFrame(){
		//Setting properties of the main frame
		super("S³owotokMaster");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setLocation(200, 200);
		setSize(600, 400);
		setLayout(new GridLayout(1, 2));
		setResizable(false);
		setBackground(bgColor);
		
		//Adding board panel
		boardPanel = new JPanel();
		boardPanel.setBackground(bgColor);
		boardPanel.setLayout(new GridLayout(4, 4, 16, 16));
		for (int i=0; i<16; i++){
			JTextField field = new JTextField(i);
			field.setHorizontalAlignment(JTextField.CENTER);
			field.setDocument(new JTextFieldLimit(1));
			field.setBackground(new Color(77, 179, 184));
			field.setBorder(new RoundedCornerBorder());
			field.addFocusListener(new FocusListener(){
				@Override
				public void focusGained(FocusEvent arg0) {
					for(int i=0; i<16; i++){
						if(boardPanel.getComponent(i).isFocusOwner() ){
							boardFocus = i;
							break;
						}
					}
				}
				@Override
				public void focusLost(FocusEvent arg0) {
					//DO NOTHING
				}
			});
			Font font = new Font("SansSerif", Font.BOLD, 40);
			field.setForeground(Color.white);
			field.setFont(font);
			boardPanel.add(field);
		}
		
		//Adding control panel
		controlPanel = new JPanel();
		controlPanel.setBackground(new Color(37, 9, 49));
		controlPanel.setLayout(new GridLayout(1, 2));
		
		//Creating and adding list
		JScrollPane scrollPane = new JScrollPane();
		JList<String> list = new JList<String>(listModel);
		ListSelectionModel listSelectionModel = list.getSelectionModel();
		listSelectionModel.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				for(Component c: boardPanel.getComponents()){
					c.setBackground(tileColor);
					c.setForeground(Color.white);
				}
				if(listModel.isEmpty()) return;
				Word selectedWord = searchEngine.foundWords.get(list.getSelectedIndex());
				for(Point p: selectedWord.points){
					boardPanel.getComponent(p.x*4 + p.y).setBackground(Color.white);
					boardPanel.getComponent(p.x*4 + p.y).setForeground(tileColor);
				}
			}
		});
		list.setLayoutOrientation(JList.VERTICAL);
		scrollPane.add(list);
		scrollPane.setViewportView(list);
		list.setBorder(new RoundedCornerBorder());
		scrollPane.setBorder(new RoundedCornerBorder());
		scrollPane.setBackground(bgColor);
		controlPanel.add(scrollPane);
		
		//Creating and adding button panel
		buttonPanel = new JPanel();
		buttonPanel.setBackground(bgColor);
		buttonPanel.setLayout(new GridLayout(8,2));
		controlPanel.add(buttonPanel);
		
		//Creating and adding buttons
		//searchButton
		searchButton = new JButton("SZUKAJ");
		searchButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(!searchEngine.searchingRunning){
					if(searchEngine.dictionary.loadingRunning()){
						printMessage("S³ownik nie za³adowany.");
					}else{
						listModel.clear();
						searchEngine.foundWords.clear();
						searchEngine.search();
					}
				}else{
					searchEngine.searchThread.stop();
					searchButton.setText("SZUKAJ");
					searchEngine.searchingRunning = false;
					printMessage("Zatrzymano wyszukiwanie.");
				}
				
			}
		});
		buttonPanel.add(searchButton);
		//printButton
		JButton printButton = new JButton("WYPISZ");
		printButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				org.opencv.core.Point p = Calibration.calibrate();
				System.out.println(p.x + " " + p.y);
				AutoControl.setStartPoint(new Point((int)p.x, (int)p.y));
				AutoControl.move(searchEngine.foundWords);
			}
		});
		buttonPanel.add(printButton);
		//Searchboard button;
		JButton searchBoardButton = new JButton("WYSZUKAJ PLANSZÊ");
		searchBoardButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				paintGreen();
				//searchEngine.setBoard();
				//Calibration.crop();
				Calibration.searchBoard();//
				paintDefault();
			}
		});
		buttonPanel.add(searchBoardButton);
		//Calibrate button
		/*JButton calibrateButton = new JButton("KALIBRUJ");
		calibrateButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				class Calibrate implements Runnable{
					@Override
					public void run() {
						searchEngine.calibrationRunning = true;
						printMessage("Wska¿ na punkt pocz¹tkowy.");
						try {
							Thread.sleep(1000);
							for(int i=4; i>0; i--){
								printMessage("Czekam jeszcze " + i + " sekund.");
								Thread.sleep(1000);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Point p = MouseInfo.getPointerInfo().getLocation();
						AutoControl.setStartPoint(p);
						printMessage("Punkt: " + p.x + ", " + p.y);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						searchEngine.calibrationRunning = false;
					}
				}
				Thread calibrateThread = new Thread(new Calibrate());
				calibrateThread.start();
			}
		});
		buttonPanel.add(calibrateButton);*/
		
		//Adding message box
		messageBox = new JTextField();
		messageBox.setEditable(false);
		buttonPanel.add(messageBox);
		
		//Adding menu bar
		//Calibration menu
		setJMenuBar(menuBar);
		JMenu calibrationMenu = new JMenu("Kalibracja");
		menuBar.add(calibrationMenu);
		JMenuItem checkCalibration = new JMenuItem("SprawdŸ kalibracjê punktu.");
		checkCalibration.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				printMessage("Punkt: " + AutoControl.points[0][0].x + ", " + AutoControl.points[0][0].y);
				AutoControl.moveToBeg();
			}
		});
		JMenuItem manualCali = new JMenuItem("Kalibruj");
		manualCali.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				org.opencv.core.Point p = Calibration.calibrate();
				System.out.println(p.x + " " + p.y);
				AutoControl.setStartPoint(new Point((int)p.x, (int)p.y));
				AutoControl.moveToBeg();
			}
		});
		calibrationMenu.add(checkCalibration);
		calibrationMenu.add(manualCali);
		//Word length range menu
		JMenu lengthMenu = new JMenu("Zakres d³ugoœci s³ów");
		menuBar.add(lengthMenu);
		ButtonGroup lengthGroup = new ButtonGroup();
		for(int i=3; i<10; i++){
			JRadioButtonMenuItem range = new JRadioButtonMenuItem(i + "-literowe");
			lengthGroup.add(range);
			lengthMenu.add(range);
			range.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					lengthSelected = Integer.parseInt(range.getText().split("-")[0]);
					System.out.println(lengthSelected);
				}
			});
			if(i==8)range.setSelected(true);
			if(i==9)range.setText(range.getText() + "(nie zalecane)");
		}
		
		//Adding whole stuff to main frame
		add(boardPanel);
		add(controlPanel);
		
		//Filling board with some random characters
		fillBoardRandom();
		
		//Opening dictionary
		searchEngine.loadDic();
		
		//Set first point of the table
		AutoControl.setStartPoint(new Point(240, 230));
		
		
	}
	
	public static void printMessage(String message){
		messageBox.setText(message);
	}
	
	public static void paintGreen(){
		for(Component c: boardPanel.getComponents()){
			c.setBackground(Color.white);
			c.setForeground(Color.white);
			c.update(c.getGraphics());
		}
	}
	
	public static void paintDefault(){
		for(Component c: boardPanel.getComponents()){
			c.setBackground(tileColor);
			c.setForeground(Color.white);
		}
	}
	
	public void fillBoardRandom(){
		for(int i=0; i<16; i++){
			char c = (char)(int)(Math.random()*24 + 65);
			JTextField field = (JTextField)boardPanel.getComponent(i);
			field.setText("" + c);
		}
	}
	
	//To limit number of characters in field
	class JTextFieldLimit extends PlainDocument{
		private static final long serialVersionUID = 1L;
		private int limit;
		JTextFieldLimit(int limit){
			super();
			this.limit = limit;
		}
		JTextFieldLimit(int limit, boolean upper){
			super();
			this.limit = limit;
		}
		public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException{
			if(str == null){
				return;
			}
			if(getLength() + str.length() <= limit){
				if(Character.isAlphabetic(str.charAt(0))){
					super.insertString(offset, str.toUpperCase(), attr);
					boardFocus += 1;
					if(boardFocus > 15){
						boardFocus = 0;
					}
					boardPanel.getComponent(boardFocus).requestFocusInWindow();
					JTextField field = (JTextField)boardPanel.getComponent(boardFocus);
					field.selectAll();
				}else{
					printMessage("Znaki musz¹ byæ literami.");
					System.out.println(str);
				}
			}
		}
	}
	
	//To make rounded fields borders
	class RoundedCornerBorder extends AbstractBorder {
		private static final long serialVersionUID = 1L;
		public RoundedCornerBorder(){}
	    @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
	        Graphics2D g2 = (Graphics2D) g.create();
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        int r = 16;
	        RoundRectangle2D round = new RoundRectangle2D.Float(x, y, width - 1, height - 1, r, r);
	        Container parent = c.getParent();
	        if (parent != null) {
	            g2.setColor(parent.getBackground());
	            Area corner = new Area(new Rectangle2D.Float(x, y, width, height));
	            corner.subtract(new Area(round));
	            g2.fill(corner);
	        }
	        g2.setColor(Color.BLACK);
	        g2.draw(round);
	        g2.dispose();
	    }
	    @Override public Insets getBorderInsets(Component c) {
	        return new Insets(4, 8, 4, 8);
	    }
	    @Override public Insets getBorderInsets(Component c, Insets insets) {
	        insets.set(4, 8, 4, 8);
	        return insets;
	    }
	}
	
}

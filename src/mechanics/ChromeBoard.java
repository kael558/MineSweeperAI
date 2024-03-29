package mechanics;

import java.awt.AWTException;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.Serial;

import enumerations.ActionType;
import enumerations.CellType;
import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import org.openqa.selenium.WebElement;	

public class ChromeBoard extends ObservableBoard  {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	WebDriver driver;
	Robot robot;
	
	public ChromeBoard(){
		super();
		createDriver();
		createRobot();
		int[] d = getData();
		
		super.ROWS = d[0];
		super.COLUMNS = d[1];
		super.totalBombs = d[2];
		updateObservableBoard();
	}

	private void createDriver() {
		String chromeDriverPath = "lib/chromedriver.exe" ;
		System.setProperty("webdriver.chrome.driver", chromeDriverPath);
		driver = new ChromeDriver();
		driver.get("http://minesweeperonline.com/");
		driver.manage().window().maximize();
		System.out.println("Succesfully created WebDriver");
	}
	
	private void createRobot() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	private int[] getData() {
		WebElement game = driver.findElement(By.id("game"));
		
		String html = game.getAttribute("innerHTML");
		String style = game.getAttribute("style");
		
		int [] d = calculateDimensions(style);	
		d[2] = getFlagsLeft(html);
		return d;
	}

	
	private int[] calculateDimensions(String style){
		//get width
		String widthSearch = "width: ";
		int beginIndex = style.indexOf(widthSearch);
		int endIndex = style.indexOf("px");
		
		String widthString = style.substring(beginIndex+7, endIndex);

		//get height
		String heightSearch = "height: ";
		beginIndex = style.indexOf(heightSearch);
		endIndex = style.indexOf("px", endIndex+1);
		
		String heightString = style.substring(beginIndex+8, endIndex);
		
		int totalWidth = Integer.parseInt(widthString);
		int totalHeight = Integer.parseInt(heightString);
		
		//may need to get sizes for each individual borders...
		int row = (totalHeight - 10 - 24 - 10 - 10)/16;
		int col = (totalWidth-20)/16;

		return new int[]{row, col, 0};
	}
	
	public int getFlagsLeft(String HTML){
		/*Get number of bombs*/
		int num = 0;
		
		String searchDigits = " id=\"mines_hundreds\"";
		int indexDigits = HTML.indexOf(searchDigits, 100);
		
		String timeSearchDigits = "=\"";
		int indexLookBombs = HTML.indexOf(timeSearchDigits, indexDigits-10);
		
		String number = HTML.substring(indexLookBombs+6, indexDigits-1);
		num+=Integer.parseInt(number)*100;

		searchDigits = " id=\"mines_tens\"";
		indexDigits = HTML.indexOf(searchDigits, 100);
		
		timeSearchDigits = "=\"";
		indexLookBombs = HTML.indexOf(timeSearchDigits, indexDigits-10);
		
		number = HTML.substring(indexLookBombs+6, indexDigits-1);
		num+=Integer.parseInt(number)*10;
		
		searchDigits = " id=\"mines_ones\"";
		indexDigits = HTML.indexOf(searchDigits, 100);
		
		timeSearchDigits = "=\"";
		indexLookBombs = HTML.indexOf(timeSearchDigits, indexDigits-10);
		
		number = HTML.substring(indexLookBombs+6, indexDigits-1);
		num+=Integer.parseInt(number);
		
		return num;
	}
	
	public void updateObservableBoard(){
		try{
			String HTML = driver.findElement(By.id("game")).getAttribute("innerHTML");
			
			/*Get squares*/
			int indexSquare = 0;
			resetSquaresRevealedCount();
			for (int row = 1; row <= ROWS; row++){
				for (int col = 1; col <= COLUMNS; col++){
					String squareSearch = " id=\"" + row + "_" + col + "\"";
					indexSquare = HTML.indexOf(squareSearch, indexSquare+9);
					
					String classSearch = "=\"";
					int indexLook = HTML.indexOf(classSearch, indexSquare - 25);
					
					String classType = HTML.substring(indexLook+2, indexSquare-1);
					switch (classType) {
						case "square blank" -> super.observableBoard[row - 1][col - 1] = CellType.HIDDEN;
						case "square open0" -> {
							super.observableBoard[row - 1][col - 1] = CellType.SQUARE0;
							incrementSquaresRevealedCount();
						}
						case "square open1" -> {
							super.observableBoard[row - 1][col - 1] = CellType.SQUARE1;
							incrementSquaresRevealedCount();
						}
						case "square open2" -> {
							super.observableBoard[row - 1][col - 1] = CellType.SQUARE2;
							incrementSquaresRevealedCount();
						}
						case "square open3" -> {
							super.observableBoard[row - 1][col - 1] = CellType.SQUARE3;
							incrementSquaresRevealedCount();
						}
						case "square open4" -> {
							super.observableBoard[row - 1][col - 1] = CellType.SQUARE4;
							incrementSquaresRevealedCount();
						}
						case "square open5" -> {
							super.observableBoard[row - 1][col - 1] = CellType.SQUARE5;
							incrementSquaresRevealedCount();
						}
						case "square open6" -> {
							super.observableBoard[row - 1][col - 1] = CellType.SQUARE6;
							incrementSquaresRevealedCount();
						}
						case "square open7" -> {
							super.observableBoard[row - 1][col - 1] = CellType.SQUARE7;
							incrementSquaresRevealedCount();
						}
						case "square open8" -> {
							super.observableBoard[row - 1][col - 1] = CellType.SQUARE8;
							incrementSquaresRevealedCount();
						}
						case "square bombflagged", "square bombmisflagged" -> super.observableBoard[row - 1][col - 1] = CellType.FLAGGED;
						case "square bombrevealed", "square bombdeath" -> super.observableBoard[row - 1][col - 1] = CellType.BOMB;
						default -> throw new IllegalArgumentException("Unknown case");
					}
				}
			}
		}catch(UnhandledAlertException e){
			System.exit(0);
		}
	}
	
	/*CLICKERS	*/
	public void playMove(Action action){
		if (action.actionType == ActionType.CLICK)
			clickCellInitial(action.row, action.col);
		else
			flagCell(action.row, action.col);
		updateObservableBoard();
		updateGameCondition();
	}

	public void clickCellInitial(int row, int col){
		String square = (row+1) + "_" + (col+1);
		//driver.findElement(By.id(square)).click();

		org.openqa.selenium.Point p = driver.findElement(By.id(square)).getLocation();
		//Keys.
		int x = p.x+5;
		int y = p.y+120;
		robot.mouseMove(x, y);    
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		//robot.delay(2000);
	}
	
	public void flagCell(int row, int col) {
		String square = (row+1) + "_" + (col+1);
		//driver.findElement(By.id(square)).sendKeys(Keys.SPACE);
	//	System.out.print
		//System.out.println(square);
		org.openqa.selenium.Point p = driver.findElement(By.id(square)).getLocation();
		
		
		int x = p.x+5;
		int y = p.y+120;
		robot.mouseMove(x, y);    
		robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
		robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
		//robot.delay(2000);
	}
	

}

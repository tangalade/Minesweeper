package board;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.imageio.ImageIO;

public class ScreenAnalyzer {

	enum Type {
		MINE,UNKNOWN,AUTO,ONE,TWO,THREE,FOUR,FIVE,SIX,SEVEN,EIGHT,WON,LOST,NUM_TYPES
	}
	enum Version {
		WINDOWS_XP, WINDOWS_7, WINDOWS_8, ONLINE, NUM_VERSIONS
	}
	
	public static Version version = Version.ONLINE;
	
	private static HashMap<Type,BufferedImage> squareImages = new HashMap<Type,BufferedImage>();
	private static Queue<Square> clicked = new LinkedList<Square>();
	private static Type[][] types;
	
	private static Robot robot;

	private static int BOARD_LEFT = 184;
	private static int BOARD_RIGHT = 1390;
	private static int BOARD_TOP = 118;
	private static int BOARD_BOT = 750;
	private static double SQUARE_WIDTH = 41.65;
	private static double SQUARE_HEIGHT = 41.65;
	private static int X_PLAY = 1;
	private static int Y_PLAY = 1;
	private static int NEW_GAME_X = 533;
	private static int NEW_GAME_Y = 686;
	private static int X_SKIP = 0;
	private static int Y_SKIP = 0;
	private static int SQUARE_DELAY = 0;
	
	public ScreenAnalyzer( int rows, int cols )
	{
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}

		switch (version)
		{
		case WINDOWS_8:
			BOARD_LEFT = 184;
			BOARD_RIGHT = 1390;
			BOARD_TOP = 118;
			BOARD_BOT = 750;
			SQUARE_WIDTH = 41.65;
			SQUARE_HEIGHT = 41.65;
			X_PLAY = 1;
			Y_PLAY = 1;
			NEW_GAME_X = 533;
			NEW_GAME_Y = 686;
			SQUARE_DELAY = 50;
			break;
		case WINDOWS_7:
			BOARD_LEFT = 178;
			BOARD_RIGHT = 1438;
			BOARD_TOP = 120;
			BOARD_BOT = 792;
			SQUARE_WIDTH = 42;
			SQUARE_HEIGHT = 42;
			X_PLAY = 0;
			Y_PLAY = 0;
			NEW_GAME_X = 902;
			NEW_GAME_Y = 510;
			SQUARE_DELAY = 50;
			break;
        case WINDOWS_XP:
			SQUARE_WIDTH = 16;
			SQUARE_HEIGHT = 16;
			X_PLAY = 0;
			Y_PLAY = 0;
			X_SKIP = 5;
			Y_SKIP = 5;
			findSmiley();
			break;
        case ONLINE:
			SQUARE_WIDTH = 16;
			SQUARE_HEIGHT = 16;
			X_PLAY = 0;
			Y_PLAY = 0;
			X_SKIP = 5;
			Y_SKIP = 5;
			findSmiley();
			break;
        default:
			System.out.println("You're playing with fire kid, be careful");
			System.exit(1);
			break;
		}
		System.out.format("(%04d,%04d)----%04d----(%04d,%04d)\n",
				BOARD_LEFT,BOARD_TOP,BOARD_RIGHT-BOARD_LEFT,BOARD_RIGHT,BOARD_TOP);
		System.out.format("      |                       |\n");
		System.out.format("    %05d                   %05d\n", BOARD_BOT-BOARD_TOP,BOARD_BOT-BOARD_TOP);
		System.out.format("      |                       |\n");
		System.out.format("(%04d,%04d)----%04d----(%04d,%04d)\n",
				BOARD_LEFT,BOARD_BOT,BOARD_RIGHT-BOARD_LEFT,BOARD_RIGHT,BOARD_BOT);
		System.out.println(SQUARE_HEIGHT + " x " + SQUARE_WIDTH + " square");
		types = new Type[rows][cols];
		for ( int i=0; i<rows; i++ )
			for ( int j=0; j<cols; j++ )
				types[i][j] = Type.UNKNOWN;
		try {
			squareImages.put(Type.MINE,ImageIO.read(new File("squares/" + version.name() + "/mine.jpg")));
			squareImages.put(Type.UNKNOWN,ImageIO.read(new File("squares/" + version.name() + "/unknown.jpg")));
			squareImages.put(Type.AUTO,ImageIO.read(new File("squares/" + version.name() + "/auto.jpg")));
//			squareImages.put(Type.WON,ImageIO.read(new File("squares/" + version.name() + "/won.jpg")));
//			squareImages.put(Type.LOST,ImageIO.read(new File("squares/" + version.name() + "/lost.jpg")));
			for ( int i=0; i<8; i++ )
			{
				try {
					squareImages.put(Type.values()[Type.ONE.ordinal()+i], ImageIO.read(new File("squares/" + version.name() + "/" + (i+1) + ".jpg")));
				} catch (IOException e) {
					System.out.println("only have numbered squares up to " + i);
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void reset()
	{
		clicked = new LinkedList<Square>();

		types = new Type[Board.rows][Board.cols];
		for ( int i=0; i<Board.rows; i++ )
			for ( int j=0; j<Board.cols; j++ )
				types[i][j] = Type.UNKNOWN;

		robot.mouseMove(NEW_GAME_X, NEW_GAME_Y);
		robot.mousePress(InputEvent.BUTTON1_MASK);
	    robot.mouseRelease(InputEvent.BUTTON1_MASK);
	    robot.delay(100);
	}

	public static void findSmiley()
    {
        BufferedImage board = robot.createScreenCapture(
                                                        new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

        BufferedImage smiley;
        try {
            smiley = ImageIO.read(new File("squares/" + version.name() + "/smiley.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        int rgb = -256;
        for ( int i=0; i<board.getWidth(); i+=5 )
        {
            for ( int j=0; j<board.getHeight(); j+=5 )
            {
                /* if pixel color is the same as the center of the smiley image, search around it */
                if ( board.getRGB(i,j) == rgb )
                {
                    Dimension coord = isSmiley(board,smiley,i,j);
                    System.out.println("found a pixel at " + i + "," + j);
                    if ( coord != null )
                    {
                        System.out.println("Smiley is at (" + coord.getWidth() + "," + coord.getHeight() + ")");
                        // USE these assignments when using real minesweeper, the current ones are for the online version
//            			BOARD_LEFT = (int) (coord.getWidth() - 229);
//            			BOARD_RIGHT = (int) (Board.rows*SQUARE_WIDTH + BOARD_LEFT);
//            			BOARD_TOP = (int) (coord.getHeight() + 38);
//            			BOARD_BOT = (int) (Board.cols*SQUARE_HEIGHT) + BOARD_TOP;
            			BOARD_LEFT = (int) (coord.getWidth() - 230);
            			BOARD_RIGHT = (int) (Board.rows*SQUARE_WIDTH + BOARD_LEFT);
            			BOARD_TOP = (int) (coord.getHeight() + 36);
            			BOARD_BOT = (int) (Board.cols*SQUARE_HEIGHT) + BOARD_TOP;
            			NEW_GAME_X = (int) (coord.getWidth() + smiley.getWidth()/2);
            			NEW_GAME_Y = (int) (coord.getHeight() + smiley.getHeight()/2);
            			return;
                    }
                    else
                    {
                    	System.out.println("Unable to find smiley");
                    	System.exit(1);
                    }
                }
            }
        }
    }

    /* 230 left, 38 down from smiley is first square */
    private static Dimension isSmiley( BufferedImage board, BufferedImage smiley, int x, int y )
    {
        double min_variation = 200;
        Dimension min_dimension = null;
        for ( int boardX=(x-smiley.getWidth()); boardX<x; boardX++ )
        {
            for ( int boardY=(y-smiley.getHeight()); boardY<y; boardY++ )
            {
                double variation = 0;
                for(int smileyX = 0;smileyX < smiley.getWidth();smileyX++)
                    for(int smileyY = 0;smileyY < smiley.getHeight();smileyY++)
                        variation += compareARGB(board.getRGB(boardX+smileyX,boardY+smileyY),
                                                 smiley.getRGB(smileyX,smileyY))/Math.sqrt(3);
                if ( variation < min_variation )
                {
                	min_variation = variation;
                	min_dimension = new Dimension(boardX,boardY);
                }
            }
        }
        System.out.println("min variation: " + min_variation);
        return min_dimension;
    }
    
    public static boolean getRevealed()
	{
		BufferedImage board = robot.createScreenCapture(
				new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
		return findSquares(board);
	}
	
	private static boolean findSquares( BufferedImage board ){
		Set<Square> done = new HashSet<Square>();
		Set<Square> unrevealed = Board.unrevealed();
		
		BufferedImage markedImg = board;
		
		for ( Square square: clicked )
			done.add(square);
		
		while ( !clicked.isEmpty() )
		{
			Square square = clicked.poll();
			if (Board.DEBUG)
				System.out.println("processing " + square);
			int y = yCoord2Pixel(square.row);
			int x = xCoord2Pixel(square.col);
		
			Type type = detectType(board,x,y);
			switch (type)
			{
			case MINE:
			case UNKNOWN:
				break;
			case NUM_TYPES:
				System.out.println("Must be a compiler bug");
				System.exit(2);
			case AUTO:
				Board.processed(square);
				Set<Square> related = Board.related(square);
				for ( Square rel:related )
					if ( unrevealed.contains(rel) && !done.contains(rel))
					{
						clicked.add(rel);
						done.add(rel);
					}
				break;
			case ONE:
			case TWO:
			case THREE:
			case FOUR:
			case FIVE:
			case SIX:
			case SEVEN:
			case EIGHT:
				Square sq = new Square(square.row,square.col,type.ordinal()-Type.ONE.ordinal()+1);
				Board.save(sq);
				Board.addWork(sq);
				break;
			case WON:
				System.out.println("Congratulations, you won!");
				MinesweeperRobot.reset();
				return false;
			case LOST:
				System.out.println("What a failure you are, you lost");
				MinesweeperRobot.reset();
				return false;
			default:
				System.out.println("What...");
				break;
			}
			types[square.row][square.col] = type;
			if (Board.DEBUG)
				System.out.println("detected " + square + " " + type);
		}
		
		if ( Board.DEBUG )
		{
			for ( int i=0; i<Board.rows; i++ )
				for ( int j=0; j<Board.cols; j++ )
					markedImg = process(markedImg,xCoord2Pixel(i),yCoord2Pixel(j),(int)(types[i][j].ordinal()-Type.ONE.ordinal()+1) + "");
			try {
				ImageIO.write(markedImg, "jpg", new File("processed.jpg"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (Board.DEBUG)
			System.out.println("detected: " + done);
		return true;
	}

	private static Type detectType( BufferedImage board, int x, int y )
	{
		double min = 1;
		Type detectedType = Type.UNKNOWN;
		for ( int xplay = -X_PLAY; xplay <= X_PLAY; xplay++ )
		   for ( int yplay = -Y_PLAY; yplay <= Y_PLAY; yplay++ )
		   {
			   for ( Type type: squareImages.keySet() )
			   {
				   BufferedImage square = squareImages.get(type);
				   double comp = compareImages(
						   board.getSubimage((int)x + xplay,(int)y + yplay,
								   square.getWidth(),square.getHeight()),square);
				   if(comp < min){
					   min = comp;
					   detectedType = type;
				   }
			   }
		   }
		return detectedType;
	}
	
	public static int xCoord2Pixel( int col )
	{
		return (int) (BOARD_LEFT + col*SQUARE_WIDTH);
	}
	
	public static int yCoord2Pixel( int row )
	{
		return (int) (BOARD_TOP + row*SQUARE_HEIGHT);
	}
	
	private static BufferedImage process( BufferedImage old, int x, int y, String str )
	{
		int w = old.getWidth();
		int h = old.getHeight();
		BufferedImage img = new BufferedImage(
				w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.drawImage(old, 0, 0, null);
		g2d.setPaint(Color.black);
		g2d.setFont(new Font("Serif", Font.BOLD, 20));
	    FontMetrics fm = g2d.getFontMetrics();
	    y += fm.getHeight();
		g2d.drawString(str, x, y );
		g2d.dispose();
		return img;
	}
	
	/**
	 * Determines how different two identically sized regions are.
	 */
	private static double compareImages(BufferedImage im1, BufferedImage im2){
		assert(im1.getHeight() == im2.getHeight() && im1.getWidth() == im2.getWidth());
		double variation = 0.0;
		for(int x = X_SKIP; x < im1.getWidth()-X_SKIP; x++){
			for(int y = Y_SKIP; y < im1.getHeight()-Y_SKIP; y++){
				variation += compareARGB(im1.getRGB(x,y),im2.getRGB(x,y))/Math.sqrt(3);
			}
		}
		return variation/(im1.getWidth()*im1.getHeight());
	}

	 /**
	 * Calculates the difference between two ARGB colours (BufferedImage.TYPE_INT_ARGB).
	 */
	 private static double compareARGB(int rgb1, int rgb2){
		 double r1 = ((rgb1 >> 16) & 0xFF)/255.0; double r2 = ((rgb2 >> 16) & 0xFF)/255.0;
		 double g1 = ((rgb1 >> 8) & 0xFF)/255.0;  double g2 = ((rgb2 >> 8) & 0xFF)/255.0;
		 double b1 = (rgb1 & 0xFF)/255.0;         double b2 = (rgb2 & 0xFF)/255.0;
		 double a1 = ((rgb1 >> 24) & 0xFF)/255.0; double a2 = ((rgb2 >> 24) & 0xFF)/255.0;
		 // if there is transparency, the alpha values will make difference smaller
		 return a1*a2*Math.sqrt((r1-r2)*(r1-r2) + (g1-g2)*(g1-g2) + (b1-b2)*(b1-b2));
	 }
	 
	 public static void click( Square square )
	 {
		 robot.mouseMove((int)(ScreenAnalyzer.xCoord2Pixel(square.col)+SQUARE_WIDTH/2), (int)(ScreenAnalyzer.yCoord2Pixel(square.row)+SQUARE_WIDTH/2));
		 robot.mousePress(InputEvent.BUTTON1_MASK);
//			robot.delay(50);
		 robot.mouseRelease(InputEvent.BUTTON1_MASK);
//			robot.delay(50);
		 clicked.add(square);
		 robot.delay(SQUARE_DELAY);
	 }
	 public static void rightClick( Square square )
	 {
		 robot.mouseMove((int)(ScreenAnalyzer.xCoord2Pixel(square.col)+SQUARE_WIDTH/2), (int)(ScreenAnalyzer.yCoord2Pixel(square.row)+SQUARE_WIDTH/2));
		 robot.mousePress(InputEvent.BUTTON3_MASK);
		 robot.mouseRelease(InputEvent.BUTTON3_MASK);
//		 robot.delay(100);
	 }
}

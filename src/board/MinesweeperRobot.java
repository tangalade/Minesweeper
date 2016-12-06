package board;

import java.awt.AWTException;
import java.awt.Robot;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import board.Board.Status;

/* when analyzing screenshot, only add squares to work queue with status UNKNOWN or SAFE
 * squares that are empty, set to status SAFE_PROCESSED and do not add to work queue
 * squares with a number, just add to work queue */
public class MinesweeperRobot {
	
	public static Square START_SQUARE = new Square(2,2);
	private final int ROWS = 16;
	private final int COLS = 30;
	private boolean MARK_FLAGS = false;
	private boolean MARK_FLAGS_FIRST = true;
	
	private Queue<Square> clicks = new LinkedList<Square>();
	private Queue<Square> rightClicks = new LinkedList<Square>();
	private Robot robot;
	
	public MinesweeperRobot() throws AWTException
	{
		new Board(ROWS,COLS);
		new ScreenAnalyzer(ROWS,COLS);
		robot = new Robot();
		robot.setAutoDelay(40);
	    robot.setAutoWaitForIdle(true);
	}
	
	public void processWork()
	{
		while ( Board.hasWork() )
		{
			Square square = Board.getWork();
			SubSet subset = new SubSet(square);
			for ( Square sq: Board.related(square) )
				subset.add(sq);
			Board.pool().add(subset);
			Board.processed(square);
		}
	}
	
	public void consolidatePool()
	{
		Queue<SubSet> post = new LinkedList<SubSet>();
		boolean changed;
		do
		{
			changed = false;
			for ( Iterator<SubSet> it = Board.pool().iterator(); it.hasNext(); )
			{
				SubSet subi = it.next();
				/* if any squares in the subset are not UNKNOWN on the board, remove them from the subset */
				for ( Iterator<Square> its = subi.squares.iterator(); its.hasNext(); )
				{
					Square square = its.next();
					if ( !Board.status(square).equals(Status.UNKNOWN) )
					{
						if (Board.status(square).equals(Status.MINE) )
						{
							subi.flag(square);
							its.remove();
							changed = true;
						}
						else if ( Board.status(square).equals(Status.SAFE) || Board.status(square).equals(Status.SAFE_PROCESSED) )
						{
							subi.save(square);
							its.remove();
							changed = true;
						}
					}
				}
				/* if subset no longer has any flags, click all squares in subset and remove from pool */
				if ( subi.flags == 0 )
				{
					for ( Square square: subi.squares )
					{
						Board.save(square);
						clicks.add(square);
					}
					it.remove();
					changed = true;
					continue;
				}
				/* if the subset has the same number of squares as number of flags, mark all as mines */
				if ( subi.flags == subi.squares.size() )
				{
					for ( Square square: subi.squares )
					{
						Board.flag(square);
						rightClicks.add(square);
					}
					it.remove();
					changed = true;
					continue;
				}
			}
			for ( Iterator<SubSet> it = Board.pool().iterator(); it.hasNext(); )
			{
				SubSet subi = it.next();
				/* if another subset is a subset of the current subset, split the current subset */
				for ( SubSet subj: Board.pool() )
				{
					if ( subj.subsetOf(subi) )
					{
						subi.split(subj);
						changed = true;
					}
					/* if subset A - (intersection of A and B) has less squares than the number of flags in A
					 * then the intersection of A and B must have the remaining flags
					 */
					Set<Square> exc = subi.exclude(subj);
					if ( exc.size() > 0 && exc.size() < subi.flags && subj.flags == subi.flags-exc.size() )
					{
						post.add(subi.exclude(exc));
						changed = true;
					}
				}
			}
			while ( !post.isEmpty() )
				Board.pool().add(post.poll());
		} while (changed);
	}
	
	public boolean processClicks()
	{
		if ( MARK_FLAGS && MARK_FLAGS_FIRST )
			while ( !rightClicks.isEmpty() )
				rightClick(rightClicks.poll());

		if ( clicks.isEmpty() )
			if ( !guess() )
				return false;
		while ( !clicks.isEmpty() )
			click(clicks.poll());

		if ( MARK_FLAGS && !MARK_FLAGS_FIRST )
			while ( !rightClicks.isEmpty() )
				rightClick(rightClicks.poll());

		robot.delay(100);
		return true;
	}
	
	/**
	 *  Find the SubSet in the pool with the lowest probability of having a flag
	 *  Pick a random Square in that SubSet and add it to {@code clicks}.
	 */
	private boolean guess()
	{
		System.out.println("GUESSING");
		Square guess = null;
		float probability = 1;
		for ( SubSet subset: Board.pool() )
		{
			if ( subset.probability() < probability )
			{
				probability = subset.probability();
				int idx = new Random().nextInt(subset.squares.size());
				guess = (Square) subset.squares.toArray()[idx];
			}
		}
		/* if pool was empty, pick first square with UNKNOWN status
		 * starting from top left, moving right then down */
		if ( guess == null )
		{
			search:
			for ( int i=0; i<Board.rows(); i++ )
			{
				for ( int j=0; j<Board.cols(); j++ )
				{
					if ( Board.status(i,j) == Status.UNKNOWN )
					{
						guess = new Square(i,j);
						break search;
					}
				}
			}
			System.out.println("Unable to guess, no squares are UNKNOWN");
			reset();
			return false;
		}
		Board.save(guess);
		clicks.add(guess);
		return true;
	}
	
	public static void reset()
	{
		// click for animation to finish quickly, whether loss or win
		click(START_SQUARE);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// click for animation to finish quickly, whether loss or win
		click(START_SQUARE);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Board.reset();
		ScreenAnalyzer.reset();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void click( Square square )
	{
		ScreenAnalyzer.click(square);
	}
	
	private void rightClick( Square square )
	{
		ScreenAnalyzer.rightClick(square);
	}
	
	private void firstClick()
	{
		click(START_SQUARE);
		robot.delay(2000);
	}
	
	public boolean newScreenCapture()
	{
		return ScreenAnalyzer.getRevealed();
	}
	
	public static void main(String[] args) throws InterruptedException, AWTException
	{
		MinesweeperRobot robot = new MinesweeperRobot();
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		robot.firstClick();
		for (;;)
		{
			robot.firstClick();
			for(;;)
			{
                if ( !robot.newScreenCapture() )
					break;
				robot.processWork();
				robot.consolidatePool();
				if ( !robot.processClicks() )
					break;
//                System.out.println("square (2,2): " + Board.status(2,2));
//                return;
			}
		}
	}
}

package board;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/* NOTE: all coordinates from top left */
public class Board {

	public static int rows;
	public static int cols;
	public static boolean DEBUG = false;
	
	public static Status squares[][];
	private static Queue<Square> workQueue = new LinkedList<Square>();
	private static ArrayList<SubSet> pool  = new ArrayList<SubSet>();
	
	public enum Status
	{
		UNKNOWN, MINE, SAFE, SAFE_PROCESSED
	}
	
	public Board(int rows, int cols)
	{
		Board.rows = rows;
		Board.cols = cols;
		squares = new Status[rows][cols];
		for ( int i=0; i<rows; i++ )
			for ( int j=0; j<cols; j++ )
				squares[i][j] = Status.UNKNOWN;
	}
	
	public static void reset()
	{
		squares = new Status[rows][cols];
		for ( int i=0; i<rows; i++ )
			for ( int j=0; j<cols; j++ )
				squares[i][j] = Status.UNKNOWN;
		workQueue = new LinkedList<Square>();
		pool = new ArrayList<SubSet>();
	}

	public static Set<Square> unrevealed()
	{
		Set<Square> unrevealed = new HashSet<Square>();
		for ( int i=0; i<rows; i++ )
			for ( int j=0; j<cols; j++ )
				if ( squares[i][j].equals(Status.UNKNOWN) || squares[i][j].equals(Status.SAFE) )
					unrevealed.add(new Square(i,j));
		return unrevealed;
	}
	
	public static Set<Square> mined()
	{
		Set<Square> mined = new HashSet<Square>();
		for ( int i=0; i<rows; i++ )
			for ( int j=0; j<cols; j++ )
				if ( squares[i][j].equals(Status.MINE) )
					mined.add(new Square(i,j));
		return mined;
	}
	
	private static boolean validRow( int row )
	{
		return row >= 0 && row < rows;
	}
	
	private static boolean validCol( int col )
	{
		return col >= 0 && col < cols;
	}
	
	public static Set<Square> related( Square square )
	{
		Set<Square> related = new HashSet<Square>();
		for ( int i=square.row-1; i<=square.row+1; i++ )
		{
			if ( validRow(i) )
			{
				if ( validCol(square.col-1) )
					related.add(new Square(i,square.col-1));
				if ( validCol(square.col+1) )
					related.add(new Square(i,square.col+1));
			}
		}
		if ( validRow(square.row-1) )
			related.add(new Square(square.row-1,square.col));
		if ( validRow(square.row+1) )
			related.add(new Square(square.row+1,square.col));
		
		return related;
	}
	
	public static Status status( Square square )
	{
		if ( !isValid(square) )
			return Status.UNKNOWN;
		return squares[square.row][square.col];
	}
	
	public static Status status( int row, int col )
	{
		if ( !isValid(new Square(row,col)) )
			return Status.UNKNOWN;
		return squares[row][col];
	}
	
	public static int rows()
	{
		return rows;
	}
	
	public static int cols()
	{
		return cols;
	}
	
	public static ArrayList<SubSet> pool()
	{
		return pool;
	}
	
	public static boolean addWork( Square square )
	{
		return workQueue.add(square);
	}
	
	public static Square getWork()
	{
		return workQueue.poll();
	}
	
	public static boolean hasWork()
	{
		return !workQueue.isEmpty();
	}
	
	public static void reveal( Square square )
	{
		workQueue.add(square);
	}
	
	public static boolean flag( Square square )
	{
		if ( !isValid(square) )
			return false;
		squares[square.row][square.col] = Status.MINE;
		return true;
	}

	public static boolean save( Square square )
	{
		if ( !isValid(square) )
			return false;
		squares[square.row][square.col] = Status.SAFE;
		return true;
	}

	public static boolean processed( Square square )
	{
		if ( !isValid(square) )
			return false;
		squares[square.row][square.col] = Status.SAFE_PROCESSED;
		return true;
	}

	private static boolean isValid( Square square )
	{
		return ( square.row < rows ) && ( square.col < cols );
	}
}

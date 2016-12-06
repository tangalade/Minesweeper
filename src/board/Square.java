package board;

public class Square
{
	public int row;
	public int col;
	public int flags = 0;
	
	public Square( int row, int col, int flags )
	{
		this.row = row;
		this.col = col;
		this.flags = flags;
	}

	public Square( int row, int col )
	{
		this.row = row;
		this.col = col;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + col;
		result = prime * result + flags;
		result = prime * result + row;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Square other = (Square) obj;
		if (col != other.col)
			return false;
		if (flags != other.flags)
			return false;
		if (row != other.row)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + row + "," + col + ")";
	}
	

}
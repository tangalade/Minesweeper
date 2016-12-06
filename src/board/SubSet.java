package board;

import java.util.HashSet;
import java.util.Set;

public class SubSet
{
	public Set<Square> squares = new HashSet<Square>();
	public int flags;
	public Square source;
	
	public SubSet( int flags )
	{
		this.flags = flags;
	}
	
	public SubSet( Square square )
	{
		flags = square.flags;
		source = square;
	}
	
	public float probability()
	{
		return flags/squares.size();
	}
	
	public boolean add( Square square )
	{
		return squares.add(square);
	}
	
	public boolean flag( Square square )
	{
		flags--;
		return true;
	}
	
	public boolean save( Square square )
	{
		return true;
	}
	
	public boolean subsetOf( SubSet subset )
	{
		if ( subset.squares.size() <= squares.size() )
			return false;
		for ( Square square: squares )
		{
			if ( !subset.squares.contains(square) )
				return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "SubSet [squares=" + squares + ", flags=" + flags + ", source="
				+ source + "]";
	}

	/**
	 * Removes the {@code Squares} in {@code subset} from this {@code SubSet}.
	 * Subtracts the flags in {@code subset} from this {@code SubSet}. 
	 */
	public boolean split( SubSet subset )
	{
		if ( subset.flags > flags )
			return false;

		for ( Square square: subset.squares )
			if (!squares.contains(square))
				return false;

		for ( Square square: subset.squares )
			squares.remove(square);
		flags -= subset.flags;
			
		return true;
	}

	
	public Set<Square> exclude( SubSet subi )
	{
		Set<Square> set = new HashSet<Square>();
		for ( Square square: squares )
			if (!subi.squares.contains(square))
				set.add(square);
		return set;
	}

	/**
	 * Return a new {@code SubSet} with the excluded squares.
	 */
	public SubSet exclude( Set<Square> exc )
	{
		SubSet set = new SubSet(exc.size());
		for ( Square square: exc )
		{
			squares.remove(square);
			set.add(square);
		}
		set.source = source;
		flags -= exc.size();
		return set;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + flags;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((squares == null) ? 0 : squares.hashCode());
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
		SubSet other = (SubSet) obj;
		if (flags != other.flags)
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (squares == null) {
			if (other.squares != null)
				return false;
		} else if (!squares.equals(other.squares))
			return false;
		return true;
	}
}


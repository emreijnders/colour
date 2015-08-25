package emr.colour.colourlist;

public class ColourRange
{
	private ColourType type;
	private int min;
	private int max;
	
	public ColourRange( ColourType type )
	{
		this.type = type;
		min = Integer.MIN_VALUE;
		max = Integer.MAX_VALUE;
	}
	
	//min is inclusive, max is exclusive
	public ColourRange( ColourType type , int min , int max )
	{
		this.type = type;
		this.min = min;
		this.max = max;
	}
	
	public void setMin( int min )
	{
		this.min = min;
	}
	
	public void setMax( int max )
	{
		this.max = max;
	}
	
	public int getMin()
	{
		return min;
	}
	
	public int getMax()
	{
		return max;
	}
	
	public ColourType getType()
	{
		return type;
	}
	
	@Override
	public String toString()
	{
		return type + ": " + min + " - " + max;
	}
	
	@Override
	public boolean equals( Object o )
	{
		boolean answer = false;
		if( o == this )
		{
			answer = true;
		}
		else if( o != null && o instanceof ColourRange )
		{
			ColourRange other = (ColourRange) o;
			if( other.getType().equals( type ) )
			{
				answer = true;
			}
		}
		return answer;
	}
	
	@Override
	public int hashCode()
	{
		return type.hashCode();
	}
}
package emr.colour.colourlist;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class ColourList
{
	private Set<ColourRange> rangelist;
	
	public ColourList()
	{
		rangelist = new HashSet<>();
		for( ColourType type : ColourType.values() )
		{			
			rangelist.add( new ColourRange( type ) );
		}
	}
	
	//this adds a range to the list,
	//replacing any existing one of the same type
	public void addRange( ColourRange range )
	{
		rangelist.remove( range );
		rangelist.add( range );
	}
	
	public Set<ColourRange> getRangeList()
	{
		return rangelist;
	}
	
	public List<Colour> getColourList( int bits )
	{
		List<Colour> list = new ArrayList<>();
		int number = (int) Math.pow( 2 , bits );
		int multiplier = 256 / number;
		for( int red = 0; red < number; red++ )
		{
			for( int green = 0; green < number; green++ )
			{
				for( int blue = 0; blue < number; blue++ )
				{
					Colour colour = new Colour( red * multiplier , green * multiplier , blue * multiplier );
					if( isInRange( colour ) )
					{
						list.add( colour );
					}					
				}
			}
		}
		return list;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for( ColourRange range : rangelist )
		{
			sb.append( range.toString() + "#" );
		}
		return sb.toString();
	}
	
	private boolean isInRange( Colour colour )
	{
		boolean answer = true;
		for( ColourRange range : rangelist )
		{
			ColourType type = range.getType();
			int value = colour.getTypeValue( type );			
			if( value < range.getMin() || value >= range.getMax() )
			{				
				answer = false;
				break;
			}			
		}
		return answer;
	}
}
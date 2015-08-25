package emr.colour.comparators;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import emr.colour.colourlist.Colour;
import emr.colour.colourlist.ColourType;

public class CustomComparator implements Comparator<Colour>
{
	private boolean ascending;
	private List<ColourType> terms;
	
	public CustomComparator( boolean ascending , String sortterms )
	{
		this.ascending = ascending;		
		String[] tempterms = sortterms.split( "," );
		terms = new ArrayList<ColourType>();
		for( int i = 0; i < tempterms.length; i++ )
		{
			terms.add( ColourType.valueOf( tempterms[ i ] ) );
		}
	}
	
	@Override
	public int compare( Colour first , Colour second )
	{
		int answer = 0;
		for( ColourType term : terms )
		{			
			answer = first.getTypeValue( term ) - second.getTypeValue( term );
			if( answer != 0 )
			{
				break;
			}
		}
		if( !ascending )
		{
			answer = -answer;
		}
		return answer;
	}
}
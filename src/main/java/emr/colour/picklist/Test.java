package emr.colour.picklist;

import java.util.Random;

public class Test
{
	public static void main( String[] args )
	{
		PickList test = PickListParser.parse( "picklists/" , args[ 0 ] );
		Random random = new Random( Long.parseLong( args[ 1 ] ) );
		while( test.hasEntry() )
		{
			System.out.println( test.pick( random , true ) );
		}
	}
}
package emr.colour.picklist;

import java.util.Scanner;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.io.IOException;

public class PickListParser
{
	public static PickList parse( String path , String filename )
	{
		PickList result = new PickList();
		Scanner scanner = null;
		try
		{
			scanner = new Scanner( Paths.get( path + filename ) , "UTF-8" );
		}
		catch( IOException ioe )
		{
			System.err.println( "Error reading file: " + filename );
			ioe.printStackTrace();
		}
		
		while( scanner.hasNextLine() )
		{
			String[] line = scanner.nextLine().split( "," );
			int amount = Integer.parseInt( line[ 0 ] );
			String valueString = line[ 1 ];
			if( valueString.startsWith( "*" ) )
			{
				PickList sublist = PickListParser.parse( path , valueString.substring( 1 ) );
				for( int i = 0; i < amount; i++ )
				{
					result.addEntry( new PickListEntryList( sublist ) );
				}
			}
			else
			{
				int value = Integer.parseInt( valueString );
				for( int i = 0; i < amount; i++ )
				{
					result.addEntry( new PickListEntryValue( value ) );
				}
			}
		}
		scanner.close();
		
		return result;
	}
}
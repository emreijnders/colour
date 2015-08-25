package emr.colour.colourlist;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.io.IOException;
import java.io.InputStream;

public class CCLFileParser
{
	private Scanner scanner;
	private CompoundColourList ccl;
	private ColourList currentlist;
	
	public CCLFileParser( String filename )
	{
		try
		{
			scanner = new Scanner( Paths.get( filename ) , "UTF-8" );
		}
		catch( IOException ioe )
		{
			System.err.println( "Error reading file: " + filename );
			ioe.printStackTrace();
		}
	}
	
	public CCLFileParser( InputStream stream )
	{
		scanner = new Scanner( stream , "UTF-8" );
	}
	
	public CompoundColourList parseFile()
	{		
		ccl = new CompoundColourList();
		currentlist = new ColourList();
		String currentid = "";		
		while( scanner.hasNextLine() )
		{
			String line = scanner.nextLine();
			if( line.startsWith( "@bits" ) && scanner.hasNextLine() )
			{
				parseBits();
			}
			else if( line.startsWith( "@order") && scanner.hasNextLine() )
			{
				parseOrder();
			}
			else if( line.startsWith( "%" ) )
			{
				finishList( currentid , currentlist );				
				currentid = parseId( line );
			}
			else if( line.startsWith( "@" ) && scanner.hasNextLine() )
			{				
				currentlist.addRange( parseRange( line ) );
			}
		}
		//add the last one
		finishList( currentid , currentlist );
		scanner.close();
		return ccl;
	}
	
	private void parseBits()
	{
		String nextline = scanner.nextLine();
		ccl.setBits( Integer.parseInt( nextline ) );
	}
	
	private void parseOrder()
	{
		String nextline = scanner.nextLine();
		String[] items = nextline.split( "," );
		List<String> order = new ArrayList<>();
		for( int i = 0; i < items.length; i++ )
		{
			order.add( items[ i ] );
		}
		ccl.setOrder( order );
	}
	
	private String parseId( String line )
	{
		return line.substring( 1 );
	}
	
	private void finishList( String id , ColourList list )
	{
		if( !id.equals( "" ) )
		{
			ccl.addColourList( id , list );
		}
		currentlist = new ColourList();
	}
	
	private ColourRange parseRange( String line )
	{
		ColourType type = ColourType.valueOf( line.substring( 1 ) );
		String nextline = scanner.nextLine();
		String[] items = nextline.split( "," );
		return new ColourRange( type , Integer.parseInt( items[ 0 ] ) , Integer.parseInt( items[ 1 ] ) );
	}
}
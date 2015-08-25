package emr.colour.colourlist;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.Writer;
import java.io.IOException;
import java.util.Map;

public class CCLFileWriter
{
	public static void writeFile( CompoundColourList ccl , String filename )
	{
		try( BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( filename ) , "UTF-8" ) ) )
		{
			//write bits
			out.write( "@bits" );
			out.newLine();
			out.write( "" + ccl.getBits() );
			out.newLine();
			out.newLine();
			
			//write order
			out.write( "@order" );
			out.newLine();
			StringBuilder sb = new StringBuilder();			
			for( String item : ccl.getOrder() )
			{
				sb.append( item + "," );
			}
			sb.deleteCharAt( sb.length() - 1 ); //remove the last comma
			out.write( sb.toString() );
			out.newLine();
			out.newLine();
			
			//write lists			
			//-write min/max for every type
			for( Map.Entry<String,ColourList> entry : ccl.getLists().entrySet() )
			{
				out.write( "%" + entry.getKey() );
				out.newLine();
				out.newLine();
				
				for( ColourRange range : entry.getValue().getRangeList() )
				{
					out.write( "@" + range.getType() );
					out.newLine();
					out.write( range.getMin() + "," + range.getMax() );
					out.newLine();
					out.newLine();					
				}
			}
			out.flush();
		}
		catch( IOException ioe )
		{
			System.err.println( "Problem with writing to " + filename );
			ioe.printStackTrace();
		}
	}
}
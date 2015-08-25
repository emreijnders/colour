package emr.colour.test;

import emr.colour.colourlist.*;

public class ColourTrial
{
	public static void main( String[] args )
	{
		CompoundColourList ccl = new CompoundColourList();
		ccl = new CCLFileParser( ccl.getClass().getResourceAsStream( "trial.ccl" ) ).parseFile();
		System.out.println( ccl.getCompoundColourList() );
		CCLFileWriter.writeFile( ccl , "output.ccl" );
	}
}
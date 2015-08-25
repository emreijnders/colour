package emr.colour;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.Writer;
import emr.colour.colourlist.CCLFileWriter;
import emr.colour.colourlist.CompoundColourList;
import java.nio.file.Paths;

public class ImageSaver
{
	public static void saveImage( BufferedImage image , ImageSettings settings , CompoundColourList ccl )
	{		
		String filename = generateFilename();
		String directory = "images" + File.separator;		
		File outputfile = new File( directory + filename + ".png" );
		try
		{
			ImageIO.write( image , "png" , outputfile );
			settings.getSettings().saveAsSettingsFile( directory + filename + ".settings" );
			CCLFileWriter.writeFile( ccl , directory + filename + ".ccl" );
		}
		catch( IOException ioe )
		{
			System.err.println( "Error: something went wrong saving the image" );
			ioe.printStackTrace();
		}
	}
	
	private static String generateFilename()
	{
		int number = 0;
		try
		{
			number = new Scanner( Paths.get( "filename.number" ) , "UTF-8" ).nextInt();
		}
		catch( IOException ioe )
		{
			System.err.println( "Error: missing filename.number" );
			ioe.printStackTrace();
			System.exit( 0 );
		}
		String filename = "Generated.Image." + number;
		number++;
		try( Writer out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( "filename.number" ) , "UTF-8" ) ) )
		{
			out.write( String.valueOf( number ) );
			out.flush();
		}
		catch( IOException ioe )
		{
			System.err.println( "Error: problem writing to filename.number" );
			ioe.printStackTrace();
			System.exit( 0 );
		}
		return filename;
	}
}
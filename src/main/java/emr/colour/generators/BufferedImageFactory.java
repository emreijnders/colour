package emr.colour.generators;

import java.awt.image.BufferedImage;
import java.awt.Color;
import emr.colour.ImageSettings;

public class BufferedImageFactory
{
	public static BufferedImage generateBufferedImage( ImageSettings settings )
	{
		int width = settings.<Long>getSetting( "image_width" ).intValue();
		int height = settings.<Long>getSetting( "image_height" ).intValue();
		Color background = Color.decode( settings.getSetting( "background_colour" ) );
		
		BufferedImage image = new BufferedImage( width , height , BufferedImage.TYPE_INT_RGB );
		
		for( int x = 0; x < width; x++ )
		{
			for( int y = 0; y < height; y++ )
			{
				image.setRGB( x , y , background.getRGB() );
			}
		}
		
		return image;
	}
}
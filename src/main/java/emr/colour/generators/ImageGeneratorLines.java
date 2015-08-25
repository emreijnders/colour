package emr.colour.generators;

import java.awt.image.BufferedImage;
import java.util.List;
import emr.colour.colourlist.Colour;
import emr.colour.ImageSettings;

public class ImageGeneratorLines implements ImageGenerator
{
	@Override
	public BufferedImage generateImage( BufferedImage image , List<Colour> colours , ImageSettings settings )
	{
		boolean horizontal = settings.getSetting( "horizontal" );
		if( horizontal )
		{			
			for( int y = 0; y < image.getHeight(); y++ )
			{
				if( colours.isEmpty() )
				{
					break;
				}
				for( int x = 0; x < image.getWidth(); x++ )
				{
					if( colours.isEmpty() )
					{
						break;
					}					
					image.setRGB( x , y , colours.remove(0).getIntValue() );
				}
			}
		}
		else
		{
			for( int x = 0; x < image.getWidth(); x++ )
			{
				if( colours.isEmpty() )
				{
					break;
				}
				for( int y = 0; y < image.getHeight(); y++ )
				{
					if( colours.isEmpty() )
					{
						break;
					}
					image.setRGB( x , y , colours.remove(0).getIntValue() );
				}
			}
		}
		return image;
	}
}
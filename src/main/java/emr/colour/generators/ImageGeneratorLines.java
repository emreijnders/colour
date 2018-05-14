package emr.colour.generators;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.LinkedList;
import emr.colour.colourlist.Colour;
import emr.colour.ImageSettings;

public class ImageGeneratorLines implements ImageGenerator
{
	private BufferedImage image;
	private ImageSettings settings;
	private List<Colour> colours;
	
	public ImageGeneratorLines( BufferedImage image , List<Colour> colours , ImageSettings settings )
	{
		this.image = image;
		this.settings = settings;
		this.colours = new LinkedList<Colour>( colours );
	}
	
	@Override
	public BufferedImage generateImage()
	{
		boolean horizontal = settings.getSetting( "horizontal" );
		if( horizontal )
		{			
			for( int y = 0; y < image.getHeight(); y++ )
			{
				for( int x = 0; x < image.getWidth(); x++ )
				{
					if( !colours.isEmpty() )
					{
						image.setRGB( x , y , colours.remove(0).getIntValue() );
					}
				}
			}
		}
		else
		{
			for( int x = 0; x < image.getWidth(); x++ )
			{
				for( int y = 0; y < image.getHeight(); y++ )
				{
					if( !colours.isEmpty() )
					{
						image.setRGB( x , y , colours.remove(0).getIntValue() );
					}
				}
			}
		}
		return image;
	}
}
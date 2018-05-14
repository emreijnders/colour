package emr.colour.generators;

import java.awt.image.BufferedImage;
import emr.colour.ImageSettings;
import emr.colour.colourlist.Colour;
import java.util.List;

public class GeneratorFactory
{
	private BufferedImage image;
	private List<Colour> colours;
	private ImageSettings settings;
	private GeneratorTypes type;
	
	public GeneratorFactory( GeneratorTypes type , ImageSettings settings )
	{
		this.type = type;
		this.settings = settings;
		image = BufferedImageFactory.generateBufferedImage( settings );
		colours = null;
	}
	
	public GeneratorFactory addBufferedImage( BufferedImage image )
	{
		this.image = image;
		return this;
	}
	
	public BufferedImage getImage()
	{
		return image;
	}
	
	public GeneratorFactory addColourList( List<Colour> colours )
	{
		this.colours = colours;
		return this;
	}
	
	public ImageGenerator getGenerator()
	{
		ImageGenerator generator;
		switch( type )
		{
			case LINES:
				generator = new ImageGeneratorLines( image , colours , settings );
				break;
			case CIRCLES:
				generator = new ImageGeneratorCircles( image , colours , settings );
				break;
			case CLOUD:
				generator = new ImageGeneratorCloud( image , colours , settings );
				break;
			case CORAL:
				generator = new ImageGeneratorCoral( image , colours , settings );
				break;
			case TREE:
				generator = new ImageGeneratorTree( image , colours , settings );
				break;
			case BLOCKS:
				generator = new ImageGeneratorBlocks( image , colours , settings );
				break;
			case STRIPES:
				generator = new ImageGeneratorStripes( image , colours , settings );
				break;
			default:
				generator = null;
		}
		return generator;
	}
}
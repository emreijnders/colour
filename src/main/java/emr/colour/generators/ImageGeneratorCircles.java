package emr.colour.generators;

import java.awt.image.BufferedImage;
import java.util.List;
import emr.colour.colourlist.Colour;
import emr.colour.ImageSettings;

public class ImageGeneratorCircles implements ImageGenerator
{
	private BufferedImage image;
	private ImageSettings settings;
	private List<Colour> colours;
	
	public ImageGeneratorCircles( BufferedImage image , List<Colour> colours , ImageSettings settings )
	{
		this.image = image;
		this.settings = settings;
		this.colours = colours;
	}
	
	@Override
	public BufferedImage generateImage()
	{
		return null;
	}
}
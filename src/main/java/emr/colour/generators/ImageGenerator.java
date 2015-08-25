package emr.colour.generators;

import java.awt.image.BufferedImage;
import java.util.List;
import emr.colour.colourlist.Colour;
import emr.colour.ImageSettings;

public interface ImageGenerator
{
	public BufferedImage generateImage( BufferedImage image , List<Colour> colours , ImageSettings settings );
	
}
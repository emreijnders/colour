package emr.colour.generators;

import emr.colour.colourlist.Colour;
import emr.colour.ImageSettings;
import emr.stuff.Bounds;
import emr.stuff.Location;
import emr.stuff.QuadTree;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class ImageGeneratorBlocks implements ImageGenerator
{
	@Override
	public BufferedImage generateImage( BufferedImage image , List<Colour> colours , ImageSettings settings )
	{
		//init
		long seed = settings.getSetting( "seed" );
		Random rand = new Random( seed );
		int number_of_blocks = settings.<Long>getSetting( "number_of_blocks" ).intValue();
		int max_size = settings.<Long>getSetting( "max_size" ).intValue();
		double current_size = max_size;
		double size_factor = settings.getSetting( "size_factor" );
		int colour_index = 0;
		Graphics2D g2 = image.createGraphics();
		QuadTree<Bounds> tree = new QuadTree<>( new Bounds( new Location( 0 , 0 ) , image.getWidth() , image.getHeight() ) );
		for( int i = 0; i < number_of_blocks; i++ )
		{
			Bounds newbounds;
			boolean intersects = false;
			do
			{
				intersects = false;
				int x = rand.nextInt( image.getWidth() );
				int y = rand.nextInt( image.getHeight() );
				newbounds = new Bounds( new Location( x , y ) , current_size , current_size );				
				for( Bounds bounds : tree.retrieve( new ArrayList<Bounds>() , newbounds) )
				{
					if( bounds.intersects( newbounds ) )
					{
						intersects = true;
						break;
					}
				}
			}
			while( intersects );
			tree.insert( newbounds );
			g2.setColor( new Color( colours.get( colour_index ).getIntValue() ) );
			colour_index = ( colour_index + 1 ) % colours.size();
			g2.fill( new Rectangle2D.Double( newbounds.getTopLeft().X , newbounds.getTopLeft().Y , current_size , current_size ) );
			current_size = current_size * size_factor;
			if( current_size < 1 )
			{
				current_size = 1;
			}
		}
		
		return image;
	}
}
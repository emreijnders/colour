package emr.colour.generators;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import emr.colour.colourlist.Colour;
import emr.colour.ImageSettings;
import emr.stuff.Location;
import emr.stuff.Direction;

public class ImageGeneratorStripes implements ImageGenerator
{
	private BufferedImage image;
	private ImageSettings settings;
	private List<Colour> colours;
	private Random rand;
	
	public ImageGeneratorStripes( BufferedImage image , List<Colour> colours , ImageSettings settings )
	{
		this.image = image;
		this.settings = settings;
		this.colours = colours;
		long seed = settings.getSetting( "seed" );
		rand = new Random( seed );		
	}
	
	@Override
	public BufferedImage generateImage()
	{
		for( int y = 0; y < image.getHeight(); y++ )
		{
			for( int x = 0; x < image.getWidth(); x++ )
			{
				Location start = new Location( x , y );
				if( isBackgroundColour( start ) && !colours.isEmpty() )
				{
					//roll length
					int length = rand.nextInt( 100 );
					//roll angle
					double angle = rand.nextDouble() * 2 * Math.PI;
					//draw line
					drawLine( start , length , angle );
				}
			}
		}
		
		return image;
	}
	
	private List<Location> getLineList( Location start , Location end )
	{
		List<Location> points = new ArrayList<>();
		int xdif = Math.abs( start.getX() - end.getX() );
		int ydif = Math.abs( start.getY() - end.getY() );
		Direction dir = start.getRelativeDirection( end );
		points.add( start );
		Location now = start;
		double ratio = 0.0;
		if( xdif != 0 && ydif != 0 )
		{
			ratio = (double) Math.min( xdif , ydif ) / (double) Math.max( xdif , ydif );
		}
		double error = 0.0;
		/**int linemax = maxsize;
		if( randomsize )
		{
			linemax = rand.nextInt(maxsize-1)+1;
		}
		int size = 0;
		**/
		while( !now.equals( end ) ) //&& size <= linemax)
		{
			int x = 0;
			int y = 0;
			x = now.getX();
			y = now.getY();
			error += ratio;
			if( xdif >= ydif )
			{
				x += dir.X;					
				if( error >= 0.5 )
				{
					y += dir.Y;
					error -= 1.0;
				}					
			}
			else
			{
				y += dir.Y;
				if( error >= 0.5 )
				{
					x += dir.X;
					error -= 1.0;
				}					
			}
			Location next = new Location( x , y );
			points.add( next );
			now = next;
			//size++;
		}
		return points;
	}
	
	private void drawLine( Location start , int length , double angle )
	{
		//System.out.println( "s:" + start + " l:" + length + " a:" + angle );
		
		int endx = (int) ( start.getX() + ( Math.cos( angle ) * length ) );
		int endy = (int) ( start.getY() + ( Math.sin( angle ) * length ) );
		Location end = new Location( endx , endy );
		
		//System.out.println( "e:" + end );
		
		int colour = colours.remove( 0 ).getIntValue();
		for( Location loc : getLineList( start , end ) )
		{
			if( isInRange( loc ) && isBackgroundColour( loc ) && !colours.isEmpty() )
			{
				image.setRGB( loc.getX() , loc.getY() , colour );
			}
		}
	}
	
	private boolean isInRange( Location loc )
	{
		return loc.getX() >= 0 && loc.getY() >= 0 && loc.getX() < image.getWidth() && loc.getY() < image.getHeight();
	}
	
	private boolean isBackgroundColour( Location loc )
	{
		return image.getRGB( loc.getX() , loc.getY() ) == -16777216;
	}
}
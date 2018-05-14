package emr.colour.generators;

import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import emr.colour.colourlist.Colour;
import emr.colour.ImageSettings;
import emr.stuff.LocationDouble;
import emr.stuff.Direction;
import emr.stuff.QuadTree;
import emr.stuff.Bounded;
import emr.stuff.Bounds;

public class ImageGeneratorCoral implements ImageGenerator
{
	private QuadTree<Mover> movers;
	private Set<LocationDouble> stuck;
	private Random rand;
	private int max_speed;
	private int iteration;
	private boolean starting_line;
	private BufferedImage image;
	private ImageSettings settings;
	private List<Colour> colours;
	
	public ImageGeneratorCoral( BufferedImage image , List<Colour> colours , ImageSettings settings )
	{
		this.image = image;
		this.settings = settings;
		this.colours = colours;
		long seed = settings.getSetting( "seed" );
		rand = new Random( seed );
		movers = new QuadTree<>( new Bounds( new LocationDouble( 0 , 0 ) , image.getWidth() , image.getHeight() ) );
		stuck = new HashSet<>();
		max_speed = settings.<Long>getSetting( "max_speed" ).intValue();
		starting_line = settings.getSetting( "starting_line" );		
	}
	
	
	@Override
	public BufferedImage generateImage()
	{		
		//init
		if( starting_line )
		{
			for( int x = 0; x < image.getWidth(); x++ )
			{
				LocationDouble loc = new LocationDouble( x , image.getHeight() - 1 );
				stuck.add( loc );
				image.setRGB( (int) loc.getX() , (int) loc.getY() , colours.get( 0 ).getIntValue() );
			}
		}
		else
		{
			//pick the starting locations		
			int amount_start = settings.<Long>getSetting( "amount_start" ).intValue();
			for( int x = 0; x < amount_start && !colours.isEmpty() ; x++ )
			{
				LocationDouble start;
				do
				{
					start = new LocationDouble( rand.nextInt( image.getWidth() ) , rand.nextInt( image.getHeight() ) );
				}
				while( stuck.contains( start ) );
				stuck.add( start );
				image.setRGB( (int) start.getX() , (int) start.getY() , colours.get( 0 ).getIntValue() );
			}		
		}
		
		//distribute other colours randomly
		int amount_total = settings.<Long>getSetting( "amount_total" ).intValue();
		for( int x = 0; x < amount_total && !colours.isEmpty(); x++ )
		{
			createNextMover( colours.get(0) );
		}		
		//move them around
		int max_iterations = settings.<Long>getSetting( "max_iterations" ).intValue();
		iteration = 0;
		while( iteration < max_iterations )
		{
			QuadTree<Mover> nextmovers = new QuadTree<>( new Bounds( new LocationDouble( 0 , 0 ) , image.getWidth() , image.getHeight() ) );
			for( Mover mover : movers.getAllLeaves( new ArrayList<>() ) )
			{
				moveMover( mover , mover.getNextLocation() , nextmovers );
			}
			movers = nextmovers;
			iteration++;
		}
		
		//return the image
		return image;
	}
	
	private LocationDouble resolveCollision( Mover mover , LocationDouble next )
	{
		LocationDouble answer = next;
		if( answer.getX() < 0 )
		{
			answer = new LocationDouble( Math.abs( answer.getX() ) , answer.getY() );
			mover.switchX();
		}
		else if( answer.getX() >= image.getWidth() )
		{
			answer = new LocationDouble( image.getWidth() - ( ( answer.getX() - image.getWidth() ) + 1 ) , answer.getY() );
			mover.switchX();
		}
		
		if( answer.getY() < 0 )
		{
			answer = new LocationDouble( answer.getX() , Math.abs( answer.getY() ) );
			mover.switchY();
		}
		else if( answer.getY() >= image.getHeight() )
		{
			answer = new LocationDouble( answer.getX() , image.getHeight() - ( ( answer.getY() - image.getHeight() ) + 1 ) );
			mover.switchY();
		}
		
		return answer;
	}
	
	private void resolveCollision( Mover mover , Mover other )
	{
		//switch their speeds
		int xtemp = mover.xspeed;
		int ytemp = mover.yspeed;
		mover.xspeed = other.xspeed;
		mover.yspeed = other.yspeed;
		other.xspeed = xtemp;
		other.yspeed = ytemp;		
	}
	
	private List<LocationDouble> getNeighbours( LocationDouble center , int distance )
	{
		List<LocationDouble> list = new ArrayList<>();
		if( distance > 0 )
		{
			for( int y = (int) center.getY() - distance; y <= center.getY() + distance; y++ )
			{
				for( int x = (int) center.getX() - distance; x <= center.getX() + distance; x++ )
				{
					if( x == center.getX() && y == center.getY() ) continue;
					list.add( new LocationDouble( x , y ) );
				}
			}
		}
		return list;
	}
	
	private void moveMover( Mover mover , LocationDouble end , QuadTree<Mover> nextmovers )
	{
		LocationDouble start = mover.location;
		int xdif = (int) Math.abs( start.getX() - end.getX() );
		int ydif = (int) Math.abs( start.getY() - end.getY() );
		Direction dir = start.getRelativeDirection( end );
		LocationDouble previous = start;
		LocationDouble now = start;
		double ratio = 0.0;
		if( xdif != 0 && ydif != 0 )
		{
			ratio = (double) Math.min( xdif , ydif ) / (double) Math.max( xdif , ydif );
		}
		double error = 0.0;
		boolean done = false;
		boolean isstuck = false;
		while( !now.equals( end ) && !done )
		{
			done = false;
			//check if we're stuck yet
			for( LocationDouble possible : getNeighbours( now , 1 ) )
			{				
				if( stuck.contains( possible ) )
				{
					end = now;
					stuck.add( end );
					done = true;
					isstuck = true;
					break;
				}
			}
			if( !done )
			{
				//figure out next location
				int x = (int) now.getX();
				int y = (int) now.getY();
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
				LocationDouble next = new LocationDouble( x , y );
				//check if the next location is out of bounds
				if( isOutside( next ) )
				{
					moveMover( mover , resolveCollision( mover , end ) , nextmovers );
					return;
				}
				previous = now;
				now = next;
			}			
		}
		//check we don't end up on top of another mover
		Mover fake = Mover.createFakeMover( end );
		List<Mover> list = nextmovers.retrieve( fake );
		int index = list.indexOf( fake );
		if( index > -1 )
		{
			end = previous;
			resolveCollision( mover , list.get( index ) );
		}
		//set the checked location
		mover.setLocation( end , colours.get( iteration % colours.size() ) );
		if( !isstuck )
		{
			nextmovers.insert( mover );
		}
	}
	
	private boolean isNeighbour( LocationDouble t , LocationDouble o )
	{
		return !t.equals( o ) && Math.abs( t.getX() - o.getX() ) < 2 && Math.abs( t.getY() - o.getY() ) < 2;
	}
	
	private boolean isOutside( LocationDouble test )
	{
		boolean answer = false;
		if( test.getX() < 0 || test.getX() >= image.getWidth() || test.getY() < 0 || test.getY() >= image.getHeight() )
		{
			answer = true;
		}
		return answer;
	}
	
	private void createNextMover( Colour col )
	{
		Mover fake;
		LocationDouble next;
		do
		{
			next = new LocationDouble( rand.nextInt( image.getWidth() ) , rand.nextInt( image.getHeight() ) );
			fake = Mover.createFakeMover( next );
		}
		while( movers.retrieve( fake ).contains( fake ) );
		int xs;
		int ys;
		if( !starting_line )
		{
			do
			{
				xs = rand.nextInt( max_speed );
				if( rand.nextBoolean() ) xs = -xs;
				ys = rand.nextInt( max_speed );
				if( rand.nextBoolean() ) ys = -ys;
			}
			while( xs == 0 && ys == 0);
		}
		else
		{
			xs = rand.nextInt( max_speed );
			if( rand.nextBoolean() ) xs = -xs;
			ys = rand.nextInt( max_speed - 1 ) + 1;
			if( rand.nextBoolean() ) ys = -ys;
		}		
		movers.insert( new Mover( next , col , xs , ys , image ) );
	}
}

class Mover implements Bounded
{
	public LocationDouble location;
	public int xspeed, yspeed;
	private BufferedImage image;
	
	public Mover( LocationDouble loc , Colour col , int xs , int ys , BufferedImage image )
	{
		location = loc;		
		xspeed = xs;
		yspeed = ys;
		this.image = image;
		image.setRGB( (int) location.getX() , (int) location.getY() , col.getIntValue() );
	}
	
	private Mover( LocationDouble loc )
	{
		location = loc;
	}
	
	public static Mover createFakeMover( LocationDouble loc )
	{
		return new Mover( loc );
	}
	
	public LocationDouble getNextLocation()
	{
		//get next location
		return new LocationDouble( location.getX() + xspeed , location.getY() + yspeed );
	}
	
	public void setLocation( LocationDouble next , Colour colour )
	{		
		//remove from old location
		image.setRGB( (int) location.getX() , (int) location.getY() , -16777216 );
		
		//add to new location
		image.setRGB( (int) next.getX() , (int) next.getY() , colour.getIntValue() );
		location = next;
	}
	
	@Override
	public LocationDouble getTopLeft()
	{
		return location;
	}
	
	@Override
	public double getWidth()
	{
		return 1;
	}
	
	@Override
	public double getHeight()
	{
		return 1;
	}
	
	/*
	@Override
	public boolean intersects( Bounded other )
	{
		boolean answer = false;
		if( location.getX() >= other.getTopLeft().getX() 
			&& location.getX() < other.getTopLeft().getX() + other.getWidth() 
			&& location.getY() >= other.getTopLeft().getY()
			&& location.getY() < other.getTopLeft().getY() + other.getHeight() )
		{
			answer = true;
		}
		return answer;
	}
	*/
	
	@Override
	public Rectangle2D getBoundingRectangle()
	{
		return new Rectangle2D.Double( location.getX() , location.getY() , 1 , 1 );
	}
	
	public void switchX()
	{
		xspeed = -xspeed;
	}
	
	public void switchY()
	{
		yspeed = -yspeed;
	}
	
	@Override
	public boolean equals( Object o )
	{
		boolean result = false;
		if( o != null )
		{
			if( o == this )
			{
				result = true;
			}
			else if( o instanceof Mover )
			{
				Mover other = (Mover) o;
				if( other.location.equals( location ) )
				{
					result = true;
				}
			}
		}
		return result;
	}
	
	@Override
	public int hashCode()
	{
		return location.hashCode();
	}
	
	@Override
	public String toString()
	{
		return "Mover at: " + location + " with speed <" + xspeed + "," + yspeed + ">";
	}
}

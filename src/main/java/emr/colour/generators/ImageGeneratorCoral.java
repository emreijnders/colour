package emr.colour.generators;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import emr.colour.colourlist.Colour;
import emr.colour.ImageSettings;
import emr.stuff.Location;
import emr.stuff.Direction;
import emr.stuff.QuadTree;
import emr.stuff.Bounded;
import emr.stuff.Bounds;

public class ImageGeneratorCoral implements ImageGenerator
{
	private QuadTree<Mover> movers;
	private Set<Location> stuck;
	private Random rand;
	private BufferedImage image;
	private int max_speed;
	private List<Colour> colours;
	private int iteration;
	private boolean starting_line;
	
	@Override
	public BufferedImage generateImage( BufferedImage image , List<Colour> colours , ImageSettings settings )
	{		
		//init
		long seed = settings.getSetting( "seed" );
		rand = new Random( seed );
		this.image = image;
		this.colours = colours;
		movers = new QuadTree<>( new Bounds( new Location( 0 , 0 ) , image.getWidth() , image.getHeight() ) );
		stuck = new HashSet<>();
		max_speed = settings.<Long>getSetting( "max_speed" ).intValue();
		starting_line = settings.getSetting( "starting_line" );
		if( starting_line )
		{
			for( int x = 0; x < image.getWidth(); x++ )
			{
				Location loc = new Location( x , image.getHeight() - 1 );
				stuck.add( loc );
				image.setRGB( loc.X , loc.Y , colours.get( 0 ).getIntValue() );
			}
		}
		else
		{
			//pick the starting locations		
			int amount_start = settings.<Long>getSetting( "amount_start" ).intValue();
			for( int x = 0; x < amount_start && !colours.isEmpty() ; x++ )
			{
				Location start;
				do
				{
					start = new Location( rand.nextInt( image.getWidth() ) , rand.nextInt( image.getHeight() ) );
				}
				while( stuck.contains( start ) );
				stuck.add( start );
				image.setRGB( start.X , start.Y , colours.get( 0 ).getIntValue() );
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
			QuadTree<Mover> nextmovers = new QuadTree<>( new Bounds( new Location( 0 , 0 ) , image.getWidth() , image.getHeight() ) );
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
	
	private Location resolveCollision( Mover mover , Location next )
	{
		Location answer = next;
		if( answer.X < 0 )
		{
			answer = new Location( Math.abs( answer.X ) , answer.Y );
			mover.switchX();
		}
		else if( answer.X >= image.getWidth() )
		{
			answer = new Location( image.getWidth() - ( ( answer.X - image.getWidth() ) + 1 ) , answer.Y );
			mover.switchX();
		}
		
		if( answer.Y < 0 )
		{
			answer = new Location( answer.X , Math.abs( answer.Y ) );
			mover.switchY();
		}
		else if( answer.Y >= image.getHeight() )
		{
			answer = new Location( answer.X , image.getHeight() - ( ( answer.Y - image.getHeight() ) + 1 ) );
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
	
	private void moveMover( Mover mover , Location end , QuadTree<Mover> nextmovers )
	{
		Location start = mover.location;
		int xdif = Math.abs(start.X - end.X);
		int ydif = Math.abs(start.Y - end.Y);
		Direction dir = start.getRelativeDirection(end);
		Location previous = start;
		Location now = start;
		double ratio = 0.0;
		if(xdif != 0 && ydif != 0)
		{
			ratio = (double)Math.min(xdif,ydif) / (double)Math.max(xdif,ydif);
		}
		double error = 0.0;
		boolean done = false;
		boolean isstuck = false;
		while( !now.equals( end ) && !done )
		{
			done = false;
			//check if we're stuck yet
			for( Location possible : now.getNeighbours() )
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
				int x = 0;
				int y = 0;
				x = now.X;
				y = now.Y;
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
		List<Mover> list = nextmovers.retrieve( new ArrayList<Mover>() , fake );
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
	
	private boolean isNeighbour( Location t , Location o )
	{
		return !t.equals(o) && Math.abs( t.X - o.X ) < 2 && Math.abs( t.Y - o.Y ) < 2;
	}
	
	private boolean isOutside( Location test )
	{
		boolean answer = false;
		if( test.X < 0 || test.X >= image.getWidth() || test.Y < 0 || test.Y >= image.getHeight() )
		{
			answer = true;
		}
		return answer;
	}
	
	private void createNextMover( Colour col )
	{
		Mover fake;
		Location next;
		do
		{
			next = new Location( rand.nextInt( image.getWidth() ) , rand.nextInt( image.getHeight() ) );
			fake = Mover.createFakeMover( next );
		}
		while( movers.retrieve( new ArrayList<Mover>() , fake ).contains( fake ) );
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
	public Location location;
	public int xspeed, yspeed;
	private BufferedImage image;
	
	public Mover( Location loc , Colour col , int xs , int ys , BufferedImage image )
	{
		location = loc;		
		xspeed = xs;
		yspeed = ys;
		this.image = image;
		image.setRGB( location.X , location.Y , col.getIntValue() );
	}
	
	private Mover( Location loc )
	{
		location = loc;
	}
	
	public static Mover createFakeMover( Location loc )
	{
		return new Mover( loc );
	}
	
	public Location getNextLocation()
	{
		//get next location
		return new Location( location.X + xspeed , location.Y + yspeed );
	}
	
	public void setLocation( Location next , Colour colour )
	{		
		//remove from old location
		image.setRGB( location.X , location.Y , -16777216 );
		
		//add to new location
		image.setRGB( next.X , next.Y , colour.getIntValue() );
		location = next;
	}
	
	public Location getTopLeft()
	{
		return location;
	}
	
	public int getWidth()
	{
		return 1;
	}
	
	public int getHeight()
	{
		return 1;
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

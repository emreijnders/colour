package emr.colour.generators;

import emr.colour.colourlist.Colour;
import emr.colour.ImageSettings;
import emr.stuff.Bounds;
import emr.stuff.Bounded;
import emr.stuff.Location;
import emr.stuff.Direction;
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
	private BufferedImage image;
	private int max_size;
	private Bounded last_intersect;
	private QuadTree<Bounded> tree;
	private Graphics2D g2;
	private Random rand;
	private Direction current_direction;
	private int steps , max_steps;
	private boolean turn;
	private int current_size;
	private int colour_index;
	private List<Colour> colours;
	
	@Override
	public BufferedImage generateImage( BufferedImage image , List<Colour> colours , ImageSettings settings )
	{
		//init
		this.image = image;
		this.colours = colours;
		long seed = settings.getSetting( "seed" );
		rand = new Random( seed );
		int number_of_blocks = settings.<Long>getSetting( "number_of_blocks" ).intValue();
		max_size = settings.<Long>getSetting( "max_size" ).intValue();
		current_size = max_size;
		colour_index = 0;
		g2 = image.createGraphics();
		tree = new QuadTree<>( new Bounds( new Location( 0 , 0 ) , image.getWidth() , image.getHeight() ) );
		
		int counter = 0;
		while( counter < number_of_blocks && current_size >= 1 )
		{
			if( placeBlock() )
			{
				counter++;
			}
			else
			{
				current_size--;
			}
		}
		System.out.println("Image done");
		g2.dispose();
		return image;
	}
	
	private boolean placeBlock()
	{
		current_direction = Direction.N;
		steps = 0;
		max_steps = 1;
		turn = false;
		
		boolean placed = true;
		int w2 = (int) ( image.getWidth() / 2.0 );
		int rl = w2 - max_size;
		int rr = w2 + max_size;
		int sx = rand.nextInt( rr - rl ) + rl;
		int h2 = (int) ( image.getHeight() / 2.0 );
		int rt = h2 - max_size;
		int rb = h2 + max_size;
		int sy = rand.nextInt( rb - rt ) + rt;
		Location start = new Location( sx , sy );
		Bounded newblock = getNewBounds( start );
		
		while( !canPlace( newblock ) )
		{
			Location next = getNext( newblock.getTopLeft() );
			if( next.equals( newblock.getTopLeft() ) )
			{
				placed = false;
				break;
			}
			newblock = getNewBounds( next );
		}
		
		if( placed )
		{
			drawBlock( newblock );
			tree.insert( newblock );			
		}
		last_intersect = null;
		
		return placed;
	}
	
	private Bounded getNewBounds( Location loc )
	{
		double height = current_size / 2.0;
		if( height < 1 ) height = 1;
		return new Bounds( loc , current_size , height );
	}
	
	private void drawBlock( Bounded block )
	{
		g2.setColor( new Color( colours.get( colour_index ).getIntValue() ) );
		colour_index = ( colour_index + 1 ) % colours.size();
		g2.fill( new Rectangle2D.Double( block.getTopLeft().X , block.getTopLeft().Y , block.getWidth() , block.getHeight() ) );
	}
	
	private boolean canPlace( Bounded block )
	{
		boolean answer = true;
		if( last_intersect != null )
		{
			if( last_intersect.intersects( block ) )
			{
				answer = false;
			}
		}
		for( Bounded bounds : tree.retrieve( block ) )
		{
			if( bounds.intersects( block ) )
			{
				answer = false;
				break;
			}
		}
		return answer;
	}
	
	private Location getNext( Location loc )
	{
		Location answer = loc;
		do
		{
			if( max_steps > Math.max( image.getWidth() , image.getHeight() ) )
			{
				answer = loc;
				break;
			}
			answer = answer.getNewLocation( current_direction );
			steps++;
			if( steps >= max_steps )
			{
				steps = 0;
				if( turn )
				{
					max_steps++;
				}
				turn = !turn;
				current_direction = current_direction.getNextClockwiseDirection().getNextClockwiseDirection();			
			}
		}
		while( isOutside( answer ) );
		return answer;
	}
	
	private boolean isOutside( Location loc )
	{
		boolean answer = false;
		if( loc.X < 0 
			|| loc.X >= ( image.getWidth() - current_size ) 
			|| loc.Y < 0 
			|| loc.Y >= ( image.getHeight() - current_size ) )
		{
			answer = true;
		}
		return answer;
	}
}
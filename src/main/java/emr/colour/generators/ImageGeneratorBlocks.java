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
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class ImageGeneratorBlocks implements ImageGenerator
{
	private BufferedImage image;
	private int current_size;
	private Bounds last_intersect;
	private QuadTree<Bounds> tree;
	private Graphics2D g2;
	private Random rand;
	private Direction current_direction;
	private int steps , max_steps;
	private boolean turn;
	private int colour_index;
	private List<Colour> colours;
	private Location start;
	
	@Override
	public BufferedImage generateImage( BufferedImage image , List<Colour> colours , ImageSettings settings )
	{
		//init
		System.out.println("Image starting");
		this.image = image;
		this.colours = colours;
		long seed = settings.getSetting( "seed" );
		rand = new Random( seed );
		int number_of_blocks = settings.<Long>getSetting( "number_of_blocks" ).intValue();
		int max_size = settings.<Long>getSetting( "max_size" ).intValue();
		current_size = max_size;
		colour_index = 0;
		g2 = image.createGraphics();
		tree = new QuadTree<>( new Bounds( new Location( 0 , 0 ) , image.getWidth() , image.getHeight() ) );
		
		//generate blocks
		SortedSet<Block> blocklist = new TreeSet<>( new BlockComparator( false ) ); //we want to go from large to small
		for( int i = 0; i < number_of_blocks && current_size > 0; i++ )
		{
			blocklist.add( createRandomBlock() );
		}
		
		//place blocks
		int x = (int) ( image.getWidth() / 2.0 ) + ( rand.nextInt( max_size * 2 ) - max_size );
		int y = (int) ( image.getHeight() / 2.0 ) + ( rand.nextInt( max_size * 2 ) - max_size );
		start = new Location( x , y );		
		for( Block block : blocklist )
		{
			if( !placeBlock( block ) )
			{
				placeBlock( new Block( block.height , block.width ) );
			}
		}
		
		System.out.println("Image done");
		g2.dispose();
		return image;
	}
	
	private boolean placeBlock( Block block )
	{
		current_direction = Direction.N;
		steps = 0;
		max_steps = 1;
		turn = false;
		
		boolean placed = true;
		/*
		int w2 = (int) ( image.getWidth() / 2.0 );
		int rl = w2 - max_size;
		int rr = w2 + max_size;
		int sx = rand.nextInt( rr - rl ) + rl;
		int h2 = (int) ( image.getHeight() / 2.0 );
		int rt = h2 - max_size;
		int rb = h2 + max_size;
		int sy = rand.nextInt( rb - rt ) + rt;		
		Location start = new Location( sx , sy );		
		*/
		Bounds newblock = getNewBounds( start , block );
		
		while( !canPlace( newblock ) )
		{
			Location next = getNext( newblock.getTopLeft() , block );
			if( next.equals( newblock.getTopLeft() ) )
			{
				placed = false;
				break;
			}
			newblock = getNewBounds( next , block );
		}
		
		if( placed )
		{
			drawBlock( newblock );
			tree.insert( newblock );			
		}
		last_intersect = null;
		
		return placed;
	}
	
	private Bounds getNewBounds( Location loc , Block block )
	{
		return new Bounds( loc , block.width , block.height );
	}
	
	private void drawBlock( Bounds block )
	{
		g2.setColor( new Color( colours.get( colour_index ).getIntValue() ) );
		colour_index = ( colour_index + 1 ) % colours.size();
		g2.fill( block );
	}
	
	private boolean canPlace( Bounds block )
	{
		boolean answer = true;
		if( isOutside( block.getTopLeft() , new Block( (int) block.getWidth() , (int) block.getHeight() ) ) )
		{
			answer = false;
		}
		else
		{
			if( last_intersect != null && last_intersect.intersects( (Bounded) block ) )
			{
				answer = false;
			}
			else
			{
				for( Bounds bounds : tree.retrieve( block ) )
				{
					if( bounds.intersects( (Bounded) block ) )
					{
						answer = false;
						break;
					}
				}
			}
		}
		return answer;
	}
	
	private Location getNext( Location loc , Block block )
	{
		Location answer = loc;
		do
		{
			if( max_steps > Math.max( image.getWidth() , image.getHeight() ) * 2 )
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
		while( isOutside( answer , block ) );
		return answer;
	}
	
	private boolean isOutside( Location loc , Block block )
	{
		boolean answer = false;
		if( loc.X < 0 
			|| loc.X >= ( image.getWidth() - block.width ) 
			|| loc.Y < 0 
			|| loc.Y >= ( image.getHeight() - block.height ) )
		{
			answer = true;
		}
		return answer;
	}
	
	private Block createRandomBlock()
	{
		int w = rand.nextInt( current_size ) + 1;
		int h = rand.nextInt( current_size ) + 1;
		if( w > current_size * 0.9 || h > current_size * 0.9 )
		{
			current_size--;
		}
		return new Block( w , h );
	}
}

class Block
{
	public final int width , height;
	
	public Block( int w , int h )
	{
		width = w;
		height = h;
	}
	
	@Override
	public String toString()
	{
		return "[" + width + "," + height + "]";
	}
}

class BlockComparator implements Comparator<Block>
{
	private final boolean ascending;
	
	public BlockComparator( boolean ascending )
	{
		this.ascending = ascending;
	}
	
	@Override
	public int compare( Block first , Block second )
	{
		int volume1 = first.width * first.height;
		int volume2 = second.width * second.height;
		int answer = volume1 - volume2;
		if( !ascending )
		{
			answer = -answer;
		}
		return answer;
	}
}
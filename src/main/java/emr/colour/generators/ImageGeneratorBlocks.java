package emr.colour.generators;

import emr.colour.colourlist.Colour;
import emr.colour.ImageSettings;
import emr.stuff.Bounds;
import emr.stuff.Bounded;
import emr.stuff.LocationDouble;
import emr.stuff.Direction;
import emr.stuff.QuadTree;
import emr.stuff.RotatableRectangle;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
//import java.util.SortedSet;
//import java.util.TreeSet;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class ImageGeneratorBlocks implements ImageGenerator
{
	private int current_width;
	private int current_height;
	private int max_size;
	private RotatableRectangle last_intersect;
	private QuadTree<RotatableRectangle> tree;
	private Graphics2D g2;
	private Random rand;
	private Direction current_direction;
	private int steps , max_steps;
	private boolean turn;
	private int colour_index;
	private LocationDouble start;
	private double factor;
	private double divisor;
	private BufferedImage image;
	private ImageSettings settings;
	private List<Colour> colours;
	
	public ImageGeneratorBlocks( BufferedImage image , List<Colour> colours , ImageSettings settings )
	{
		this.image = image;
		this.settings = settings;
		this.colours = colours;
		this.image = image;
		this.colours = colours;
		long seed = settings.getSetting( "seed" );
		rand = new Random( seed );
		int max_size = settings.<Long>getSetting( "max_size" ).intValue();		
		current_width = max_size;
		current_height = max_size;
		colour_index = 0;
		g2 = image.createGraphics();
		tree = new QuadTree<>( new Bounds( new LocationDouble( 0 , 0 ) , image.getWidth() , image.getHeight() ) );
		divisor = settings.getSetting( "divisor" );
		factor = Math.PI / divisor;		
	}
	
	
	@Override
	public BufferedImage generateImage()
	{
		//init
		System.out.println("Image starting");
		int number_of_blocks = settings.<Long>getSetting( "number_of_blocks" ).intValue();
		boolean shuffle_blocks = settings.getSetting( "shuffle_blocks" );
		boolean sort_blocks = settings.getSetting( "sort_blocks" );
		boolean blocks_ascending = settings.getSetting( "blocks_ascending" );
		
		//generate blocks
		List<RotatableRectangle> blocklist = new ArrayList<>();
		for( int i = 0; i < number_of_blocks; i++ )
		{
			blocklist.add( createRandomBlock() );
		}
		if( shuffle_blocks )
		{
			Collections.shuffle( blocklist , rand );
		}
		if( sort_blocks )
		{
			Collections.sort( blocklist , new BlockComparator( blocks_ascending ) );
		}
		
		//place blocks
		int index = 1;
		int miss_counter = 0;
		for( RotatableRectangle block : blocklist )
		{
			System.out.println( "placing block " + index + " of " + blocklist.size() );
			index++;
			double x = ( image.getWidth() / 2.0 ) + ( rand.nextInt( max_size * 2 ) - max_size );
			double y = ( image.getHeight() / 2.0 ) + ( rand.nextInt( max_size * 2 ) - max_size );
			start = new LocationDouble( x , y );		
		
			int counter = 0;
			boolean placed = false;
			while( !placed && counter < divisor )
			{				
				if( placeBlock( block ) )
				{
					placed = true;
					System.out.println( "block " + index + " placed" );
				}
				else
				{
					block.setAngle( block.getAngle() + factor );				
					counter++;
					System.out.println( "counter: " + counter );
				}
			}
			if( !placed )
			{
				miss_counter++;
				System.out.println( "block " + index + " not placed" );
			}
			else
			{
				miss_counter = 0;
			}
			if( miss_counter > 9 )
			{
				break;
			}
		}
		
		System.out.println("Image done");
		g2.dispose();
		return image;
	}
	
	private boolean placeBlock( RotatableRectangle block )
	{
		current_direction = Direction.N;
		steps = 0;
		max_steps = 1;
		turn = false;
		
		boolean placed = true;
		
		block.setLocationDouble( start );
		while( !canPlace( block ) )
		{
			if( !getNext( block ) )
			{
				placed = false;
				break;
			}
		}
		
		if( placed )
		{
			drawBlock( block );
			tree.insert( block );
		}
		last_intersect = null;
		
		return placed;
	}
	
	private void drawBlock( RotatableRectangle block )
	{
		g2.setColor( new Color( colours.get( colour_index ).getIntValue() ) );
		colour_index = ( colour_index + 1 ) % colours.size();
		g2.fill( block.getPath() );
	}
	
	private boolean canPlace( RotatableRectangle block )
	{
		boolean answer = true;
		if( isOutside( block ) )
		{
			answer = false;
		}
		else
		{
			if( last_intersect != null && last_intersect.intersects( block ) )
			{
				answer = false;
			}
			else
			{
				Rectangle2D br = block.getBoundingRectangle();
				Bounds bounds = new Bounds( new LocationDouble( br.getX() , br.getY() ) , br.getWidth() , br.getHeight() );
				for( RotatableRectangle rect : tree.retrieve( bounds ) )
				{
					if( rect.intersects( block ) )
					{
						last_intersect = rect;
						answer = false;
						break;
					}
				}
			}
		}
		return answer;
	}
	
	private boolean getNext( RotatableRectangle block )
	{
		//System.out.println( "getNext start" );
		LocationDouble answer = block.getCenter();
		boolean result = true;
		do
		{
			if( max_steps > Math.max( image.getWidth() , image.getHeight() ) * 2 )
			{
				result = false;
				break;
			}
			//System.out.println( "getNext " + max_steps + " " + ( max_steps <= Math.max( image.getWidth() , image.getHeight() ) * 2 ) + " " + ( Math.max( image.getWidth() , image.getHeight() ) * 2 ) );
			answer = answer.getNextLocation( current_direction );
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
			block.setLocationDouble( answer );
		}
		while( isOutside( block ) );
		//System.out.println( "getNext end" );
		return result;
	}
	
	private boolean isOutside( RotatableRectangle block )
	{
		boolean answer = false;
		LocationDouble center = block.getCenter();
		Rectangle2D br = block.getBoundingRectangle();
		if( br.getX() < 0 
			|| br.getX() >= ( image.getWidth() - br.getWidth() ) 
			|| br.getY() < 0 
			|| br.getY() >= ( image.getHeight() - br.getHeight() ) )
		{
			answer = true;
		}
		return answer;
	}
	
	private RotatableRectangle createRandomBlock()
	{
		int w = rand.nextInt( current_width ) + 1;
		int h = rand.nextInt( current_width ) + 1;		
		double angle = rand.nextInt( (int) divisor ) * factor;
		if( w > current_width * 0.9 && current_width > 1 )
		{
			current_width--;			
		}
		if( h > current_height * 0.9 && current_height > 1 )
		{
			current_height--;
		}
		return new RotatableRectangle( new LocationDouble( 0 , 0 ) , w , h , angle );
	}
}

/*
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
*/

class BlockComparator implements Comparator<RotatableRectangle>
{
	private final boolean ascending;
	
	public BlockComparator( boolean ascending )
	{
		this.ascending = ascending;
	}
	
	@Override
	public int compare( RotatableRectangle first , RotatableRectangle second )
	{
		double volume1 = first.getWidth() * first.getHeight();
		double volume2 = second.getWidth() * second.getHeight();
		int answer = (int) ( volume1 - volume2 );
		if( !ascending )
		{
			answer = -answer;
		}
		return answer;
	}
}
package emr.colour.generators;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import emr.colour.colourlist.Colour;
import emr.colour.ImageSettings;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Random;
import emr.stuff.Location;
import java.util.Comparator;
import java.awt.Color;
import java.util.Map.Entry;
import java.util.Map;
import emr.colour.comparators.CustomComparator;

public class ImageGeneratorCloud implements ImageGenerator
{
	private HashMap<Location, Colour> locationmap;
	private TreeMap<Colour, List<Location>> scoremap;
	private Random rand;
	private Location start;
	private BufferedImage image;
	private ImageSettings settings;
	private List<Colour> colours;
	private int backgroundcolour;
	
	public ImageGeneratorCloud( BufferedImage image , List<Colour> colours , ImageSettings settings )
	{
		locationmap = new HashMap<>();
		boolean score_ascending = settings.getSetting( "score_ascending" );
		String terms = settings.getSetting( "score_comparator" );
		Comparator< Colour > comparator = new CustomComparator( score_ascending , terms );
		scoremap = new TreeMap<>( comparator );
		long seed = settings.getSetting( "seed" );
		rand = new Random( seed );
		this.image = image;
		this.settings = settings;
		this.colours = new LinkedList<Colour>( colours );
		backgroundcolour = Color.decode( settings.getSetting( "background_colour" ) ).getRGB();
	}
	
	@Override
	public BufferedImage generateImage()
	{
		//generation
		int x = rand.nextInt( image.getWidth() );
		int y = rand.nextInt( image.getHeight() );
		start = new Location( x , y );
		addEntry( start );
		while( !colours.isEmpty() && !locationmap.isEmpty() )
		{
			//pop a colour
			Colour colour = colours.remove(0);
			//find the best fit from the wavefront
			boolean precise = settings.getSetting( "precise" );
			Location best;
			if( precise )
			{
				best = getBestPrecise( colour );
			}
			else
			{
				best = getBestRough( colour );
			}
			//remove best location from wavefront
			removeEntry( best );
			//place the colour
			image.setRGB( best.getX() , best.getY() , colour.getIntValue() );
			//add the neighbours of the newly placed colour to the wavefront
			addNeighbours( best );			
		}
		return image;
	}
	
	private Location getBestPrecise( Colour colour )
	{
		List<Location> bestlist = scoremap.entrySet().parallelStream().min( Comparator.comparing( entry -> entry.getKey().getDifference( colour ) ) ).get().getValue();		
		return getBestFromList( bestlist );
	}
	
	private Location getBestRough( Colour colour )
	{
		List<Location> bestlist = scoremap.get( colour );
		if( bestlist == null )
		{
			Map.Entry< Colour, List< Location > > lower = scoremap.lowerEntry( colour );
			Map.Entry< Colour, List< Location > > higher = scoremap.higherEntry( colour );
			if( lower == null && higher == null )
			{
				System.out.println( "something went wrong because scoremap is empty" );
			}
			else
			{
				if( lower == null )
				{
					bestlist = higher.getValue();
				}
				else if( higher == null )
				{
					bestlist = lower.getValue();
				}
				else
				{
					int lowerdifference = lower.getKey().getDifference( colour );
					int higherdifference = higher.getKey().getDifference( colour );
					if( lowerdifference >= higherdifference )
					{
						bestlist = higher.getValue();
					}
					else
					{
						bestlist = lower.getValue();
					}
				}
			}
		}
		return getBestFromList( bestlist );
	}
	
	private Location getBestFromList( List< Location > bestlist )
	{
		Location best = null;
		if( bestlist == null || bestlist.isEmpty() )
		{
			System.out.println( "something went wrong, bestlist is null or empty" );
		}
		else
		{
			String decider = settings.getSetting( "decider" );
			switch( decider )
			{
				case "oldest":
					best = bestlist.get( 0 );
					break;
				case "newest":
					best = bestlist.get( bestlist.size() - 1 );
					break;
				case "closest":
					best = bestlist.stream().min( Comparator.comparing( loc -> getDistance( loc ) ) ).get();
					break;
				case "farthest":
					best = bestlist.stream().max( Comparator.comparing( loc -> getDistance( loc ) ) ).get();
					break;
				case "random":
				default:
					best = bestlist.get( rand.nextInt( bestlist.size() ) );
					break;
			}		
		}
		return best;
	}
	
	private double getDistance( Location here )
	{
		int xd = Math.abs( here.getX() - start.getX() );
		int yd = Math.abs( here.getY() - start.getY() );
		boolean tiling = settings.getSetting( "tiling" );
		if( tiling )
		{
			xd = Math.min( xd , image.getWidth() - xd );
			yd = Math.min( yd , image.getHeight() - yd );
		}
		return Math.sqrt( ( xd * xd ) + ( yd * yd ) );
	}
	
	private Colour getScore( Location loc )
	{
		Colour result = new Colour( 0 , 0 , 0 );
		List<Color> neighbourcolours = new ArrayList<>();
		int comparison_distance = settings.<Long>getSetting( "comparison_distance" ).intValue();
		
		for( Location nb : getNeighbours( loc , comparison_distance ) )
		{
			Location neighbour = normalize( nb );
			int colour = image.getRGB( neighbour.getX() , neighbour.getY() );
			//if( colour != -16777216 ) //not background black
			if( colour != backgroundcolour )
			{
				neighbourcolours.add( new Color( colour ) );
			}
		}
		if( !neighbourcolours.isEmpty() )
		{
			String score_type = settings.getSetting( "score_type" );
			if( score_type.equals( "average" ) )
			{
				double rtotal = 0.0;
				double gtotal = 0.0;
				double btotal = 0.0;
				for( Color colour : neighbourcolours )
				{
					rtotal += colour.getRed();
					gtotal += colour.getGreen();
					btotal += colour.getBlue();
				}
				result = new Colour( (int)( rtotal / neighbourcolours.size() ) , (int)( gtotal / neighbourcolours.size() ) , (int)( btotal / neighbourcolours.size() ) );
			}
			else if( score_type.equals( "minimum old" ) )
			{
				Colour min = new Colour( 255 , 255 , 255 );
				for( Color colour : neighbourcolours )
				{
					Colour temp = new Colour( colour );
					if( temp.compareTo( min ) < 0 )
					{
						min = temp;
					}
				}
				result = min;
			}
			else if( score_type.equals( "minimum" ) )
			{
				int min_red = 255;
				int min_blue = 255;
				int min_green = 255;
				for( Color colour : neighbourcolours )
				{
					if( colour.getRed() < min_red )
					{
						min_red = colour.getRed();
					}
					if( colour.getGreen() < min_green )
					{
						min_green = colour.getGreen();
					}
					if( colour.getBlue() < min_blue )
					{
						min_blue = colour.getBlue();
					}
				}
				result = new Colour( min_red , min_green , min_blue );
			}
		}
		return result;
	}
	
	private List<Location> getNeighbours( Location center , int distance )
	{
		List<Location> list = new ArrayList<>();
		if( distance > 0 )
		{
			for( int y = center.getY() - distance; y <= center.getY() + distance; y++ )
			{
				for( int x = center.getX() - distance; x <= center.getX() + distance; x++ )
				{
					if( x == center.getX() && y == center.getY() ) continue;
					list.add( new Location( x , y ) );
				}
			}
		}
		return list;
	}
	
	private void addNeighbours( Location loc )
	{
		boolean tiling = settings.getSetting( "tiling" );
		for( Location location : getNeighbours( loc , 1 ) )
		{
			Location neighbour = location;
			if( tiling )
			{
				neighbour = normalize( location );
			}
			if( tiling || isInside( neighbour ) )
			{
				//if( image.getRGB( neighbour.getX() , neighbour.getY() ) == -16777216 ) //background black
				int neighbourcolour = image.getRGB( neighbour.getX() , neighbour.getY() );				
				if( neighbourcolour == backgroundcolour )
				{
					addEntry( neighbour );
				}
			}
		}
	}
	
	private void addEntry( Location loc )
	{
		removeEntry( loc );
		Colour score = getScore( loc );
		locationmap.put( loc , score );
		List<Location> list = scoremap.get( score );
		if( list == null )
		{
			list = new ArrayList<>();
			scoremap.put( score , list );			
		}
		list.add( loc );
	}
	
	private void removeEntry( Location loc )
	{
		Colour oldscore = locationmap.remove( loc );
		if( oldscore != null )
		{
			List<Location> list = scoremap.get( oldscore );
			list.remove( loc );
			if( list.isEmpty() )
			{
				scoremap.remove( oldscore );
			}
		}
	}
	
	private boolean isInside( Location loc )
	{
		return loc.getX() >= 0 && loc.getY() >= 0 && loc.getX() < image.getWidth() && loc.getY() < image.getHeight();
	}
	
	private Location normalize( Location loc )
	{
		//check x
		int newx = loc.getX();
		if( newx < 0 )
		{
			newx += image.getWidth();
		}
		else if( newx >= image.getWidth() )
		{
			newx -= image.getWidth();
		}
		//check y
		int newy = loc.getY();
		if( newy < 0 )
		{
			newy += image.getHeight();
		}
		else if( newy >= image.getHeight() )
		{
			newy -= image.getHeight();
		}
		return new Location( newx , newy );
	}
}
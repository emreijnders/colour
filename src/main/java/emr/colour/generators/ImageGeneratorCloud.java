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
import java.util.Set;
import java.util.HashSet;

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
	private boolean quarters;
	private int width;
	private int height;
	
	public ImageGeneratorCloud( BufferedImage image , List<Colour> colours , ImageSettings settings )
	{
		locationmap = new HashMap<>();
		//boolean score_ascending = settings.getSetting( "score_ascending" );
		//String terms = settings.getSetting( "score_comparator" );
		//Comparator< Colour > comparator = new CustomComparator( score_ascending , terms );
		//scoremap = new TreeMap<>( comparator );
		//no no no
		//the treemap needs the natural ordering or it breaks!
		//!!!!!
		scoremap = new TreeMap<>();
		long seed = settings.getSetting( "seed" );
		rand = new Random( seed );
		this.image = image;
		this.settings = settings;
		this.colours = new LinkedList<Colour>( colours );
		backgroundcolour = Color.decode( settings.getSetting( "background_colour" ) ).getRGB();
		quarters = settings.getSetting( "quarters" );
		if( !isEven( image.getWidth() ) || !isEven( image.getHeight() ) )
		{
			System.err.println( "image size not even, required for quarters setting" );
			System.err.println( "width: " + image.getWidth() + " height: " + image.getHeight() );
			System.exit(0);
		}
		width = image.getWidth();
		height = image.getHeight();
		if( quarters )
		{
			width = width / 2;
			height = height / 2;
		}
		
	}
	
	@Override
	public BufferedImage generateImage()
	{
		//generation
		int x = rand.nextInt( width );
		int y = rand.nextInt( height );
		start = new Location( x , y );
		addEntry( start );
		
		//        Set<Location> allbests = new HashSet<>();
		while( !colours.isEmpty() && !locationmap.isEmpty() )
		{
			//pop a colour
			Colour colour = getNextColour();
			//     System.out.println("colour:" + colour);
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
			/*
			if( allbests.add(best) == false )
			{
				System.out.println("something wrong:" + best);
				Colour mark1 = locationmap.get( best );
				System.out.println( "marker 1:" + mark1 );
				System.out.println( "marker 2:" + scoremap.get( mark1 ) );
				System.out.println( new Color( image.getRGB( best.getX() , best.getY() ) ) );
				System.exit(0);
			}
			*/
			//      System.out.println("############## best:" + best);
			//remove best location from wavefront
			removeEntry( best );
			//place the colour
			drawBest( best , colour );
			
			//add the neighbours of the newly placed colour to the wavefront
			addNeighbours( best );
			//     System.out.println( "afterbest" + locationmap );
			//     System.out.println( scoremap );
		}
		System.out.println( image.getRGB(x,y) );
		System.out.println( image.getRGB(image.getWidth() - x - 1,y) );
		System.out.println( image.getRGB(x, image.getHeight() - y - 1 ) );
		System.out.println( image.getRGB(image.getWidth() - x - 1,image.getHeight() - y - 1) );
		return image;
	}
	
	private Colour getNextColour()
	{
		Colour colour = colours.remove( 0 );
		if( colour.getIntValue() == backgroundcolour )
		{
			colour = colours.remove( 0 );
		}
		return colour;
	}
	
	private void drawBest( Location best , Colour colour )
	{
		image.setRGB( best.getX() , best.getY() , colour.getIntValue() );
		boolean no_reuse = settings.getSetting( "no_reuse" );
		if( quarters )
		{
			int secondx = image.getWidth() - best.getX() - 1;
			int secondy = image.getHeight() - best.getY() - 1;
			if( no_reuse )
			{
				//System.out.println("mark 1" + colour);
				colour = getNextColour();
				//System.out.println("mark 2" + colour);
			}
			image.setRGB( best.getX() , secondy , colour.getIntValue() );
			if( no_reuse )
			{
				colour = getNextColour();
				//System.out.println("mark 3" + colour);
			}
			image.setRGB( secondx , best.getY() , colour.getIntValue() );
			if( no_reuse )
			{
				colour = getNextColour();
				//System.out.println("mark 4" + colour);
			}
			image.setRGB( secondx , secondy , colour.getIntValue() );
		}
	}
	
	private boolean isEven( int number )
	{
		return Math.abs( number ) % 2 == 0;
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
		
		for( Location neighbour : getNeighbours( loc , comparison_distance ) )
		{
			int colour = image.getRGB( neighbour.getX() , neighbour.getY() );
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
		boolean tiling = settings.getSetting( "tiling" );
		List<Location> list = new ArrayList<>();
		if( distance > 0 )
		{
			for( int y = center.getY() - distance; y <= center.getY() + distance; y++ )
			{
				for( int x = center.getX() - distance; x <= center.getX() + distance; x++ )
				{
					if( x == center.getX() && y == center.getY() ) continue;
					Location neighbour = new Location( x , y );
					if( tiling )
					{
						neighbour = normalize( neighbour );
					}
					if( tiling || isInside( neighbour ) )
					{
						list.add( neighbour );
					}
				}
			}
		}
		return list;
	}
	
	private void addNeighbours( Location loc )
	{
		for( Location neighbour : getNeighbours( loc , 1 ) )
		{
			int neighbourcolour = image.getRGB( neighbour.getX() , neighbour.getY() );				
			if( neighbourcolour == backgroundcolour )
			{
				addEntry( neighbour );
			}
		}
	}
	
	private void addEntry( Location loc )
	{
		/*System.out.println("adding:" + loc);
		if( loc.equals( new Location(26,32)) ) 
		{
			System.out.println("halt");
			System.out.println( locationmap );
			System.out.println( scoremap );
			System.exit(0);
		}
		*/
		removeEntry( loc );
		Colour score = getScore( loc );
		/*     System.out.println("score:" + score);
		if(locationmap.containsKey(loc)) 
		{
			System.out.println("locationmap already contains:" + loc);
			System.exit(0);
		}
		*/
		locationmap.put( loc , score );
		List<Location> list = scoremap.get( score );
		//       System.out.println(loc+","+score+":"+list);
		//       System.out.println("#######marker:" + scoremap );
		if( list == null )
		{
			//        System.out.println("list null");
			list = new ArrayList<>();
			scoremap.put( score , list );			
		}
		list.add( loc );
	}
	
	private void removeEntry( Location loc )
	{
		//      System.out.println("removing:" + loc);
		Colour oldscore = locationmap.remove( loc );
		//        System.out.println("oldscore:" + oldscore);
		if( oldscore != null )
		{
			List<Location> list = scoremap.get( oldscore );
			boolean temp = list.remove( loc );
			//        System.out.println("true?:"+temp);
			if( list.isEmpty() )
			{
				scoremap.remove( oldscore );
			}
		}
	}
	
	private boolean isInside( Location loc )
	{
		return loc.getX() >= 0 && loc.getY() >= 0 && loc.getX() < width && loc.getY() < height;
	}
	
	private Location normalize( Location loc )
	{
		//check x
		int newx = loc.getX();
		if( newx < 0 )
		{
			newx += width;
		}
		else if( newx >= width )
		{
			newx -= width;
		}
		//check y
		int newy = loc.getY();
		if( newy < 0 )
		{
			newy += height;
		}
		else if( newy >= height )
		{
			newy -= height;
		}
		return new Location( newx , newy );
	}
}
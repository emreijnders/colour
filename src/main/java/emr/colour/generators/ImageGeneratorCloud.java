package emr.colour.generators;

import java.awt.image.BufferedImage;
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

public class ImageGeneratorCloud implements ImageGenerator
{
	private HashMap<Location, Colour> locationmap;
	private TreeMap<Colour, List<Location>> scoremap;
	private BufferedImage image;
	
	@Override
	public BufferedImage generateImage( BufferedImage image , List<Colour> colours , ImageSettings settings )
	{
		//initialization
		locationmap = new HashMap<>();
		scoremap = new TreeMap<>();
		long seed = settings.getSetting( "seed" );
		Random rand = new Random( seed );
		this.image = image;
		
		//generation
		int x = rand.nextInt( image.getWidth() );
		int y = rand.nextInt( image.getHeight() );
		Location start = new Location( x , y );
		addEntry( start );
		while( !colours.isEmpty() && !locationmap.isEmpty() )
		{
			//pop a colour
			Colour colour = colours.remove(0);
			//find the best fit from the wavefront
			Location best = scoremap.entrySet().parallelStream().min( Comparator.comparing( entry -> entry.getKey().getDifference( colour ) ) ).get().getValue().get( 0 );
			//remove best location from wavefront
			removeEntry( best );
			//place the colour
			image.setRGB( best.X , best.Y , colour.getIntValue() );
			//add the neighbours of the newly placed colour to the wavefront
			addNeighbours( best );			
		}
		return image;
	}
	
	private Colour getScore( Location loc )
	{
		Colour result = new Colour( 0 , 0 , 0 );
		List<Color> neighbourcolours = new ArrayList<>();
		for( Location nb : loc.getNeighbours() )
		{
			Location neighbour = validate( nb );
			int colour = image.getRGB( neighbour.X , neighbour.Y );
			if( colour != -16777216 ) //not background black
			{
				neighbourcolours.add( new Color( colour ) );
			}
		}
		if( !neighbourcolours.isEmpty() )
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
		return result;
	}
	
	private void addNeighbours( Location loc )
	{
		for( Location location : loc.getNeighbours() )
		{
			Location neighbour = validate( location );
			if( image.getRGB( neighbour.X , neighbour.Y ) == -16777216 ) //background black
			{
				addEntry( neighbour );
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
	
	private Location validate( Location loc )
	{
		//check x
		int newx = loc.X;
		if( newx < 0 )
		{
			newx += image.getWidth();
		}
		else if( newx >= image.getWidth() )
		{
			newx -= image.getWidth();
		}
		//check y
		int newy = loc.Y;
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
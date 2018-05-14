package emr.colour.generators;

import java.util.List;
import java.util.ArrayList;
import emr.colour.ImageSettings;
import emr.colour.colourlist.CompoundColourList;
import emr.colour.colourlist.Colour;
import emr.colour.comparators.CustomComparator;
import java.util.Random;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ColourListFactory
{
	public static List<Colour> generateColourList( ImageSettings settings, CompoundColourList ccl )
	{
		//generate colourlist from ccl
		//--generate list
		List<Colour> colourlist = ccl.getCompoundColourList();		
		//--shuffle list if required
		boolean shuffle = settings.getSetting( "shuffle" );
		if( shuffle )
		{
			long seed = settings.getSetting( "seed" );
			Collections.shuffle( colourlist , new Random( seed ) );
		}
		//--sort list if required
		boolean sort = settings.getSetting( "sort" );
		if( sort )
		{
			//---create comparator
			boolean ascending = settings.getSetting( "ascending" );
			String terms = settings.getSetting( "sorting_comparator" );
			Comparator<Colour> comparator = new CustomComparator( ascending , terms );
			colourlist = new ArrayList<Colour>( colourlist.parallelStream().sorted( comparator ).collect( Collectors.toList() ) );
		}
		return colourlist;
	}
}
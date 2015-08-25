package emr.colour;

import emr.stuff.Listener;
import emr.stuff.Message;
import emr.stuff.Talker;
import emr.colour.ImageSettings;
import emr.colour.ImageSaver;
import emr.colour.gui.ImageGeneratorGUI;
import emr.colour.colourlist.CCLFileParser;
import emr.colour.colourlist.CompoundColourList;
import emr.colour.colourlist.Colour;
import emr.colour.comparators.CustomComparator;
import emr.colour.generators.Generators;
import emr.colour.generators.ImageGenerator;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.Scanner;
import java.awt.image.BufferedImage;

public class ImageGeneratorController implements Listener
{
	private ImageSettings settings;
	private CompoundColourList ccl;
	private String settingsfilename;
	private String cclfilename;
	private ImageGeneratorGUI gui;
	
	public ImageGeneratorController()
	{
		settingsfilename = "";
		cclfilename = "";
		Talker talker = new IGTalker();
		talker.addListener( this );
		settings = new ImageSettings();
		CompoundColourList ccl = new CompoundColourList(); //empty colourlist
		gui = new ImageGeneratorGUI( talker );
		gui.startGUI();
	}
	
	@Override
	public void receiveMessage( Message message )
	{
		switch( IGMessageType.valueOf( message.getType() ) )
		{
			case LOAD_CCL:
				loadCCL( message.getText() );
				break;
			case LOAD_SETTINGS:
				loadSettings( message.getText() );
				break;
			case RELOAD:
				reload();
				break;
			case GENERATE:
				generateImage();
				break;
		}
	}
	
	private void generateImage()
	{
		int width = settings.<Long>getSetting( "image_width" ).intValue();
		int height = settings.<Long>getSetting( "image_height" ).intValue();
		BufferedImage image = new BufferedImage( width , height , BufferedImage.TYPE_INT_RGB );
		
		boolean show_gui = settings.getSetting( "show_gui" );
		if( show_gui )
		{
			gui.showImage( image );
		}
		String generatorname = settings.getSetting( "generator" );
		ImageGenerator generator  = Generators.getGenerator( Generators.valueOf( generatorname ) );
		List<Colour> colours = getColourList();
		generator.generateImage( image , colours , settings );
		
		boolean save = settings.getSetting( "save" );
		if( save )
		{
			saveImage( image );
		}
	}
	
	private List<Colour> getColourList()
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
	
	private void saveImage( BufferedImage image )
	{		
		ImageSaver.saveImage( image , settings , ccl );
	}
	
	private void loadSettings( String filename )
	{		
		settings.setSettings( filename );
	}
	
	private void loadCCL( String filename )
	{
		cclfilename = filename;
		ccl = new CCLFileParser( filename ).parseFile();
	}
	
	private void reload()
	{
		loadSettings( settingsfilename );
		loadCCL( cclfilename );
	}
}
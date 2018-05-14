package emr.colour;

import emr.stuff.Listener;
import emr.stuff.Message;
import emr.stuff.Talker;
import emr.colour.ImageSettings;
import emr.colour.ImageSaver;
import emr.colour.IGMessageType;
import emr.colour.gui.ImageGeneratorGUI;
import emr.colour.colourlist.CCLFileParser;
import emr.colour.colourlist.CompoundColourList;
import emr.colour.colourlist.Colour;
import emr.colour.comparators.CustomComparator;
import emr.colour.generators.GeneratorFactory;
import emr.colour.generators.GeneratorTypes;
import emr.colour.generators.ImageGenerator;
import emr.colour.generators.ColourListFactory;
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
	private Talker talker;
	
	public ImageGeneratorController()
	{
		settingsfilename = "";
		cclfilename = "";
		talker = new IGTalker();
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
		//get type
		GeneratorTypes type = GeneratorTypes.valueOf( settings.getSetting( "generator" ) );
		
		//get colours
		List<Colour> colours = ColourListFactory.generateColourList( settings, ccl );
		
		//make generator
		GeneratorFactory generatorFactory = new GeneratorFactory( type , settings );
		
		generatorFactory.addColourList( colours );
		
		ImageGenerator generator = generatorFactory.getGenerator();
		
		//get the image for reference
		BufferedImage image = generatorFactory.getImage();
		
		//show gui if needed
		boolean show_gui = settings.getSetting( "show_gui" );
		if( show_gui )
		{
			gui.showImage( image );
		}
		
		System.out.println( "starting generator" );
		generator.generateImage();
		
		System.out.println( "done generating" );
		
		//save image if needed
		boolean save = settings.getSetting( "save" );
		if( save )
		{
			saveImage( image );
			System.out.println( "image saved" );
		}
	}
	
	private void saveImage( BufferedImage image )
	{		
		ImageSaver.saveImage( image , settings , ccl );
	}
	
	private void loadSettings( String filename )
	{		
		settingsfilename = filename;
		settings.setSettings( filename );
		new Thread( () -> talker.sendMessage( new IGMessage( IGMessageType.NEW_SETTINGS , settings.toString() ) ) ).start();
	}
	
	private void loadCCL( String filename )
	{
		cclfilename = filename;
		ccl = new CCLFileParser( filename ).parseFile();
		new Thread( () -> talker.sendMessage( new IGMessage( IGMessageType.NEW_CCL , ccl.toString() ) ) ).start();
	}
	
	private void reload()
	{
		loadSettings( settingsfilename );
		loadCCL( cclfilename );
	}
}
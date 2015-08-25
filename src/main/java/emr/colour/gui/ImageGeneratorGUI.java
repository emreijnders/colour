package emr.colour.gui;

import emr.stuff.SettingsFileParser;
import emr.stuff.Settings;
import emr.colour.ImageSettings;
import emr.colour.ImageSaver;
import emr.colour.colourlist.CCLFileParser;
import emr.colour.colourlist.CompoundColourList;
import emr.colour.colourlist.Colour;
import emr.colour.comparators.CustomComparator;
import emr.colour.generators.Generators;
import emr.colour.generators.ImageGenerator;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.Timer;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.Scanner;

public class ImageGeneratorGUI
{
	private ImageSettings settings;
	private CompoundColourList ccl;
	private String settingsfilename;
	private String cclfilename;
	private JFrame frame;
	
	public ImageGeneratorGUI()
	{
		loadImageSettings();
		ccl = new CompoundColourList(); //empty colourlist
		settingsfilename = "";
		cclfilename = "";
	}
	
	public void startGUI()
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				frame = new JFrame( "Image Generator" );
				frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
				
				JPanel panel = constructPanel();
				frame.add( panel );
				
				JMenuBar menubar = constructMenu();
				frame.setJMenuBar( menubar );
				
				frame.pack();
				frame.setVisible( true );
			}
		}
		);
	}
	
	private JPanel constructPanel()
	{
		JPanel panel = new IGPanel();
		JButton reloadbutton = new JButton( new ReloadAction( ImageGeneratorGUI.this ) );
		panel.add( reloadbutton );
		
		JButton startbutton = new JButton( new StartAction( ImageGeneratorGUI.this ) );
		panel.add( startbutton );
		return panel;
	}
	
	private JMenuBar constructMenu()
	{
		JMenuBar menubar = new JMenuBar();
		JMenu menu = new JMenu( "File" );
		
		JMenuItem loadsettings = new JMenuItem( new LoadSettingsAction( ImageGeneratorGUI.this , frame ) );
		JMenuItem loadccl = new JMenuItem( new LoadCCLAction( ImageGeneratorGUI.this , frame ) );
		JMenuItem exit = new JMenuItem( new ExitAction( frame ) );
		
		menu.add( loadsettings );
		menu.add( loadccl );
		menu.add( exit );
		
		menubar.add( menu );
		
		return menubar;
	}
	
	public void generateImage()
	{
		int width = settings.<Long>getSetting( "image_width" ).intValue();
		int height = settings.<Long>getSetting( "image_height" ).intValue();
		BufferedImage image = new BufferedImage( width , height , BufferedImage.TYPE_INT_RGB );
		
		boolean gui = settings.getSetting( "show_gui" );
		if( gui )
		{			
			SwingUtilities.invokeLater( new Runnable()
			{
				@Override
				public void run()
				{
					JDialog display = new JDialog( frame );
					display.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
					JPanel displaypanel = new DisplayPanel( image );					
					display.add( displaypanel );
					
					class UpdateAction extends AbstractAction
					{
						@Override
						public void actionPerformed( ActionEvent e )
						{
							displaypanel.repaint();
						}
					}
					
					display.pack();
					display.setVisible( true );
					Timer timer = new Timer( 33 , new UpdateAction() );
					timer.start();
				}
			}
			);
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
	
	private void loadImageSettings()
	{
		settings = new ImageSettings( new SettingsFileParser( getClass().getResourceAsStream( "defaults.settings" ) ).parseFile() );
	}
	
	public void loadSettings( String filename )
	{
		settingsfilename = filename;
		settings.setSettings( new SettingsFileParser( filename ).parseFile() );
	}
	
	public void loadCCL( String filename )
	{
		cclfilename = filename;
		ccl = new CCLFileParser( filename ).parseFile();
	}
	
	void reload()
	{
		loadSettings( settingsfilename );
		loadCCL( cclfilename );
	}
}
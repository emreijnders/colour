package emr.colour.gui;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.Timer;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;
import emr.stuff.Talker;

public class ImageGeneratorGUI
{
	private JFrame frame;
	private Talker talker;
	
	public ImageGeneratorGUI( Talker talker )
	{
		this.talker = talker;
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
		});
	}
	
	private JPanel constructPanel()
	{
		JPanel panel = new IGPanel();
		JButton reloadbutton = new JButton( new ReloadAction( talker ) );
		panel.add( reloadbutton );
		
		JButton startbutton = new JButton( new StartAction( talker ) );
		panel.add( startbutton );
		return panel;
	}
	
	private JMenuBar constructMenu()
	{
		JMenuBar menubar = new JMenuBar();
		JMenu menu = new JMenu( "File" );
		
		JMenuItem loadsettings = new JMenuItem( new LoadSettingsAction( frame , talker ) );
		JMenuItem loadccl = new JMenuItem( new LoadCCLAction( frame , talker ) );
		JMenuItem exit = new JMenuItem( new ExitAction( frame ) );
		
		menu.add( loadsettings );
		menu.add( loadccl );
		menu.add( exit );
		
		menubar.add( menu );
		
		return menubar;
	}
	
	public void showImage( BufferedImage image )
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
				
				display.pack();
				display.setVisible( true );
				Timer timer = new Timer( 33 , new UpdateAction( displaypanel ) );
				timer.start();
			}
		}
		);
	}
}
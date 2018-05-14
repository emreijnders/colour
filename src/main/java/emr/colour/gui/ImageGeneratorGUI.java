package emr.colour.gui;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.Timer;
import javax.swing.JLabel;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;
import emr.stuff.Talker;
import emr.stuff.Listener;
import emr.stuff.Message;
import emr.colour.IGMessageType;

public class ImageGeneratorGUI implements Listener
{
	private JFrame frame;
	private Talker talker;
	private JLabel settingslabel;
	private JLabel ccllabel;
	
	public ImageGeneratorGUI( Talker talker ) 
	{
		this.talker = talker;
		talker.addListener( this );
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
		
		settingslabel = new JLabel();
		panel.add( settingslabel );
		
		ccllabel = new JLabel();
		panel.add( ccllabel );
		
		return panel;
	}
	
	@Override
	public void receiveMessage( Message message )
	{
		switch( IGMessageType.valueOf( message.getType() ) )
		{
			case NEW_SETTINGS:
				updateSettings( message.getText() );
				break;
			case NEW_CCL:
				updateCCL( message.getText() );
				break;
		}
	}
	
	private void updateSettings( String settingstext )
	{
		StringBuilder labeltext = new StringBuilder();
		labeltext.append( "<HTML>" );
		for( String setting : settingstext.split( "#" ) )
		{
			labeltext.append( setting + "<br>" );
		}
		labeltext.append( "</HTML>" );
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				settingslabel.setText( labeltext.toString() );
				frame.repaint();
			}
		});
	}
	
	private void updateCCL( String ccltext )
	{
		StringBuilder labeltext = new StringBuilder();
		labeltext.append( "<HTML>" );
		for( String line : ccltext.split( "#" ) )
		{
			labeltext.append( line + "<br>" );
		}
		labeltext.append( "</HTML>" );
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				ccllabel.setText( labeltext.toString() );
				frame.repaint();
			}
		});
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
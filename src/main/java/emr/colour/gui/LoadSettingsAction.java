package emr.colour.gui;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

class LoadSettingsAction extends AbstractAction
{
	private ImageGeneratorGUI igg;
	private JFrame frame;
	
	public LoadSettingsAction( ImageGeneratorGUI igg , JFrame frame )
	{
		super( "load settings file..." );
		this.igg = igg;
		this.frame = frame;
	}
	
	@Override
	public void actionPerformed( ActionEvent e )
	{
		String startdir = System.getProperty( "user.dir" );						
		JFileChooser jfc = new JFileChooser( startdir );
		jfc.setFileFilter( new CustomFilter( "settings" ) );
		if( jfc.showOpenDialog( frame ) == JFileChooser.APPROVE_OPTION )
		{
			String name = jfc.getSelectedFile().getName();
			new Thread( () -> igg.loadSettings( name ) ).start();
		}
	}
}
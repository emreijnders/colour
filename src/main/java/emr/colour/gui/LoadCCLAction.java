package emr.colour.gui;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

class LoadCCLAction extends AbstractAction
{
	private ImageGeneratorGUI igg;
	private JFrame frame;
	
	public LoadCCLAction( ImageGeneratorGUI igg , JFrame frame )
	{
		super( "load colourlist file..." );
		this.igg = igg;
		this.frame = frame;
	}
	
	@Override
	public void actionPerformed( ActionEvent e )
	{
		String startdir = System.getProperty( "user.dir" );						
		JFileChooser jfc = new JFileChooser( startdir );
		jfc.setFileFilter( new CustomFilter( "ccl" ) );
		if( jfc.showOpenDialog( frame ) == JFileChooser.APPROVE_OPTION )
		{
			String name = jfc.getSelectedFile().getName();
			new Thread( () -> igg.loadCCL( name ) ).start();
		}
	}
}
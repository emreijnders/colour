package emr.colour.gui;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import emr.stuff.Talker;
import emr.stuff.Message;
import emr.colour.IGMessage;
import emr.colour.IGMessageType;

class LoadSettingsAction extends AbstractAction
{
	private JFrame frame;
	private Talker talker;
	
	public LoadSettingsAction( JFrame frame , Talker talker )
	{
		super( "load settings file..." );		
		this.frame = frame;
		this.talker = talker;
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
			Message message = new IGMessage( IGMessageType.LOAD_SETTINGS , name );
			new Thread( () -> talker.sendMessage( message ) ).start();
		}
	}
}
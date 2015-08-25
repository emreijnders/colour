package emr.colour.gui;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import emr.stuff.Talker;
import emr.stuff.Message;
import emr.colour.IGMessage;
import emr.colour.IGMessageType;

class ReloadAction extends AbstractAction
{
	private Talker talker;
	
	public ReloadAction( Talker talker )
	{
		super( "reload" );
		this.talker = talker;
	}
	
	@Override
	public void actionPerformed( ActionEvent e )
	{
		new Thread( () -> talker.sendMessage( new IGMessage( IGMessageType.RELOAD , "" ) ) ).start();
	}
}
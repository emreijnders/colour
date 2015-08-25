package emr.colour.gui;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;

class ExitAction extends AbstractAction
{
	private JFrame frame;
	
	public ExitAction( JFrame frame )
	{
		super( "exit" );
		this.frame = frame;
	}
	
	@Override
	public void actionPerformed( ActionEvent e )
	{
		int result = JOptionPane.showConfirmDialog( frame , "Do you really want to exit?" , "Exit" , JOptionPane.YES_NO_CANCEL_OPTION );
		if( result == JOptionPane.YES_OPTION )
		{
			System.exit(0);
		}
	}
}
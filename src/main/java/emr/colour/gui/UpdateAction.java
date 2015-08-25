package emr.colour.gui;

import javax.swing.JPanel;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

class UpdateAction extends AbstractAction
{
	private JPanel displaypanel;
	
	public UpdateAction( JPanel displaypanel )
	{
		this.displaypanel = displaypanel;
	}
	
	@Override
	public void actionPerformed( ActionEvent e )
	{
		displaypanel.repaint();
	}
}
package emr.colour.gui;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

class ReloadAction extends AbstractAction
{
	private ImageGeneratorGUI igg;
	
	public ReloadAction( ImageGeneratorGUI igg )
	{
		super( "reload" );
		this.igg = igg;
	}
	
	@Override
	public void actionPerformed( ActionEvent e )
	{
		new Thread( () -> igg.reload() ).start();
	}
}
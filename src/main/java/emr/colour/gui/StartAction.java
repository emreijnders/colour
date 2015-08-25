package emr.colour.gui;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

class StartAction extends AbstractAction
{
	private ImageGeneratorGUI igg;
	
	public StartAction( ImageGeneratorGUI igg )
	{
		super( "Generate Image" );
		this.igg = igg;
	}
	
	@Override
	public void actionPerformed( ActionEvent e )
	{
		new Thread( () -> igg.generateImage() ).start();
	}
}
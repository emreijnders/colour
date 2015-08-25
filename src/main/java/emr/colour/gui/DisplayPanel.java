package emr.colour.gui;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

class DisplayPanel extends JPanel
{
	private BufferedImage image;
	
	public DisplayPanel( BufferedImage image )
	{
		this.image = image;
	}
	
	@Override
	protected void paintComponent( Graphics g )
	{
		super.paintComponent( g );
		Graphics2D g2 = (Graphics2D) g;
		if( image != null )
		{
			g2.drawImage( image , 0 , 0 , null );
		}
	}
	
	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension( image.getWidth() , image.getHeight() );
	}
}
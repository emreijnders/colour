package emr.colour.generators;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import emr.colour.colourlist.Colour;
import emr.colour.ImageSettings;
import emr.stuff.Location;
import emr.stuff.Direction;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.BasicStroke;

public class ImageGeneratorTree implements ImageGenerator
{
	private BufferedImage image;
	private ImageSettings settings;
	private List<Colour> colours;
	
	public ImageGeneratorTree( BufferedImage image , List<Colour> colours , ImageSettings settings )
	{
		this.image = image;
		this.settings = settings;
		this.colours = colours;
	}
	
	@Override
	public BufferedImage generateImage()
	{
		int depth = settings.<Long>getSetting( "max_depth" ).intValue();
		int starting_length = settings.<Long>getSetting( "starting_length" ).intValue();
		int angle_change = settings.<Long>getSetting( "angle_change" ).intValue();
		ArrayList<Branch> current = new ArrayList<>();		
		int angle = 0;
		//make first branch at depth 0
		int sx = (int) Math.round( image.getWidth() / 2.0 );
		int sy = image.getHeight() - 1;
		Location start = new Location( sx , sy );
		Branch trunk = new Branch( start , depth , angle , depth , starting_length * depth );
		current.add(trunk);
		//draw it
		drawBranches( image.createGraphics() , current , colours );
		int left_angle = angle;
		int right_angle = angle;
		Graphics2D g2 = image.createGraphics();
		while( depth > 1 )
		{
			//split into more branches			
			depth--;
			int length = starting_length * depth;
			ArrayList<Branch> next = new ArrayList<>();
			for( Branch branch : current )
			{
				List<Branch> split = splitBranch( branch , depth , angle_change , depth , length );
				next.add( split.get(0) );
				next.add( split.get(1) );
			}
			//draw them
			drawBranches( g2 , next , colours );
			current = next;
		}
		g2.dispose();
		return image;
	}
	
	private void drawBranches( Graphics2D g2 , List<Branch> branches , List<Colour> colours )
	{
		for( Branch branch : branches )
		{
			g2.setStroke( new BasicStroke( (float) branch.width , BasicStroke.CAP_ROUND , BasicStroke.JOIN_MITER ) );
			g2.setColor( new Color( colours.get( branch.depth ).getIntValue() ) );
			g2.draw( new Line2D.Double( branch.start.getX() , branch.start.getY() , branch.end.getX() , branch.end.getY() ) );			
		}
	}
	
	private List<Branch> splitBranch( Branch branch , int depth , int angle_change , int width , int length )
	{
		ArrayList<Branch> answer = new ArrayList<>();
		int left_angle = branch.angle - angle_change;
		if( left_angle < 0 )
		{
			left_angle = 360 + left_angle;
		}
		int right_angle = ( branch.angle + angle_change ) % 360;
		Branch left = new Branch( branch.end , depth , left_angle , width , length );
		Branch right = new Branch( branch.end , depth , right_angle , width , length );
		answer.add( left );
		answer.add( right );
		return answer;
	}
}

class Branch
{
	public int depth;
	public int angle;
	public double width;
	public double length;
	public Location start;
	public Location end;
	
	public Branch( Location start , int depth , int angle , double width , double length )
	{
		this.start = start;
		this.depth = depth;
		this.angle = angle;
		this.width = width;
		this.length = length;
		end = getEnd();
		//System.out.println( "branch at depth " + depth + " length " + length + " angle " + angle + " start " + start + " end " + end + " width " + width );
	}
	
	private Location getEnd()
	{
		int w = (int) Math.round( Math.sin( Math.toRadians( angle ) ) * length );
		int h = (int) Math.round( Math.cos( Math.toRadians( angle ) ) * length );
		int x = (int) ( start.getX() + w );
		int y = (int) ( start.getY() - h );
		return new Location( x , y );
	}
}
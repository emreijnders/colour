package emr.colour.colourlist;

import java.awt.Color;

public class Colour implements Comparable<Colour>
{
	private final Color COLOUR;
	
	public Colour( int r , int g , int b )
	{
		COLOUR = new Color( r , g , b );
	}
	
	public Colour( Color color )
	{
		COLOUR = color;
	}
	
	public int getRed()
	{
		return COLOUR.getRed();
	}
	
	public int getGreen()
	{
		return COLOUR.getGreen();
	}
	
	public int getBlue()
	{
		return COLOUR.getBlue();
	}
	
	public int getHue()
	{
		return Math.round( Color.RGBtoHSB( getRed() , getGreen() , getBlue() , null )[0] );
		//this looks a bit complicated so here's the breakdown
		//RGBtoHSB returns a float array
		//we get the value from the array, it's between 0 (inclusive) and 1 (exclusive)
	}
	
	public int getSaturation()
	{
		return Math.round( Color.RGBtoHSB( getRed() , getGreen() , getBlue() , null )[1] );
	}
	
	public int getBrightness()
	{
		return Math.round( Color.RGBtoHSB( getRed() , getGreen() , getBlue() , null )[2] );
	}
	
	public Integer getIntValue()
	{
		return COLOUR.getRGB();
	}
	
	public int getRGBScore()
	{
		return ( getRed() * getRed() ) + ( getGreen() * getGreen() ) + ( getBlue() * getBlue() );
	}
	
	public int getHSBScore()
	{
		return ( getHue() * getHue() ) + ( getSaturation() * getSaturation() ) + ( getBrightness() * getBrightness() );
	}
	
	@Override
	public int compareTo( Colour other )
	{
		return getIntValue().compareTo( other.getIntValue() );
	}
	
	public int getDifference( Colour other )
	{
		int rd = getRed() - other.getRed();
		int gd = getGreen() - other.getGreen();
		int bd = getBlue() - other.getBlue();
		return ( rd * rd ) + ( gd * gd ) + ( bd * bd );
	}
	
	public int getTypeValue( ColourType type )
	{
		int answer;
		switch( type )
		{
			case RED:
				answer = getRed();
				break;
			case GREEN:
				answer = getGreen();
				break;
			case BLUE:
				answer = getBlue();
				break;
			case HUE:
				answer = getHue();
				break;
			case SATURATION:
				answer = getSaturation();
				break;
			case BRIGHTNESS:
				answer = getBrightness();
				break;
			case RGBSCORE:
				answer = getRGBScore();
				break;
			case HSBSCORE:
				answer = getHSBScore();
				break;
			case NONE:
			default:
				answer = 0;
				break;
		}
		return answer;
	}
	
	@Override
	public boolean equals( Object o )
	{
		boolean answer = false;
		if( this == o ) answer  = true;
		else if( o != null && o instanceof Colour )
		{
			Colour other = (Colour) o;
			if( getIntValue() == other.getIntValue() ) answer = true;
		}
		return answer;
	}
	
	@Override
	public int hashCode()
	{
		return getIntValue();
	}
	
	@Override
	public String toString()
	{
		return "[" + getRed() + "," + getGreen() + "," + getBlue() + "]";
	}
}
package emr.colour;

import emr.stuff.Settings;

public class ImageSettings
{
	private Settings defaults;
	private Settings settings;
	
	public ImageSettings( Settings defaults )
	{
		this.defaults = defaults;
		settings = new Settings(); //empty settings
	}
	
	public void setSettings( Settings settings )
	{
		this.settings = settings;
	}
	
	public Settings getSettings()
	{
		return settings;
	}
	
	@SuppressWarnings( "unchecked" )
	public <T> T getSetting( String name )
	{
		return settings.getSetting( name , defaults.getSetting( name ) );
	}
}
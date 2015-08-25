package emr.colour;

import emr.stuff.Settings;
import emr.stuff.SettingsFileParser;

public class ImageSettings
{
	private Settings defaults;
	private Settings settings;
	
	public ImageSettings()
	{
		defaults = new SettingsFileParser( getClass().getResourceAsStream( "defaults.settings" ) ).parseFile();
		settings = new Settings(); //empty settings
	}
	
	public void setSettings( String filename )
	{
		settings = new SettingsFileParser( filename ).parseFile();
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
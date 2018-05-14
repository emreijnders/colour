package emr.colour;

import emr.stuff.Settings;
import emr.stuff.Setting;
import emr.stuff.SettingsFileParser;

public class ImageSettings
{
	//private Settings defaults;
	private Settings settings;
	
	public ImageSettings()
	{
		//defaults = new SettingsFileParser( getClass().getResourceAsStream( "defaults.settings" ) ).parseFile();
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
		
		//return settings.getSetting( name , defaults.getSetting( name ) );
		if( !settings.hasSetting( name ) )
		{
			System.err.println( "missing setting: " + name );
			System.exit(0);
		}
		return settings.getSetting( name );
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for( Setting setting : settings.getSettings() )
		{
			sb.append( setting.getName() + ": " + setting.getValueString() + "#" );			
		}
		sb.deleteCharAt( sb.length() - 1 ); //removing the last #
		return sb.toString();
	}
}
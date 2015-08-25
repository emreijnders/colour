package emr.colour.gui;

import javax.swing.filechooser.FileFilter;
import java.io.File;

class CustomFilter extends FileFilter
{
	private String filter;
	
	public CustomFilter( String filter )
	{
		super();
		this.filter = filter;						
	}
	
	@Override
	public boolean accept( File f )
	{
		boolean answer = false;
		if( f != null )
		{
			answer = f.isDirectory() || f.getName().endsWith( "." + filter );
		}
		return answer;
	}
	
	@Override
	public String getDescription()
	{
		return filter + " files only";
	}
}
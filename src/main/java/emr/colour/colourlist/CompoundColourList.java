package emr.colour.colourlist;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class CompoundColourList
{
	private List<String> order;
	private Map<String,ColourList> lists;
	private int bits;
	
	public CompoundColourList()
	{
		order = new ArrayList<>();
		lists = new HashMap<>();		
	}
	
	public void setOrder( List<String> order )
	{
		this.order = order;		
	}
	
	public void setBits( int bits )
	{
		this.bits = bits;
	}
	
	public void addColourList( String id , ColourList list )
	{
		lists.put( id , list );		
	}
	
	public int getBits()
	{
		return bits;
	}
	
	public List<String> getOrder()
	{
		return order;
	}
	
	public Map<String,ColourList> getLists()
	{
		return lists;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "bits: " + bits + "#" );
		sb.append( "order: " );
		for( String item : order )
		{
			sb.append( item + "," );
		}
		sb.deleteCharAt( sb.length() - 1 );
		sb.append( "#" );
		for( Map.Entry< String , ColourList > entry : lists.entrySet() )
		{
			sb.append( entry.getKey() + "#" + entry.getValue().toString() );
		}
		return sb.toString();
	}
	
	public List<Colour> getCompoundColourList()
	{
		ArrayList<Colour> list = new ArrayList<>();		
		for( String id : order )
		{			
			list.addAll( lists.get( id ).getColourList( bits ) );
		}
		return list;
	}
}
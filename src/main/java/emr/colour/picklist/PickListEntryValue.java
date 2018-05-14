package emr.colour.picklist;

import java.util.Random;

import java.util.Random;

public class PickListEntryValue implements PickListEntry
{
	private int value;
	
	public PickListEntryValue( int value )
	{
		this.value = value;
	}
	
	@Override
	public int getValue( Random random , boolean remove )
	{
		return value;
	}
}
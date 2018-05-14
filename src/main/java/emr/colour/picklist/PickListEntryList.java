package emr.colour.picklist;

import java.util.Random;

public class PickListEntryList implements PickListEntry
{
	private PickList pickList;
	
	public PickListEntryList( PickList pickList )
	{
		this.pickList = pickList;
	}
	
	@Override
	public int getValue( Random random , boolean remove )
	{
		return pickList.pick( random , remove );
	}
}
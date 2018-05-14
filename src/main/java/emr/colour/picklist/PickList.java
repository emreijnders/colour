package emr.colour.picklist;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class PickList
{
	private List<PickListEntry> pickList;

	public PickList()
	{
		pickList = new ArrayList<>();
	}
	
	public int pick( Random random ,  boolean remove )
	{
		int index = random.nextInt( pickList.size() );
		int result = 0;
		if( remove )
		{
			result = pickList.remove( index ).getValue( random , remove );
		}
		else
		{
			result = pickList.get( index ).getValue( random , remove );
		}
		return result;
	}
	
	public void addEntry( PickListEntry entry )
	{
		pickList.add( entry );
	}
	
	public boolean hasEntry()
	{
		return !pickList.isEmpty();
	}
}
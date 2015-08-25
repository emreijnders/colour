package emr.colour;

import java.util.List;
import java.util.ArrayList;
import emr.stuff.Listener;
import emr.stuff.Talker;
import emr.stuff.Message;

public class IGTalker implements Talker
{
	private List<Listener> listeners;
	
	public IGTalker()
	{
		listeners = new ArrayList<>();
	}
	
	@Override
	public void addListener( Listener listener )
	{
		listeners.add( listener );
	}
	
	@Override
	public void removeListener( Listener listener )
	{
		listeners.remove( listener );
	}
	
	@Override
	public void removeAllListeners()
	{
		listeners.clear();
	}
	
	@Override
	public void sendMessage( Message message )
	{
		for( Listener listener : listeners )
		{
			listener.receiveMessage( message );
		}
	}
}
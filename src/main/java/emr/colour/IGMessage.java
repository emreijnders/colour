package emr.colour;

import emr.stuff.Message;
import emr.stuff.MessageType;

public class IGMessage implements Message
{
	private final String type;
	private final String text;
	
	public IGMessage( IGMessageType type , String text )
	{
		this.type = type.toString();
		this.text = text;
	}
	
	@Override
	public String getText()
	{
		return text;
	}
	
	@Override
	public String getType()
	{
		return type;
	}
}
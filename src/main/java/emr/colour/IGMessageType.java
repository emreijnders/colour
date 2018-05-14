package emr.colour;

import emr.stuff.MessageType;

public enum IGMessageType implements MessageType
{
	LOAD_CCL , LOAD_SETTINGS , RELOAD , GENERATE , NEW_SETTINGS , NEW_CCL;
	
	@Override
	public String getType()
	{
		return this.toString();
	}
}
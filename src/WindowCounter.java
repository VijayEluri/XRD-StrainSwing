

public class WindowCounter
{

	private static int windowCount;
	
	public synchronized static int increaseWindowCount()
	{
		return ++windowCount;
	}
	
	public synchronized static int decreaseWindowCount()
	{
		return --windowCount;
	}
	
	
}

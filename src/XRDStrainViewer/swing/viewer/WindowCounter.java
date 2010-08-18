package XRDStrainViewer.swing.viewer;

public class WindowCounter
{

	private static int windowCount = 0;
	
	public synchronized static int increaseWindowCount()
	{
		return ++windowCount;
	}
	
	public synchronized static int decreaseWindowCount()
	{
		return --windowCount;
	}
	
	
}

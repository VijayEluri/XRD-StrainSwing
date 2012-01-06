package XRDStrainViewer.swing;

import ca.sciencestudio.process.xrd.monitor.FolderMonitor;

public class FolderMonitorService {

	private static FolderMonitor folderMonitor = new FolderMonitor(null, false);
	
	public static FolderMonitor getFolderMonitor()
	{
		return folderMonitor;
	}
	
}

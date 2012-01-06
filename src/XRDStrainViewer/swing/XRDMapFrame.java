package XRDStrainViewer.swing;

import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import XRDStrainViewer.swing.viewer.WindowCounter;
import XRDStrainViewer.swing.viewer.XRDMapViewer;

import ca.sciencestudio.process.xrd.datastructures.mapdata.ProjectData;




public class XRDMapFrame extends JFrame
{

	
	public XRDMapFrame()
	{
		buildUI(null);
	}
	
	public XRDMapFrame(ProjectData data)
	{
		buildUI(data);
	}
	
	private void buildUI(ProjectData data)
	{
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new WindowListener() {
			
			public void windowOpened(WindowEvent e)
			{
				WindowCounter.increaseWindowCount();
			}
		
			public void windowIconified(WindowEvent e){}
			
			public void windowDeiconified(WindowEvent e){}
		
			public void windowDeactivated(WindowEvent e){}
		
			public void windowClosing(WindowEvent e)
			{
				XRDMapFrame.this.setVisible(false);
				XRDMapFrame.this.dispose();
				if (WindowCounter.decreaseWindowCount() == 0)
				{
					System.exit(0);
				}
			}
		
			public void windowClosed(WindowEvent e)
			{

			}
		
			public void windowActivated(WindowEvent e){}
		});
		
		Container c = getContentPane();
		c.add( new XRDMapViewer(data, this) );
				
		pack();
		setVisible(true);
		
	}

	
	public void close()
	{
		XRDMapFrame.this.setVisible(false);
		XRDMapFrame.this.dispose();
	}
	
	
}

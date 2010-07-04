package XRDStrainViewer.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Panel;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import XRDStrainViewer.swing.viewer.WindowCounter;
import XRDStrainViewer.swing.viewer.XRDMapViewer;

import swidget.containers.SwidgetContainer;
import swidget.containers.SwidgetFrame;

import ca.sciencestudio.process.xrd.datastructures.ProcessXRDResults_ProjectData;




public class XRDMapFrame extends SwidgetFrame
{

	public static void main(String args[])
	{
		new XRDMapFrame();	
	}
	
	
	
	
	
	
	public XRDMapFrame()
	{
		buildUI(null);
	}
	
	public XRDMapFrame(ProcessXRDResults_ProjectData data)
	{
		buildUI(data);
	}
	
	private void buildUI(ProcessXRDResults_ProjectData data)
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
				XRDMapFrame.this.close();
			}
		
			public void windowClosed(WindowEvent e)
			{
				if (WindowCounter.decreaseWindowCount() == 0)
				{
					System.exit(0);
				}
			}
		
			public void windowActivated(WindowEvent e){}
		});
		
		Container c = getContentPane();
		c.add( new XRDMapViewer(data, this) );
				
		pack();
		setVisible(true);
		
	}	
	
}

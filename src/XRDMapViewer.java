import XRDStrainViewer.swing.XRDMapFrame;

import swidget.Swidget;
import swidget.icons.IconFactory;



public class XRDMapViewer
{

	
	public static void main(String args[])
	{
		Swidget.initialize();
		IconFactory.customPath = "/XRDStrainViewer/swing/icons/";
		
		new XRDMapFrame();
		
	}
	
}

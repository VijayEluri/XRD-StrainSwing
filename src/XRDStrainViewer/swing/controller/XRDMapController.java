package XRDStrainViewer.swing.controller;

import java.util.LinkedList;
import java.util.List;

import scidraw.drawing.DrawingRequest;
import scidraw.drawing.common.Spectrums;

import ca.sciencestudio.process.xrd.datastructures.ProcessXRDResults_Listener;
import ca.sciencestudio.process.xrd.datastructures.ProcessXRDResults_Map;
import ca.sciencestudio.process.xrd.datastructures.ProcessXRDResults_ProjectData;
import eventful.EventfulEnum;



public class XRDMapController extends EventfulEnum<ControllerMessage>
{


	
	private ProcessXRDResults_ProjectData	model;
	private ProcessXRDResults_Map			map;
	public DrawingRequest					dr;
	public boolean							dataLoaded	= false;
	
	public XRDMapController(ProcessXRDResults_ProjectData model)
	{	
		dr = new DrawingRequest();
		dr.maxYIntensity = 1;
		this.model = model;
	}

	public XRDMapController()
	{
		this(null);
	}
		
	public void setScale(float scale)
	{
		dr.maxYIntensity = scale;
		updateListeners(ControllerMessage.NORMAL);
	}
	
	public double getScale()
	{
		return dr.maxYIntensity;
	}
	
	
	public void setMap(ProcessXRDResults_Map map)
	{
		this.map = map;
		updateListeners(ControllerMessage.NEWMAP);
	}
	
	
	public ProcessXRDResults_Map getMap()
	{
		return map;
	}
	
	
	public ProcessXRDResults_ProjectData getModel()
	{
		return model;
	}
	
	
	public void loadData(String folder)
	{
		
		List<String> folders = new LinkedList<String>();
		folders.add(folder);

		model = new ProcessXRDResults_ProjectData(folder, Spectrums.ThermalScale());
				
		model.setListener(new ProcessXRDResults_Listener() {

			public void change()
			{
				dataLoaded = true;
				map = model.maps[0];
				updateListeners(ControllerMessage.NEWDATA);
				
			}
		});
		
		

	}

}

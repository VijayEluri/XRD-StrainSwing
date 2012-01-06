package XRDStrainViewer.swing.controller;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import XRDStrainViewer.swing.FolderMonitorService;

import scidraw.drawing.DrawingRequest;
import scidraw.drawing.common.Spectrums;

import ca.sciencestudio.process.xrd.datastructures.mapdata.ProjectData;
import ca.sciencestudio.process.xrd.datastructures.mapdata.maps.XRDMap;
import ca.sciencestudio.process.xrd.monitor.FolderMonitor;
import eventful.EventfulEnum;
import eventful.EventfulTypeListener;



public class XRDMapController extends EventfulEnum<ControllerMessage>
{


	
	private ProjectData				model;
	private XRDMap<?>				map;
	public  DrawingRequest			dr;
	
	public XRDMapController(ProjectData model)
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
	
	
	public void setMap(XRDMap map)
	{
		this.map = map;
		updateListeners(ControllerMessage.NEWMAP);
	}
	
	
	public XRDMap getMap()
	{
		return map;
	}
	
	
	public ProjectData getModel()
	{
		return model;
	}
	
	
	public void loadData(String folder)
	{
		//clear out the old model data from the folder monitor
		if (model != null) FolderMonitorService.getFolderMonitor().removeProject(model);
		
		List<String> folders = new LinkedList<String>();
		folders.add(folder);
		
		File dir = new File(folder);
		
		FolderMonitor fm = FolderMonitorService.getFolderMonitor();
		

		model = fm.requestProject(dir.getName(), dir.getParent(), false); 
		
		
		map = model.maps.get(0);
		
		model.addListener(new EventfulTypeListener<String>() {

			public void change(String message) {
				
				updateListeners(ControllerMessage.NEWDATA);
			}
		});

		updateListeners(ControllerMessage.NEWDATASET);
		

	}
	
	public boolean hasData()
	{
		return (model != null);
	}

}

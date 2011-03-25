package XRDStrainViewer.swing.controller;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import XRDStrainViewer.swing.FolderMonitorService;

import scidraw.drawing.DrawingRequest;
import scidraw.drawing.common.Spectrums;

import ca.sciencestudio.process.xrd.datastructures.ProcessXRD_Map;
import ca.sciencestudio.process.xrd.datastructures.ProcessXRD_ProjectData;
import ca.sciencestudio.process.xrd.monitor.FolderMonitor;
import eventful.EventfulEnum;
import eventful.EventfulTypeListener;



public class XRDMapController extends EventfulEnum<ControllerMessage>
{


	
	private ProcessXRD_ProjectData	model;
	private ProcessXRD_Map			map;
	public DrawingRequest			dr;
	
	public XRDMapController(ProcessXRD_ProjectData model)
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
	
	
	public void setMap(ProcessXRD_Map map)
	{
		this.map = map;
		updateListeners(ControllerMessage.NEWMAP);
	}
	
	
	public ProcessXRD_Map getMap()
	{
		return map;
	}
	
	
	public ProcessXRD_ProjectData getModel()
	{
		return model;
	}
	
	
	public void loadData(String folder)
	{
		//clear out the old model data from the folder monitor
		if (model != null) FolderMonitorService.getFolderMonitor().removeProject(model.projectName);
		
		List<String> folders = new LinkedList<String>();
		folders.add(folder);
		
		File dir = new File(folder);
		
		FolderMonitor fm = FolderMonitorService.getFolderMonitor();
		

		model = fm.requestProject(dir.getName(), dir.getParent(), false); 
		model.setSpectrum(Spectrums.ThermalScale());	
		
		
		map = model.maps[0];
		
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

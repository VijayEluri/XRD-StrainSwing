import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import peakaboo.controller.CanvasController;
import peakaboo.datatypes.Coord;
import peakaboo.datatypes.Range;
import peakaboo.datatypes.eventful.Eventful;
import peakaboo.drawing.DrawingRequest;
import peakaboo.drawing.backends.DrawingSurfaceFactory;
import peakaboo.drawing.backends.Surface;
import peakaboo.drawing.common.Spectrums;
import peakaboo.drawing.map.MapDrawing;
import peakaboo.drawing.map.painters.RasterColorMapPainter;
import peakaboo.drawing.map.painters.axis.SpectrumCoordsAxisPainter;
import peakaboo.drawing.map.palettes.AbstractPalette;
import peakaboo.drawing.map.palettes.ThermalScalePalette;
import peakaboo.drawing.painters.axis.AxisPainter;
import ca.sciencestudio.process.xrd.datastructures.ProcessXRDResults_Listener;
import ca.sciencestudio.process.xrd.datastructures.ProcessXRDResults_Map;
import ca.sciencestudio.process.xrd.datastructures.ProcessXRDResults_Pixel;
import ca.sciencestudio.process.xrd.datastructures.ProcessXRDResults_ProjectData;
import ca.sciencestudio.process.xrd.datastructures.ProcessXRDResults_StrainMap;



public class XRDMapController extends CanvasController
{

	public enum Message{
		NEWDATA,
		NEWMAP,
	}
	
	public ProcessXRDResults_ProjectData	model;
	private ProcessXRDResults_Map			map;
	public boolean							dataLoaded	= false;
		
	//drawing objects
	private MapDrawing						drawing;
	private RasterColorMapPainter			painter;
	private DrawingRequest					dr;
	private boolean							isPDF;	
	
	public XRDMapController()
	{
		super(new BufferedImage(10, 10, BufferedImage.TYPE_4BYTE_ABGR));
		
		dr = new DrawingRequest();
		dr.maxYIntensity = 1;
		drawing = new MapDrawing(null, dr);
		painter = new RasterColorMapPainter();
		drawing.setPainters(painter);
		
	}


	@Override
	protected void drawBackend(Surface backend, boolean vector)
	{
		
		if (!dataLoaded || map == null) return;
		
		//set the drawing requests dimensions for the data and the screen
		dr.dataHeight = map.height;
		dr.dataWidth = map.width;

		//get a list of colors to draw on the map
		List<Color> colors = getColorMapFromPixelMap(map, dr);

		List<AbstractPalette> palettes = new LinkedList<AbstractPalette>();
		palettes.add(new ThermalScalePalette(false, true));

		AxisPainter axis = new SpectrumCoordsAxisPainter(
				true,
				new Coord<Number>(1, 1),
				new Coord<Number>(map.width, 1),
				new Coord<Number>(1, map.height),
				new Coord<Number>(map.width, map.height),
				null,
				true,
				15,
				1000,
				palettes,
				false,
				map.name,
				true
				);

		drawing.setAxisPainters(axis);
		
		dr.drawToVectorSurface = vector;

		//set the painter and drawings data, and paint the screen
		drawing.needsMapRepaint();
		drawing.setDR(dr);
		painter.setPixels(colors);
		drawing.setContext(backend);
		drawing.draw();
	}


	@Override
	public float getUsedHeight()
	{
		// TODO Auto-generated method stub
		return dr.imageHeight;
	}


	@Override
	public float getUsedWidth()
	{
		// TODO Auto-generated method stub
		return dr.imageWidth;
	}


	@Override
	public void setOutputIsPDF(boolean isPDF)
	{
		this.isPDF = isPDF;
	}
	
	public void setDimensions(Coord<Integer> dims)
	{
		dr.imageWidth = dims.x;
		dr.imageHeight = dims.y;
	}
	
	public void setScale(float scale)
	{
		dr.maxYIntensity = scale;
		updateListeners();
	}
	
	public double getScale()
	{
		return dr.maxYIntensity;
	}
	
	public void setMap(ProcessXRDResults_Map map)
	{
		this.map = map;
		updateListeners(Message.NEWMAP);
	}
	

	public Coord<Integer> getMapCoordinateAtPoint(float x, float y)
	{

		if (drawing == null) return null;

		Coord<Range<Float>> borders = drawing.calcAxisBorders();
		float topOffset, leftOffset;
		topOffset = borders.y.start;
		leftOffset = borders.x.start;

		float mapX, mapY;
		mapX = x - leftOffset;
		mapY = y - topOffset;

		Coord<Float> mapSize = drawing.calcMapSize();
		float percentX, percentY;
		percentX = mapX / mapSize.x;
		percentY = mapY / mapSize.y;

		percentY = 1.0f - percentY;

		int indexX = (int) Math.floor(dr.dataWidth * percentX);
		int indexY = (int) Math.floor(dr.dataHeight * percentY);

		return new Coord<Integer>(indexX, indexY);

	}
	
	
	public ProcessXRDResults_Map getMap()
	{
		return map;
	}
	
	private List<Color> getColorMapFromPixelMap(ProcessXRDResults_Map map, DrawingRequest dr)
	{
		List<Color> colorMap = new ArrayList<Color>();

		for (int i = 0; i < map.height * map.width; i++)
		{
			colorMap.add(Color.black);
		}

		Color c;

		if (map instanceof ProcessXRDResults_StrainMap)
		{
			((ProcessXRDResults_StrainMap) map).setSpectrumRange(dr.maxYIntensity);

			for (int i = 0; i < map.height * map.width; i++)
			{
				ProcessXRDResults_Pixel pixel = map.getPixel(i);
				if (pixel == null) continue;
				int index = pixel.x + (pixel.y * map.width);
				c = new Color(pixel.r, pixel.g, pixel.b);
				colorMap.set(index, c);
			}

		}
		else
		{
			for (int i = 0; i < map.height * map.width; i++)
			{
				ProcessXRDResults_Pixel pixel = map.getPixel(i);
				if (pixel == null) continue;
				int index = pixel.x + (pixel.y * map.width);
				c = new Color(pixel.r, pixel.g, pixel.b);
				colorMap.set(index, c);
			}

		}


		return colorMap;
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
				updateListeners(Message.NEWDATA);
				
			}
		});
		
		

	}

}

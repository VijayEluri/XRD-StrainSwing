package XRDStrainViewer.swing.viewer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ca.sciencestudio.process.xrd.datastructures.ProcessXRD_Map;
import ca.sciencestudio.process.xrd.datastructures.ProcessXRD_Pixel;
import ca.sciencestudio.process.xrd.datastructures.ProcessXRD_StrainMap;

import XRDStrainViewer.swing.controller.XRDMapController;


import scidraw.drawing.DrawingRequest;
import scidraw.drawing.backends.Surface;
import scidraw.drawing.map.MapDrawing;
import scidraw.drawing.map.painters.RasterColorMapPainter;
import scidraw.drawing.map.painters.axis.SpectrumCoordsAxisPainter;
import scidraw.drawing.map.palettes.AbstractPalette;
import scidraw.drawing.map.palettes.ThermalScalePalette;
import scidraw.drawing.painters.axis.AxisPainter;
import scidraw.swing.GraphicsPanel;
import scitypes.Coord;



public class MapGraphicsPanel extends GraphicsPanel
{

	//drawing objects
	private MapDrawing						drawing;
	private RasterColorMapPainter			painter;
	
	private XRDMapController 				controller;
	
	public MapGraphicsPanel(XRDMapController controller)
	{
		drawing = new MapDrawing(null, controller.dr);
		painter = new RasterColorMapPainter();
		drawing.setPainters(painter);
		
		this.controller = controller;
	}
	
	@Override
	protected void drawGraphics(Surface backend, boolean vector)
	{
		ProcessXRD_Map map = controller.getMap();
		DrawingRequest dr = controller.dr;
		
		if (!controller.hasData() || map == null) return;
		
		//set the drawing requests dimensions for the data and the screen
		dr.imageWidth = getWidth();
		dr.imageHeight = getHeight();
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
				2,
				true
				);

		if (controller.getMap() instanceof ProcessXRD_StrainMap) {
			drawing.setAxisPainters(axis);
		} else {
			drawing.clearAxisPainters();
		}
		
		dr.drawToVectorSurface = vector;

		//set the painter and drawings data, and paint the screen
		drawing.needsMapRepaint();
		drawing.setDrawingRequest(dr);
		painter.setPixels(colors);
		drawing.setContext(backend);
		drawing.draw();
	}


	@Override
	public float getUsedHeight()
	{
		return drawing.calcTotalSize().y;		
	}


	@Override
	public float getUsedWidth()
	{
		return drawing.calcTotalSize().x;
	}

	private List<Color> getColorMapFromPixelMap(ProcessXRD_Map map, DrawingRequest dr)
	{
		List<Color> colorMap = new ArrayList<Color>();

		for (int i = 0; i < map.height * map.width; i++)
		{
			colorMap.add(Color.black);
		}

		Color c;
		
		int maxInd = Math.min(map.dataIndex, map.height * map.width);

		if (map instanceof ProcessXRD_StrainMap)
		{
			((ProcessXRD_StrainMap) map).setSpectrumRange(0, dr.maxYIntensity);

			for (int i = 0; i < maxInd; i++)
			{
				ProcessXRD_Pixel pixel = map.getPixel(i);
				if (pixel == null) continue;
				int index = pixel.x + (pixel.y * map.width);
				c = new Color(pixel.r, pixel.g, pixel.b);
				colorMap.set(index, c);
			}

		}
		else
		{
			for (int i = 0; i < maxInd; i++)
			{
				ProcessXRD_Pixel pixel = map.getPixel(i);
				if (pixel == null) continue;
				int index = pixel.x + (pixel.y * map.width);
				c = new Color(pixel.r, pixel.g, pixel.b);
				colorMap.set(index, c);
			}

		}


		return colorMap;
	}
	
	
}

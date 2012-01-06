package XRDStrainViewer.swing.viewer;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;

import ca.sciencestudio.process.xrd.datastructures.mapdata.Pixel;
import ca.sciencestudio.process.xrd.datastructures.mapdata.PixelData;
import ca.sciencestudio.process.xrd.datastructures.mapdata.maps.StrainMap;
import ca.sciencestudio.process.xrd.datastructures.mapdata.maps.XRDMap;
import fava.functionable.FList;

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
import scitypes.Bounds;
import scitypes.Coord;



public class MapGraphicsPanel extends GraphicsPanel
{

	//drawing objects
	private MapDrawing						drawing;
	private RasterColorMapPainter			painter;
	
	private XRDMapController 				controller;
	
	public MapGraphicsPanel(final XRDMapController controller, final JLabel status)
	{
		drawing = new MapDrawing(null, controller.dr);
		painter = new RasterColorMapPainter();
		drawing.setPainters(painter);
		
		this.controller = controller;
		
		this.addMouseListener(new MouseListener() {
			
			public void mouseReleased(MouseEvent e) {}
			
			public void mousePressed(MouseEvent e) {}
			
			public void mouseExited(MouseEvent e) {}
			
			public void mouseEntered(MouseEvent e) {}
			
			public void mouseClicked(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				
				Coord<Integer> coord = drawing.getMapCoordinateAtPoint(x, y, true);
				
				if (coord == null || controller.getModel() == null){
					status.setText("X: -, Y: -, Filename: -");
					return;
				}

				
				int datax, datay;
				datay = coord.y; //controller.getModel().scanHeight - coord.y - 1;
				datax = coord.x;
				
				PixelData pixel = controller.getModel().pixels.get(datax + datay * controller.getModel().scanWidth);
				if (pixel == null) {
					status.setText("X: -, Y: -, Filename: -");
					return;
				}
				
				String fn = pixel.sourceImage;
				status.setText("X: " + datax + ", Y: " + datay + ", Filename: " + (fn == null ? "-" : fn));
				
			}
		});
		
	}
	
	@Override
	protected void drawGraphics(Surface backend, boolean vector)
	{
		XRDMap<?> map = controller.getMap();
		DrawingRequest dr = controller.dr;
		
		if (!controller.hasData() || map == null) return;
		
		//set the drawing requests dimensions for the data and the screen
		dr.imageWidth = getWidth();
		dr.imageHeight = getHeight();
		
		dr.dataHeight = map.height;
		dr.dataWidth = map.width;
		dr.uninterpolatedHeight = map.height;
		dr.uninterpolatedWidth = map.width;

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

		if (controller.getMap() instanceof StrainMap) {
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

	private List<Color> getColorMapFromPixelMap(XRDMap<?> map, DrawingRequest dr)
	{
		List<Color> colorMap = new FList<Color>();

		for (int i = 0; i < map.height * map.width; i++)
		{
			colorMap.add(Color.black);
		}

		Color c;
		
		if (map instanceof StrainMap)
		{
			Bounds<Float> spectrumBounds = new Bounds<Float>(0f, dr.maxYIntensity);

			for (int i = 0; i < map.size(); i++)
			{
				Pixel pixel = map.getPixel(i, spectrumBounds);
				if (pixel == null) continue;
				int index = pixel.x + (pixel.y * map.width);
				c = new Color(pixel.r, pixel.g, pixel.b);
				colorMap.set(index, c);
			}

		}
		else
		{
			for (int i = 0; i < map.size(); i++)
			{
				Pixel pixel = map.getPixel(i, null);
				if (pixel == null) continue;
				int index = pixel.x + (pixel.y * map.width);
				c = new Color(pixel.r, pixel.g, pixel.b);
				colorMap.set(index, c);
			}

		}


		return colorMap;
	}
	
	
}

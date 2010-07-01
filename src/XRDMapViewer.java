import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.sciencestudio.process.xrd.controllers.ProcessXRDResults_Controller;
import ca.sciencestudio.process.xrd.controllers.ProcessXRDResults_ImageDataController;
import ca.sciencestudio.process.xrd.datastructures.ProcessXRDResults_Listener;
import ca.sciencestudio.process.xrd.datastructures.ProcessXRDResults_Map;
import ca.sciencestudio.process.xrd.datastructures.ProcessXRDResults_Pixel;
import ca.sciencestudio.process.xrd.datastructures.ProcessXRDResults_ProjectData;
import ca.sciencestudio.process.xrd.datastructures.ProcessXRDResults_StrainMap;
import fava.FList;
import fava.Fn;
import fava.FunctionEach;
import fava.FunctionMap;
import fava.Functions;

import peakaboo.datatypes.Coord;
import peakaboo.datatypes.Spectrum;
import peakaboo.datatypes.eventful.PeakabooMessageListener;
import peakaboo.datatypes.eventful.PeakabooSimpleListener;
import peakaboo.drawing.DrawingRequest;
import peakaboo.drawing.backends.DrawingSurfaceFactory;
import peakaboo.drawing.backends.graphics2d.ImageBuffer;
import peakaboo.drawing.common.Spectrums;
import peakaboo.drawing.map.MapDrawing;
import peakaboo.drawing.map.painters.MapPainter;
import peakaboo.drawing.map.painters.MapTechniqueFactory;
import peakaboo.drawing.map.painters.RasterColorMapPainter;
import peakaboo.drawing.map.painters.axis.SpectrumCoordsAxisPainter;
import peakaboo.drawing.map.palettes.AbstractPalette;
import peakaboo.drawing.map.palettes.ThermalScalePalette;
import peakaboo.drawing.painters.PainterData;
import peakaboo.drawing.painters.axis.AxisPainter;
import peakaboo.ui.swing.icons.IconSize;
import peakaboo.ui.swing.plotting.PeakabooContainer;
import peakaboo.ui.swing.widgets.ImageButton;
import peakaboo.ui.swing.widgets.ImageButton.Layout;
import peakaboo.ui.swing.widgets.pictures.SavePicture;



public class XRDMapViewer extends JPanel
{

	//map drawing data

	private JPanel							targetPanel;
	
	private XRDMapController				controller;
	private PeakabooContainer				container;
	
	
	//widgets and the listeners which will have to be disabled when handling updates
	private JSpinner 						scaleSpinner;
	private ChangeListener 					scaleChange;



	public XRDMapViewer(ProcessXRDResults_ProjectData data, PeakabooContainer container)
	{
		controller = new XRDMapController();
		controller.model = data;
		if (data != null)
		{
			controller.dataLoaded = true;
			controller.setMap(controller.model.maps[0]);
		}
		
		this.container = container;
		
		buildUI();
		
		makeTitle();
		
	}
		

	private void buildUI()
	{	
		
		setPreferredSize(new Dimension(500, 500));

		setLayout(new BorderLayout());
		
		
		targetPanel = new JPanel() {

			public void paintComponent(Graphics g)
			{
				g.setColor(Color.white);
				g.fillRect(0, 0, getWidth(), getHeight());
				controller.setDimensions(new Coord<Integer>(getWidth(), getHeight()));
				controller.draw(g);
			}

		};
		
		add(targetPanel, BorderLayout.CENTER);
		
		add(buildToolbar(), BorderLayout.NORTH);
		
		add(buildStatusbar(), BorderLayout.SOUTH);
		
		buildMenu();
		
		
		controller.addListener(new PeakabooMessageListener() {
			
			public void change()
			{
				updateUI();
			}

			public void change(Object message)
			{
				
				if (message instanceof XRDMapController.Message)
				{
					
					XRDMapController.Message m = (XRDMapController.Message)message;
					switch (m)
					{
						case NEWMAP:
							break;
						case NEWDATA:
							
							buildMenu();
							
							break;
					}
					
					updateUI();
					
				}
			}
			
			private void updateUI()
			{
				makeTitle();
				repaint();
				
				scaleSpinner.removeChangeListener(scaleChange);
				scaleSpinner.setValue(controller.getScale());
				scaleSpinner.addChangeListener(scaleChange);
			}
			
		});
		
				
	}
	
	
	public JToolBar buildToolbar()
	{
		JToolBar toolbar = new JToolBar();
		ImageButton newWindow = new ImageButton("new-window", "New Window", Layout.IMAGE, IconSize.TOOLBAR_SMALL);
		ImageButton picture = new ImageButton("picture", "Save Picture", Layout.IMAGE, IconSize.TOOLBAR_SMALL);
		
		newWindow.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				new XRDMapFrame(controller.model);
			}
		});
		
		picture.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				if (controller.getMap() != null) new SavePicture(container, controller, "");
			}
		});
		
		if (! container.isApplet()) toolbar.add(newWindow);
		toolbar.add(picture);
		
		return toolbar;
	}
	
	
	public JPanel buildStatusbar()
	{
		
		JPanel statusbar = new JPanel();
		statusbar.setLayout(new BorderLayout());
		
		statusbar.add(scaleControl(), BorderLayout.WEST);
		
		return statusbar;
		
	}
	
	public JPanel scaleControl()
	{
		JPanel scale = new JPanel();
		scale.setLayout(new BorderLayout());
		
		JLabel scaleLabel = new JLabel("Scale ");
		scale.add(scaleLabel, BorderLayout.WEST);
		
		scaleSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1000.0, 0.1));
		scale.add(scaleSpinner, BorderLayout.EAST);
		
		scaleChange = new ChangeListener() {
			
			public void stateChanged(ChangeEvent e)
			{
				
				controller.setScale(  ((Double)scaleSpinner.getValue()).floatValue()  );
			}
		};
		
		scaleSpinner.addChangeListener(scaleChange);
		
		return scale;
	}
	
	
	private void buildMenu()
	{
		JMenuBar menubar = new JMenuBar();

		final JMenu filemenu = new JMenu("File");
		final JMenu mapsmenu = new JMenu("Maps");


		JMenuItem open = new JMenuItem("Open Data...");
		open.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				controller.loadData("/home/nathaniel/Projects/XRDStrainViewer/Gaussian");
			}
		});
		filemenu.add(open);
		
		filemenu.addSeparator();

		JMenuItem close = new JMenuItem("Close Window");
		close.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				container.close();
			}
		});
		filemenu.add(close);
		
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});
		filemenu.add(exit);
		

		menubar.add(filemenu);
		
		
		if (controller.model != null)
		{
					
			//get the maps as a list
			FList<ProcessXRDResults_Map> maps = Fn.map(controller.model.maps, Functions.<ProcessXRDResults_Map> id());

			//sort the maps by string name field
			Fn.sortBy(maps, 
				
			//simple string comparitor
			new Comparator<String>() {
				
				public int compare(String o1, String o2)
				{
					return o1.compareTo(o2);
				}
			},

			//map the map to the map name
			new FunctionMap<ProcessXRDResults_Map, String>() {

				public String f(ProcessXRDResults_Map map)
				{
					return map.name;
				}
			});

			
			//for each map, create an entry in the menu
			maps.each(new FunctionEach<ProcessXRDResults_Map>() {

				public void f(final ProcessXRDResults_Map map)
				{
					JMenuItem item = new JMenuItem(map.name);
					item.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e)
						{
							controller.setMap(map);
						}
					});
					mapsmenu.add(item);

				}
			});
			
			menubar.add(mapsmenu);
			
		}
		
		container.setJMenuBar(menubar);
	}
	
	
	private void makeTitle()
	{
		String title;
		title = "XRD Crystal Viewer";
		if (controller.model != null)
		{
			title += ": " + controller.model.filePrefix;
		}
		
		if (controller.getMap() != null)
		{
			title += " - " + controller.getMap().name;
		}
		
		container.setTitle(title);
			
	}



	public static void main(String args[])
	{
		new XRDMapFrame();
	}


}

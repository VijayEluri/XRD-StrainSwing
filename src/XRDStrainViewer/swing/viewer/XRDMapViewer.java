package XRDStrainViewer.swing.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import XRDStrainViewer.swing.XRDMapFrame;
import XRDStrainViewer.swing.controller.ControllerMessage;
import XRDStrainViewer.swing.controller.XRDMapController;

import ca.sciencestudio.process.xrd.datastructures.ProcessXRDResults_Map;
import ca.sciencestudio.process.xrd.datastructures.ProcessXRDResults_ProjectData;
import eventful.EventfulEnumListener;
import fava.Fn;
import fava.Functions;
import fava.lists.FList;
import fava.signatures.FunctionEach;
import fava.signatures.FunctionMap;

import scidraw.swing.GraphicsPanel;
import scidraw.swing.SavePicture;
import swidget.containers.SwidgetContainer;
import swidget.icons.IconFactory;
import swidget.icons.IconSize;
import swidget.widgets.ImageButton;
import swidget.widgets.ToolbarImageButton;
import swidget.widgets.ImageButton.Layout;



public class XRDMapViewer extends JPanel
{

	//map drawing data

	private GraphicsPanel					targetPanel;
	
	private XRDMapController				controller;
	private SwidgetContainer				container;
	
	
	//widgets and the listeners which will have to be disabled when handling updates
	private JSpinner 						scaleSpinner;
	private ChangeListener 					scaleChange;



	public XRDMapViewer(ProcessXRDResults_ProjectData data, SwidgetContainer container)
	{
		controller = new XRDMapController(data);
		if (data != null)
		{
			controller.dataLoaded = true;
			controller.setMap(controller.getModel().maps[0]);
		}
		
		this.container = container;
		
		buildUI();
		
		makeTitle();
		
	}
		

	private void buildUI()
	{	
		
		setPreferredSize(new Dimension(500, 500));

		setLayout(new BorderLayout());
		
		
		targetPanel = new MapGraphicsPanel(controller);
		
		
		add(targetPanel, BorderLayout.CENTER);
		
		add(buildToolbar(), BorderLayout.NORTH);
		
		add(buildStatusbar(), BorderLayout.SOUTH);
		
		buildMenu();
		
		
		controller.addListener(new EventfulEnumListener<ControllerMessage>() {
			
			public void change(ControllerMessage message)
			{
				
				switch (message)
				{
					case NEWMAP:
						break;
					case NEWDATA:
						
						buildMenu();
						
						break;
					case NORMAL:
						break;
				}
				
				updateUI();
				
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
		toolbar.setFloatable(false);
		
		ImageButton newWindow = new ToolbarImageButton("window-new", "New Window", "Opens a new window to view data in", false);
		ImageButton openData = new ToolbarImageButton("document-open", "Open Data", "Opens a new data set for viewing", false);
		ImageButton picture = new ToolbarImageButton("picture", "Save Picture", "Save the currently displayed data as a picture", true);
		
		newWindow.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				actionNewWindow();
			}
		});
		
		picture.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				actionSavePicture();
			}
		});
		
		openData.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				actionOpenData();
			}
		});
		
		if (! container.isApplet()) toolbar.add(newWindow);
		toolbar.add(openData);
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


		JMenuItem newWindow = new JMenuItem("New Window", IconFactory.getMenuIcon("window-new"));
		newWindow.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				actionNewWindow();
			}
		});
		
		filemenu.add(newWindow);
		
		filemenu.addSeparator();
		
		JMenuItem open = new JMenuItem("Open Data...", IconFactory.getMenuIcon("document-open"));
		open.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				actionOpenData();
			}
		});
		filemenu.add(open);
		
		JMenuItem save = new JMenuItem("Save Picture", IconFactory.getMenuIcon("picture"));
		save.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				actionSavePicture();
			}
		});
		filemenu.add(save);
		
		filemenu.addSeparator();

		JMenuItem close = new JMenuItem("Close Window");
		close.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				container.close();
			}
		});
		filemenu.add(close);
		
		JMenuItem exit = new JMenuItem("Exit", IconFactory.getMenuIcon("window-close"));
		exit.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});
		filemenu.add(exit);
		

		menubar.add(filemenu);
		
		
		if (controller.getModel() != null)
		{
					
			//get the maps as a list
			FList<ProcessXRDResults_Map> maps = Fn.map(controller.getModel().maps, Functions.<ProcessXRDResults_Map> id());

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
				}
			);

			
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
		if (controller.getModel() != null)
		{
			title += ": " + controller.getModel().filePrefix;
		}
		
		if (controller.getMap() != null)
		{
			title += " - " + controller.getMap().name;
		}
		
		container.setTitle(title);
			
	}



	
	
	
	
	
	
	
	public void actionNewWindow()
	{
		new XRDMapFrame(controller.getModel());
	}
	
	public void actionSavePicture()
	{
		if (controller.getMap() != null) new SavePicture(container, targetPanel, "");
	}
	
	public void actionOpenData()
	{
		controller.loadData("/home/nathaniel/Projects/XRDStrainViewer/Gaussian");
	}
	
	
	
	
	
	
	public static void main(String args[])
	{
		new XRDMapFrame();
	}


}

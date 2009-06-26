/*
 * org.openmicroscopy.shoola.agents.imviewer.view.ControlPane
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.imviewer.view;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.actions.ColorModelAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ColorPickerAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ViewerAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ui.ChannelButton;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.slider.OneKnobSlider;
import org.openmicroscopy.shoola.util.ui.slider.TwoKnobsSlider;
import pojos.ChannelData;

/** 
 * Presents variable controls.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 			<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ControlPane
    extends JPanel
    implements ActionListener, ChangeListener, MouseWheelListener, 
    PropertyChangeListener
{

    /** The description of a z-sections selection slider. */
    private static final String 	Z_SLIDER_DESCRIPTION = 
    								"Select a z-section.";

    /** The description of a timepoint selection slider. */
    private static final String 	T_SLIDER_DESCRIPTION = 
    								"Select a timepoint.";
    
    /** The description of a timepoint selection slider. */
    private static final String 	LITEIME_SLIDER_DESCRIPTION = 
    								"Select .";
    
    /** The description of a magnification selection slider. */
    private static final String 	RATIO_SLIDER_DESCRIPTION = "Select the " +
                            "magnification factor of an image composing " +
                            "the grid.";

    /** The description of a magnification selection slider. */
    private static final String 	ZOOM_SLIDER_DESCRIPTION = "Select the " +
                            "magnification factor of the image.";
    
    /** The description of a z-sections selection slider. */
    private static final String 	PROJECTION_SLIDER_DESCRIPTION = 
                            "Select the interval of z-sections to project.";
    
    /** The tipString of the {@link #zSlider}. */
    private static final String 	Z_SLIDER_TIPSTRING = "Z";

    /** The tipString of the {@link #tSlider}. */
    private static final String 	T_SLIDER_TIPSTRING = "T";
    
    /** The tipString of the {@link #lifetimeSlider}. */
    private static final String 	LIFETIME_SLIDER_TIPSTRING = "L";
    
    /** The maximum height of a magnification slider. */
    private static final int		SLIDER_HEIGHT = 100;
    
    /** Action command id. */
	private static final int		FREQUENCY = 0;
	
	/** Action command id. */
	private static final int		TYPE = 1;
	
    /** Default text describing the compression check box.  */
    private static final String		PROJECTION_DESCRIPTION = 
    				"Select the type of projection.";
    
    /** The value after which no ticks are displayed. */
    private static final int		MAX_NO_TICKS = 10;
    
    /** Dimension of the box between the channel buttons. */
    private static final Dimension VBOX = new Dimension(1, 10);
    
    /** The type of projections supported. */
	private static final Map<Integer, String>	projections;
	
    static {
    	projections = new LinkedHashMap<Integer, String>();
    	projections.put(ImViewer.MAX_INTENSITY, "Maximum");
    	projections.put(ImViewer.MEAN_INTENSITY, "Mean");
    	//projections.put(ImViewer.SUM_INTENSITY, "Sum");
    }

    /** Reference to the Control. */
    private ImViewerControl 		controller;
    
    /** Reference to the Model. */
    private ImViewerModel   		model;
    
    /** Reference to the View. */
    private ImViewerUI      		view;
    
    /** Slider to select the lifetime bin. */
    private OneKnobSlider			lifetimeSlider;
    
    /** Slider to select the z-section. */
    private OneKnobSlider			zSlider;
    
    /** Slider to select the timepoint. */
    private OneKnobSlider			tSlider;
    
    /** Slider to select the z-section. */
    private OneKnobSlider			zSliderGrid;
    
    /** Slider to select the timepoint. */
    private OneKnobSlider			tSliderGrid;
    
    /** Slider to select the timepoint. */
    private OneKnobSlider			tSliderProjection;
    
    /** Slider to select the z-sections interval to project. */
    private TwoKnobsSlider			projectionRange;
    
    /** Slider to set the magnification factor of an image of the grid. */
    private OneKnobSlider			gridRatioSlider;
    
    /** Slider to set the magnification factor of the image. */
    private OneKnobSlider			ratioSlider;
    
    /** Slider to set the magnification factor of the image. */
    private OneKnobSlider			projectionRatioSlider;
    
    /** One  {@link ChannelButton} per channel. */
    private List<ChannelButton>		channelButtons;

    /** One  {@link ChannelButton} per channel. */
    private List<ChannelButton>		channelButtonsGrid;
    
    /** One  {@link ChannelButton} per channel. */
    private List<ChannelButton>		channelButtonsProjection;
   
    /** Button to play movie across channel. */
    private JButton         		channelMovieButton;
    
    /** Button to select the color model. */
    private JButton         		colorModelButton;
    
    /** Button to select the color model. */
    private JButton         		colorModelButtonGrid;
    
    /** Button to select the color model. */
    private JButton         		colorModelButtonProjection;
    
    /** Button to bring up the color picker. */
    private JButton         		colorPickerButton;
    
    /** Button to paint some textual information on top of the grid image. */
    private JToggleButton			textVisibleButton;
    
    /** Button to play movie across T. */
    private JButton					playTMovie;
    
    /** Button to play mvoie across lifetime bin. */
    private JButton					playLifetimeMovie;
    
    /** Button to play movie across T displayed in the split view. */
    private JButton					playTMovieGrid;
    
    /** Button to play movie across T. */
    private JButton					playZMovie;
    
    /** Button to play movie across T displayed in the split view. */
    private JButton					playZMovieGrid;
    
    /** Button to bring up the color picker. */
    private JButton         		projectionProject;

    /** The type of supported projections. */
    private JComboBox				projectionTypesBox;
    
	/** The type of projection. */
	private Map<Integer, Integer> 	projectionTypes;

    /** Sets the stepping for the mapping. */
    private JSpinner			   	projectionFrequency;
    
    /** Helper reference. */
    private IconManager     		icons;
    

    /**
     * Sets the selected plane.
     * 
     * @param z The selected z-section.
     * @param t The selected timepoint.
     */
    private void setSelectedXYPlane(int z, int t)
    {
    	int bin = -1;
    	if (model.isLifetime()) bin = lifetimeSlider.getValue();
    	controller.setSelectedXYPlane(z, t, bin);
    }
    
    /**
     * Handles the event when the wheel is moved over the {@link #zSlider}
     * or {@link #zSliderGrid}.
     * 
     * @param e The event to handle.
     */
    private void mouseWheelMovedZ(MouseWheelEvent e)
    {
        boolean up = true;
        if (e.getWheelRotation() > 0) up = false;
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int v = model.getDefaultZ()-e.getWheelRotation();
            if (up) {
                if (v <= model.getMaxZ())
                    setSelectedXYPlane(v,  model.getDefaultT());
            } else { //moving down
                if (v >= 0)
                    setSelectedXYPlane(v,  model.getDefaultT());
            }
        } else {
     
        }
    }
    
    /**
     * Handles the event when the wheel is moved over the {@link #tSlider}
     * or {@link #tSliderGrid}.
     * 
     * @param e The event to handle.
     */
    private void mouseWheelMovedT(MouseWheelEvent e)
    {
        boolean up = true;
        if (e.getWheelRotation() > 0) up = false;
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int v = model.getDefaultT()-e.getWheelRotation();
            if (up) {
                if (v <= model.getMaxT())
                    setSelectedXYPlane(model.getDefaultZ(), v);
            } else { //moving down
                if (v >= 0)
                    setSelectedXYPlane(model.getDefaultZ(), v);
            }
        } else {
            
        }
    }
    
    /**
     * Handles the event when the wheel is moved over the {@link #tSlider}
     * or {@link #tSliderGrid}.
     * 
     * @param e The event to handle.
     */
    private void mouseWheelMovedLifetime(MouseWheelEvent e)
    {
        boolean up = true;
        if (e.getWheelRotation() > 0) up = false;
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int v = model.getDefaultT()-e.getWheelRotation();
            if (up) {
                if (v <= model.getMaxLifetimeBin())
                    setSelectedXYPlane(model.getDefaultZ(), 
                    		model.getDefaultT());
            } else { //moving down
                if (v >= 0)
                    setSelectedXYPlane(model.getDefaultZ(), 
                    		model.getDefaultT());
            }
        } else {
            
        }
    }
    
    /**
     * Returns the description of the current color model.
     * 
     * @param model The color model.
     * @return See above.
     */
    private String getColorModelDescription(String model)
    {
    	if (ImViewer.GREY_SCALE_MODEL.equals(model))
            return ColorModelAction.DESCRIPTION_GREY_SCALE;
        else if (ImViewer.RGB_MODEL.equals(model))
        	return ColorModelAction.DESCRIPTION_RGB;
        return null;
    }
    
    /**
     * Returns the icon corresponding to the current color model.
     * 
     * @param model The color model.
     * @return See above.
     */
    private Icon getColorModelIcon(String model)
    {
        if (ImViewer.GREY_SCALE_MODEL.equals(model))
            return icons.getIcon(IconManager.GRAYSCALE);
        else if (ImViewer.RGB_MODEL.equals(model))
            return icons.getIcon(IconManager.RGB);
        return null;
    }

    /** Initializes the components composing the display. */
    private void initComponents()
    {
    	channelButtons = new ArrayList<ChannelButton>();
    	channelButtonsGrid = new ArrayList<ChannelButton>();
    	channelButtonsProjection = new ArrayList<ChannelButton>();
    	projectionRange = new TwoKnobsSlider(0, 1, 0, 1);
    	projectionRange.setOrientation(TwoKnobsSlider.VERTICAL);
    	projectionRange.setEnabled(false);
    	projectionRange.setToolTipText(PROJECTION_SLIDER_DESCRIPTION);
    	
        zSlider = new OneKnobSlider(OneKnobSlider.VERTICAL, 0, 1, 0);
        zSlider.setEnabled(false);
        tSlider = new OneKnobSlider(OneKnobSlider.HORIZONTAL, 0, 1, 0);
        tSlider.setEnabled(false);
        zSliderGrid = new OneKnobSlider(OneKnobSlider.VERTICAL, 0, 1, 0);
        zSliderGrid.setEnabled(false);
        tSliderGrid = new OneKnobSlider(OneKnobSlider.HORIZONTAL, 0, 1, 0);
        tSliderGrid.setEnabled(false);
       
        tSliderProjection = new OneKnobSlider(OneKnobSlider.HORIZONTAL, 0, 1, 
        		0);
        tSliderProjection.setEnabled(false);
        lifetimeSlider = new OneKnobSlider(OneKnobSlider.HORIZONTAL, 0, 1, 0);
        lifetimeSlider.setEnabled(false);
        
        IconManager icons = IconManager.getInstance();
        gridRatioSlider = new OneKnobSlider(OneKnobSlider.VERTICAL, 1, 10, 5);
        gridRatioSlider.setEnabled(true);
        gridRatioSlider.setShowArrows(true);
        gridRatioSlider.setToolTipText(RATIO_SLIDER_DESCRIPTION);
        gridRatioSlider.setArrowsImageIcon(
        				icons.getImageIcon(IconManager.RATIO_MAX), 
        				icons.getImageIcon(IconManager.RATIO_MIN));
        ratioSlider = new OneKnobSlider(OneKnobSlider.VERTICAL, 
        					ZoomAction.MIN_ZOOM_INDEX, 
        					ZoomAction.MAX_ZOOM_INDEX, 
        					ZoomAction.DEFAULT_ZOOM_INDEX);
        ratioSlider.setEnabled(true);
        ratioSlider.setShowArrows(true);
        ratioSlider.setToolTipText(ZOOM_SLIDER_DESCRIPTION);
       
        ratioSlider.setArrowsImageIcon(
        		icons.getImageIcon(IconManager.RATIO_MAX), 
        		icons.getImageIcon(IconManager.RATIO_MIN));
        
        projectionRatioSlider = new OneKnobSlider(OneKnobSlider.VERTICAL, 
				ZoomAction.MIN_ZOOM_INDEX, 
				ZoomAction.MAX_ZOOM_INDEX, 
				ZoomAction.DEFAULT_ZOOM_INDEX);
        projectionRatioSlider.setEnabled(true);
        projectionRatioSlider.setShowArrows(true);
        projectionRatioSlider.setToolTipText(ZOOM_SLIDER_DESCRIPTION);

        projectionRatioSlider.setArrowsImageIcon(
        		icons.getImageIcon(IconManager.RATIO_MAX), 
        		icons.getImageIcon(IconManager.RATIO_MIN));

        channelMovieButton = new JButton(
                controller.getAction(ImViewerControl.CHANNEL_MOVIE));
        UIUtilities.unifiedButtonLookAndFeel(channelMovieButton);
        colorModelButton = new JButton();
        UIUtilities.unifiedButtonLookAndFeel(colorModelButton);
        colorModelButton.addActionListener(controller);
        colorModelButtonGrid = new JButton();
        UIUtilities.unifiedButtonLookAndFeel(colorModelButtonGrid);
        colorModelButtonGrid.addActionListener(controller);
        
        colorModelButtonProjection = new JButton();
        UIUtilities.unifiedButtonLookAndFeel(colorModelButtonProjection);
        colorModelButtonProjection.addActionListener(controller);
        
        ViewerAction a = controller.getAction(ImViewerControl.COLOR_PICKER);
        colorPickerButton = new JButton(a);
        colorPickerButton.addMouseListener((ColorPickerAction) a);
        UIUtilities.unifiedButtonLookAndFeel(colorPickerButton);
        textVisibleButton = new JToggleButton();
        textVisibleButton.setSelected(model.isTextVisible());
        textVisibleButton.setAction(
        		controller.getAction(ImViewerControl.TEXT_VISIBLE));
        playTMovie = new JButton(
        			controller.getAction(ImViewerControl.PLAY_MOVIE_T));
        UIUtilities.unifiedButtonLookAndFeel(playTMovie);
        playTMovieGrid = new JButton(
    			controller.getAction(ImViewerControl.PLAY_MOVIE_T));
        UIUtilities.unifiedButtonLookAndFeel(playTMovieGrid);
        playZMovie = new JButton(
    			controller.getAction(ImViewerControl.PLAY_MOVIE_Z));
	    UIUtilities.unifiedButtonLookAndFeel(playZMovie);
	    
	    playZMovieGrid = new JButton(
				controller.getAction(ImViewerControl.PLAY_MOVIE_Z));
	    UIUtilities.unifiedButtonLookAndFeel(playZMovieGrid);
	    
	    projectionProject = new JButton(
	    		controller.getAction(ImViewerControl.PROJECTION_PROJECT));
	    
	    projectionFrequency = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
	    JComponent comp = projectionFrequency.getEditor();
	    if (comp instanceof JSpinner.NumberEditor) {
	    	JFormattedTextField field = 
	    		((JSpinner.NumberEditor) comp).getTextField();
	    	field.addActionListener(this);
	    	field.setActionCommand(""+FREQUENCY);
	    }
	    String[] names = new String[projections.size()];
        int index = 0;
        Entry entry;
        Iterator i = projections.entrySet().iterator();
        projectionTypes = new HashMap<Integer, Integer>();
        int j;
        while (i.hasNext()) {
        	entry = (Entry) i.next();
			j = (Integer) entry.getKey();
			projectionTypes.put(index, j);
			names[index] = (String) entry.getValue();
			index++;
		}
        projectionTypesBox = EditorUtil.createComboBox(names, 0, 
        		getBackground());
        projectionTypesBox.setBackground(getBackground());
        projectionTypesBox.setToolTipText(PROJECTION_DESCRIPTION);
        projectionTypesBox.setActionCommand(""+TYPE);
        projectionTypesBox.addActionListener(this);
        
        playLifetimeMovie = new JButton(
    			controller.getAction(ImViewerControl.PLAY_LIFETIME_MOVIE));
	    UIUtilities.unifiedButtonLookAndFeel(playLifetimeMovie);
    }
    
    /**
     * Attaches listener to the passed slider and sets the default values.
     * 
     * @param slider	The slider to handle.
     * @param max		The maximum value.
     * @param v			The default value.
     * @param toolTip	The tooltip text.
     * @param endLabel	The text for the tooltip which is displayed when 
     * 					slider changes value, as well as the label shown at 
     * 					the end of the text. 
     */
    private void initSlider(OneKnobSlider slider, int max, int v, 
    						String toolTip, String endLabel)
    {
    	slider.setVisible(max != 0);
    	slider.setMaximum(max);
    	slider.setValue(v);
    	slider.addChangeListener(this);
        slider.addMouseWheelListener(this);
        slider.setToolTipText(toolTip);
        slider.setEndLabel(endLabel);
        slider.setShowEndLabel(true);
        slider.setShowTipLabel(true);
        if (max > 0  && max <= MAX_NO_TICKS) {
        	slider.setPaintTicks(true);
        	slider.setMajorTickSpacing(1);
        }
    }
    
    /**
     * Initializes the value of the components displaying the currently selected
     * z-section and timepoint.
     */
    private void initializeValues()
    {
        int maxZ = model.getMaxZ();
        int maxT = model.getMaxT();
        projectionRange.setValues(maxZ+1, 1, maxZ+1, 1, 1, maxZ+1);
        projectionRange.addPropertyChangeListener(this);
        projectionRange.addMouseWheelListener(this);
        projectionRange.setToolTipText(PROJECTION_SLIDER_DESCRIPTION);
        
        initSlider(tSliderProjection, maxT, model.getDefaultT(), 
        		T_SLIDER_DESCRIPTION, T_SLIDER_TIPSTRING);
        
        initSlider(zSlider, maxZ, model.getDefaultZ(), 
        			Z_SLIDER_DESCRIPTION, Z_SLIDER_TIPSTRING);
        initSlider(zSliderGrid, maxZ, model.getDefaultZ(), 
    			Z_SLIDER_DESCRIPTION, Z_SLIDER_TIPSTRING);
       
        initSlider(tSlider, maxT, model.getDefaultT(), 
        		T_SLIDER_DESCRIPTION, T_SLIDER_TIPSTRING);
        initSlider(tSliderGrid, maxT, model.getDefaultT(), 
        		T_SLIDER_DESCRIPTION, T_SLIDER_TIPSTRING);
        
        gridRatioSlider.addChangeListener(this);
        ratioSlider.addChangeListener(this);
        projectionRatioSlider.addChangeListener(this);
        
        playTMovie.setVisible(maxT != 0);
        playTMovieGrid.setVisible(maxT != 0);
        playZMovie.setVisible(maxZ != 0);
        playZMovieGrid.setVisible(maxZ != 0);
        colorModelButton.setIcon(getColorModelIcon(model.getColorModel()));
        colorModelButton.setToolTipText(
        				getColorModelDescription(model.getColorModel()));
        colorModelButtonGrid.setIcon(getColorModelIcon(model.getColorModel()));
        colorModelButtonGrid.setToolTipText(
        				getColorModelDescription(model.getColorModel()));
        colorModelButtonProjection.setIcon(
        		getColorModelIcon(model.getColorModel()));
        colorModelButtonProjection.setToolTipText(
        				getColorModelDescription(model.getColorModel()));
        SpinnerNumberModel m = 
        	(SpinnerNumberModel) projectionFrequency.getModel();
        m.setMaximum(view.getMaxZ()+1);
		projectionFrequency.addChangeListener(this);
		
		//Lifetime for now
		int maxBin = model.getMaxLifetimeBin()-1;
		initSlider(lifetimeSlider, maxBin, model.getSelectedBin(), 
     			LITEIME_SLIDER_DESCRIPTION, LIFETIME_SLIDER_TIPSTRING);
		lifetimeSlider.setPaintTicks(false);
    }
    
    /**
     * Helper method to create a panel hosting the passed slider.
     * 
     * @param slider    The slider to host.
     * @return See above.
     */
    private JPanel layoutSlider(JComponent slider)
    {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(slider);
        return pane;
    }
    
    /**
     * Builds a tool bar hosting the passed button.
     * 
     * @param button The button to add.
     * @return See above
     */
    private JToolBar createMovieButtonBar(JButton button)
    {
    	JToolBar bar = new JToolBar();
    	bar.setFloatable(false);
    	bar.setRollover(true);
    	bar.setBorder(null);
    	bar.add(button);
    	return bar;
    }
    
    /**
     * Helper method to create a panel hosting the passed slider.
     * 
     * @return See above.
     */
    private JPanel createZSliderPane()
    {
    	JPanel pane = new JPanel();
    	double[][] tl = {{TableLayout.FILL}, 
				{TableLayout.FILL, TableLayout.PREFERRED}};
    	pane.setLayout(new TableLayout(tl));
    	pane.add(zSlider, "0, 0");
    	
    	pane.add(createMovieButtonBar(playZMovie), "0, 1");
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(pane);
        return p;
    }

    /**
     * Helper method to create a panel hosting the passed slider.
     * 
     * @return See above.
     */
    private JPanel createZGridSliderPane()
    {
    	JPanel pane = new JPanel();
    	double[][] tl = {{TableLayout.FILL}, 
				{TableLayout.FILL, TableLayout.PREFERRED}};
    	pane.setLayout(new TableLayout(tl));
    	pane.add(zSliderGrid, "0, 0");
    	pane.add(createMovieButtonBar(playZMovieGrid), "0, 1");
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(pane);
        return p;
    }
    
    /** 
     * Builds the tool bar displayed on the left side of the image.
     * 
     * @return See above.
     */
    private JToolBar buildToolBar()
    {
        JToolBar bar = new JToolBar(JToolBar.VERTICAL);
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        bar.add(colorModelButton);
        if (!model.isLifetime()) {
        	bar.add(Box.createRigidArea(VBOX));
            bar.add(channelMovieButton);
            bar.add(Box.createRigidArea(VBOX));
            bar.add(colorPickerButton);
        } else {
        	bar.add(Box.createRigidArea(VBOX));
            bar.add(Box.createRigidArea(VBOX));
            bar.add(Box.createRigidArea(VBOX));
        }
        
        return bar;
    }
    
    /** 
     * Builds the tool bar displayed on the left side of the  grid view.
     * 
     * @param button The button to add to the tool bar.
     * @return See above.
     */
    private JToolBar buildGridBar(JComponent button)
    {
    	JToolBar bar = new JToolBar(JToolBar.VERTICAL);
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        bar.add(button);
        return bar;
    }
    
    /** 
     * Builds the tool bar displayed on the left side of the  projection view.
     * 
     * @return See above.
     */
    private JToolBar buildProjectionBar()
    {
    	JToolBar bar = new JToolBar(JToolBar.VERTICAL);
    	bar.setFloatable(false);
    	bar.setRollover(true);
    	bar.setBorder(null);
        bar.add(colorModelButtonProjection);
        return bar;
    }

    /**
     * Creates a UI component hosting the {@link ChannelButton}s.
     * 
     * @return See above.
     */
    private JPanel createChannelsPane()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        ChannelButton button;
        Dimension d;
        int w = 0, h = 0;
        if (!model.isLifetime()) {
        	p.add(Box.createRigidArea(VBOX));
            channelButtons = createChannelButtons();
            Iterator<ChannelButton> i = channelButtons.iterator();
            while (i.hasNext()) {
    			button = i.next();
    			d = button.getPreferredSize();
    			if (d.width > w) w = d.width;
            	if (d.height > h) h = d.height;
    			button.addPropertyChangeListener(controller);
    			p.add(button);
                p.add(Box.createRigidArea(VBOX));
    		}
        }
        JPanel controls = new JPanel();
        double size[][] = {{TableLayout.PREFERRED}, 
        				{TableLayout.PREFERRED, TableLayout.PREFERRED,
        				TableLayout.PREFERRED, SLIDER_HEIGHT}};
        controls.setLayout(new TableLayout(size));
        
        controls.add(Box.createVerticalStrut(20), "0, 0");
        int k = 1;
        controls.add(buildToolBar(), "0, "+k+", c, c");
        k++;
        if (channelButtons.size() > ImViewer.MAX_CHANNELS) {
        	JScrollPane sp = new JScrollPane(p);
        	d = new Dimension(2*w, h*ImViewer.MAX_CHANNELS);
        	sp.setPreferredSize(d);
        	controls.add(sp, "0, "+k+", r, c");
        } else controls.add(p, "0, "+k);
        k++;
        controls.add(ratioSlider, "0, "+k+", c, c");
        return UIUtilities.buildComponentPanel(controls);
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(createChannelsPane());
        add(createZSliderPane());
    }
    
    /**
     * Sets the value of the specified slider when a propertyChange has been
     * fired.
     * 
     * @param slider The slider to update.
     * @param v The selected value.
     */
    private void updateSlider(JSlider slider, int v)
    {
        slider.removeChangeListener(this);
        slider.setValue(v);
        slider.addChangeListener(this);
    }
  
    /**
     * Sets the maximum value of the slider.
     * 
     * @param slider The slider to handle.
     * @param max	 The maximum value to set.
     */
    private void setSliderMax(JSlider slider, int max)
    {
    	slider.removeChangeListener(this);
    	slider.setMaximum(max);
    	slider.addChangeListener(this);
    }

    /**
     * Creates a new instance.
     * 
     * @param controller    Reference to the Control.
     *                      Mustn't be <code>null</code>.
     * @param model         Reference to the Model.
     *                      Mustn't be <code>null</code>.
     * @param view          Reference to the View.
     *                      Mustn't be <code>null</code>.                    
     */
    ControlPane(ImViewerControl controller, ImViewerModel model,
                ImViewerUI view)
    {
        if (controller == null) throw new NullPointerException("No control.");
        if (model == null) throw new NullPointerException("No model.");
        if (view == null) throw new NullPointerException("No view.");
        this.controller = controller;
        this.model = model;
        this.view = view;
        icons = IconManager.getInstance();
        initComponents();
    }
    
    /**
     * Creates a collection of <code>ChannelButton</code>s.
     * 
     * @return See above.
     */
    List<ChannelButton> createChannelButtons()
    {
    	List<ChannelButton> channelButtons = new ArrayList<ChannelButton>();
    	List<ChannelData> data = model.getChannelData();
    	boolean gs = model.getColorModel().equals(ImViewer.GREY_SCALE_MODEL);
    	ChannelButton button;
    	ChannelData d;
    	Dimension dim;
    	Dimension dimMax = ChannelButton.DEFAULT_MIN_SIZE;
    	Iterator<ChannelData> i = data.iterator();
    	int k;
    	while (i.hasNext()) {
    		d = i.next();
    		k = d.getIndex();
    		button = new ChannelButton(""+d.getChannelLabeling(), 
    				model.getChannelColor(k), k, model.isChannelActive(k));
    		if (gs) button.setGrayedOut(gs);
    		channelButtons.add(button);
    		dim = button.getPreferredSize();
    		if (dim.width > dimMax.width) 
    			dimMax = new Dimension(dim.width, dimMax.height);
    	}

    	Iterator<ChannelButton> j = channelButtons.iterator();
    	while (j.hasNext())
    		j.next().setPreferredSize(dimMax);
    	return channelButtons;
    }
    
    /** 
     * This method should be called straight after the metadata and the
     * rendering settings are loaded.
     */
    void buildComponent()
    {
        initializeValues();
        buildGUI();
    }
    
    /**
     * Builds the control panel displayed in the grid view.
     * 
     * @return See above.
     */
    JPanel buildGridComponent()
    {
    	if (model.isLifetime()) return new JPanel();
    	JPanel p = createZGridSliderPane();
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        ChannelButton button;
        buttons.add(Box.createRigidArea(VBOX));
        channelButtonsGrid = createChannelButtons();
        Iterator<ChannelButton> i = channelButtonsGrid.iterator();
        Dimension d;
        int w = 0, h = 0;
        while (i.hasNext()) {
        	button = i.next();
        	d = button.getPreferredSize();
        	if (d.width > w) w = d.width;
        	if (d.height > h) h = d.height;
        	buttons.add(button);
            buttons.add(Box.createRigidArea(VBOX));
            button.addPropertyChangeListener(controller);
            
		}
        JPanel controls = new JPanel();
        double size[][] = {{TableLayout.PREFERRED}, 
        				{TableLayout.PREFERRED, TableLayout.PREFERRED,
        	TableLayout.PREFERRED, TableLayout.PREFERRED, SLIDER_HEIGHT}};
        
        controls.setLayout(new TableLayout(size));
        controls.add(Box.createVerticalStrut(20), "0, 0");
        JToolBar bar = buildGridBar(colorModelButtonGrid);
        bar.add(Box.createRigidArea(VBOX));
        controls.add(bar, "0, 1, c, c");
        controls.add(buildGridBar(textVisibleButton), "0, 2, c, c");
        if (channelButtonsGrid.size() > ImViewer.MAX_CHANNELS) {
        	JScrollPane sp = new JScrollPane(buttons);
        	d = new Dimension(2*w, h*ImViewer.MAX_CHANNELS);
        	sp.setPreferredSize(d);
        	controls.add(sp, "0, 3, r, c");
        } else controls.add(buttons, "0, 3");
        controls.add(gridRatioSlider, "0, 4, c, c");
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
        content.add(UIUtilities.buildComponentPanel(controls));
        content.add(p);
        return content;
    }
    
    /** 
     * Builds the component hosting controls for projection.
     * 
     * @return See above
     */
    JPanel buildProjectionToolBar()
    {
    	JPanel bar = new JPanel();
		bar.setBorder(null);
		bar.add(new JLabel("Intensity: "));
		bar.add(projectionTypesBox);
		bar.add(new JLabel(" Every n-th slice: "));
		bar.add(projectionFrequency);
		bar.add(Box.createRigidArea(VBOX));
		bar.add(projectionProject);
		JPanel projectionBar = new JPanel();
		projectionBar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		projectionBar.add(bar);
		return projectionBar;
    }
    
    /**
     * Builds the component hosting the controls to manage the projected image.
     * 
     * @return See above.
     */
    JPanel buildProjectionComponent()
    {
    	if (model.isLifetime()) return new JPanel();
    	JPanel p = layoutSlider(projectionRange);
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        ChannelButton button;
        buttons.add(Box.createRigidArea(VBOX));
        channelButtonsProjection = createChannelButtons();
        Iterator<ChannelButton> i = channelButtonsProjection.iterator();
        int w = 0, h = 0;
        Dimension d;
        while (i.hasNext()) {
        	button = i.next();
        	d = button.getPreferredSize();
        	if (d.width > w) w = d.width;
        	if (d.height > h) h = d.height;
        	buttons.add(button);
            buttons.add(Box.createRigidArea(VBOX));
            button.addPropertyChangeListener(controller);
		}
        JPanel controls = new JPanel();
        double size[][] = {{TableLayout.PREFERRED}, 
        				{TableLayout.PREFERRED, TableLayout.PREFERRED,
        				TableLayout.PREFERRED, SLIDER_HEIGHT}};
        
        controls.setLayout(new TableLayout(size));
        controls.add(Box.createVerticalStrut(20), "0, 0");
        int k = 1;
        controls.add(buildProjectionBar(), "0, "+k+", c, c");
        k++;
        if (channelButtonsProjection.size() > ImViewer.MAX_CHANNELS) {
        	JScrollPane sp = new JScrollPane(buttons);
        	d = new Dimension(2*w, h*ImViewer.MAX_CHANNELS);
        	sp.setPreferredSize(d);
        	controls.add(sp, "0, "+k+", r, c");
        } else controls.add(buttons, "0, "+k+", c, c");
        k++;
        controls.add(projectionRatioSlider, "0, "+k+", c, c");
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
        content.add(UIUtilities.buildComponentPanel(controls));
        content.add(p);
        return content;
    }
    
    /**
     * Updates UI components when a new timepoint is selected.
     * 
     * @param t The selected timepoint.
     */
    void setTimepoint(int t)
    { 
    	updateSlider(tSlider, t);
    	updateSlider(tSliderGrid, t);
    	updateSlider(tSliderProjection, t);
    }
    
    /**
     * Updates UI components when a new z-section is selected.
     * 
     * @param z The selected z-section.
     */
    void setZSection(int z)
    { 
    	updateSlider(zSlider, z); 
    	updateSlider(zSliderGrid, z); 
    }
    
    /** Updates UI components when a new color model is selected. */
    void setColorModel()
    {
        boolean gs = (model.getColorModel().equals(ImViewer.GREY_SCALE_MODEL));
        Iterator i = channelButtons.iterator();
        ChannelButton button;
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            button.setSelected(
                    model.isChannelActive(button.getChannelIndex()));
            button.setGrayedOut(gs);
        }
        i = channelButtonsGrid.iterator();
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            if (!gs) 
            	button.setSelected(
                   model.isChannelActive(button.getChannelIndex()));
            button.setGrayedOut(gs);
        }
        i = channelButtonsProjection.iterator();
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            button.setSelected(
            		model.isChannelActive(button.getChannelIndex()));
            button.setGrayedOut(gs);
        }
        colorModelButton.setIcon(getColorModelIcon(model.getColorModel()));
        colorModelButton.setToolTipText(getColorModelDescription(
				model.getColorModel()));
        colorModelButtonGrid.setIcon(getColorModelIcon(model.getColorModel()));
        colorModelButtonGrid.setToolTipText(getColorModelDescription(
				model.getColorModel()));
        colorModelButtonProjection.setIcon(
        		getColorModelIcon(model.getColorModel()));
        colorModelButtonProjection.setToolTipText(getColorModelDescription(
				model.getColorModel()));
    }
    
    /**
     * Sets the selected channels in the grid view.
     * 
     * @param channels Collection of channels to set.
     */
    void setChannelsSelection(List channels)
    {
    	Iterator i = channelButtonsGrid.iterator();
        ChannelButton button;
        int index;
		while (i.hasNext()) {
            button = (ChannelButton) i.next();
            index = button.getChannelIndex();
            button.setSelected(channels.contains(index));
        }
    }
    
    /** 
     * Updates the {@link ChannelButton}s when a new one is selected or 
     * deselected.
     * 
     * @param index One of the following constants {@link ImViewerUI#GRID_ONLY},
	 * 				{@link ImViewerUI#VIEW_ONLY} and 
	 * 				{@link ImViewerUI#ALL_VIEW}.
     */
    void setChannelsSelection(int index)
    {
        Iterator i;
        ChannelButton button;
        switch (index) {
			case ImViewerUI.GRID_ONLY:
				i = channelButtonsGrid.iterator();
				while (i.hasNext()) {
		            button = (ChannelButton) i.next();
		            button.setSelected(
		                    model.isChannelActive(button.getChannelIndex()));
		        }
				break;
			case ImViewerUI.VIEW_ONLY:
				i = channelButtons.iterator();
				while (i.hasNext()) {
		            button = (ChannelButton) i.next();
		            button.setSelected(
		                    model.isChannelActive(button.getChannelIndex()));
		        }
				break;
			case ImViewerUI.PROJECTION_ONLY:
				i = channelButtonsProjection.iterator();
				while (i.hasNext()) {
		            button = (ChannelButton) i.next();
		            button.setSelected(
		                    model.isChannelActive(button.getChannelIndex()));
		        }
				break;
			case ImViewerUI.ALL_VIEW:
				i = channelButtons.iterator();
				while (i.hasNext()) {
		            button = (ChannelButton) i.next();
		            button.setSelected(
		                    model.isChannelActive(button.getChannelIndex()));
		        }
				i = channelButtonsGrid.iterator();
				while (i.hasNext()) {
		            button = (ChannelButton) i.next();
		            button.setSelected(
		                    model.isChannelActive(button.getChannelIndex()));
		        }
				i = channelButtonsProjection.iterator();
				while (i.hasNext()) {
		            button = (ChannelButton) i.next();
		            button.setSelected(
		                    model.isChannelActive(button.getChannelIndex()));
		        }
		}
    }
    
    /**
     * Sets the color of selected channel.
     * 
     * @param index The channel index.
     * @param c     The color to set.
     */
    void setChannelColor(int index, Color c)
    {
        Iterator i = channelButtons.iterator();
        ChannelButton button;
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            if (index == button.getChannelIndex()) 
                button.setColor(c);
        }
        i = channelButtonsGrid.iterator();
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            if (index == button.getChannelIndex()) 
                button.setColor(c);
        }
        i = channelButtonsProjection.iterator();
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            if (index == button.getChannelIndex()) 
                button.setColor(c);
        }
    }
    
    /** Resets the rendering settings. */
    void resetRndSettings()
    {
        boolean gs = (model.getColorModel().equals(ImViewer.GREY_SCALE_MODEL));
        Iterator i = channelButtons.iterator();
        ChannelButton button;
        int index;
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            index = button.getChannelIndex();
            button.setSelected(model.isChannelActive(index));
            button.setColor(model.getChannelColor(index)); 
            button.setGrayedOut(gs);
        }
        i = channelButtonsGrid.iterator();
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            index = button.getChannelIndex();
            button.setSelected(model.isChannelActive(index));
            button.setColor(model.getChannelColor(index)); 
            button.setGrayedOut(gs);
        }
        i = channelButtonsProjection.iterator();
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            index = button.getChannelIndex();
            button.setSelected(model.isChannelActive(index));
            button.setColor(model.getChannelColor(index)); 
            button.setGrayedOut(gs);
        }
        Icon icon = getColorModelIcon(model.getColorModel());
        String tip = getColorModelDescription(model.getColorModel());
        colorModelButton.setIcon(icon);
        colorModelButton.setToolTipText(tip);
        colorModelButtonGrid.setIcon(icon);
        colorModelButtonGrid.setToolTipText(tip);
        colorModelButtonProjection.setIcon(icon);
        colorModelButtonProjection.setToolTipText(tip);
        setZSection(model.getDefaultZ());
        setTimepoint(model.getDefaultT());
        ProjectionParam ref = model.getLastProjRef();
        if (ref != null)
        	projectionRange.setInterval(ref.getStartZ(), ref.getEndZ());
    }
    
    /** Resets the UI when the user switches to a new rendering control. */
    void switchRndControl()
    {
    	setSliderMax(zSlider, model.getMaxZ());
    	setSliderMax(zSliderGrid, model.getMaxZ());
    	resetRndSettings();
    }

    /** 
     * Reacts to {@link ImViewer} change events.
     * 
     * @param b Pass <code>true</code> to enable the UI components, 
     *          <code>false</code> otherwise.
     */
    void onStateChange(boolean b)
    {
        //if (model.isPlayingMovie()) enableSliders(!b);
        //else enableSliders(b);
        Iterator i = channelButtons.iterator();
        while (i.hasNext())
            ((ChannelButton) i.next()).setEnabled(b);
        i = channelButtonsGrid.iterator();
        while (i.hasNext())
            ((ChannelButton) i.next()).setEnabled(b);
        i = channelButtonsProjection.iterator();
        while (i.hasNext())
            ((ChannelButton) i.next()).setEnabled(b);
        colorModelButton.setEnabled(b);
        colorModelButtonGrid.setEnabled(b);
        colorModelButtonProjection.setEnabled(b);
        
        //projection stuff
        if (projectionTypesBox != null) projectionTypesBox.setEnabled(b);
    	if (projectionFrequency != null) projectionFrequency.setEnabled(b);
    }
    
    /**
     * Builds and returns a UI component hosting the time slider 
     * corresponding to the passed index.
     * 
     * @param index The index used to identify the slider.
     * @return See above.
     */
    JPanel getTimeSliderPane(int index) 
    {
    	switch (index) {
			case ImViewer.GRID_INDEX:
				JPanel p = new JPanel();
	        	p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
	        	p.add(createMovieButtonBar(playTMovieGrid));
	        	p.add(tSliderGrid);
	        	return p;
			case ImViewer.PROJECTION_INDEX:
				return layoutSlider(tSliderProjection);
			case ImViewer.VIEW_INDEX:
			default:
				JPanel pane = new JPanel();
	        	pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
	        	pane.add(createMovieButtonBar(playTMovie));
	        	pane.add(tSlider);
	        	return pane;
		}
    }
    
    /**
     * Builds and returns a UI component hosting the time slider 
     * corresponding to the passed index.
     * 
     * @param index The index used to identify the slider.
     * @return See above.
     */
    JPanel getLifetimeSliderPane(int index) 
    {
    	switch (index) {
			case ImViewer.VIEW_INDEX:
			default:
				JPanel pane = new JPanel();
	        	pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
	        	//pane.add(createMovieButtonBar(playLifetimeMovie));
	        	pane.add(lifetimeSlider);
	        	return pane;
		}
    }
    
    /**
     * Sets the <code>enable</code> flag of the slider used to select
     * the current z-section and timepoint.
     * 
     * @param b Pass <code>true</code> to enable the sliders,
     * 			<code>false</code> otherwise.
     */
    void enableSliders(boolean b)
    {
    	enableZSliders(b);
    	enableTSliders(b);
    	if (b) lifetimeSlider.setEnabled(model.getMaxLifetimeBin() != 0);
    	else lifetimeSlider.setEnabled(b);
	}
    
    /**
     * Sets the <code>enable</code> flag of the slider used to select
     * the current z-section and timepoint.
     * 
     * @param b Pass <code>true</code> to enable the sliders,
     * 			<code>false</code> otherwise.
     */
    void enableZSliders(boolean b)
    {
    	if (b) {
            zSlider.setEnabled(model.getMaxZ() != 0);
            zSliderGrid.setEnabled(model.getMaxZ() != 0);
            projectionRange.setEnabled(model.getMaxZ() != 0);
        } else {
            zSlider.setEnabled(b);
            zSliderGrid.setEnabled(b);
            projectionRange.setEnabled(b);
        } 
	}
    
    /**
     * Sets the <code>enable</code> flag of the slider used to select
     * the current z-section and timepoint.
     * 
     * @param b Pass <code>true</code> to enable the sliders,
     * 			<code>false</code> otherwise.
     */
    void enableTSliders(boolean b)
    {
    	if (b) {
            tSlider.setEnabled(model.getMaxT() != 0);
            tSliderGrid.setEnabled(model.getMaxT() != 0);
            tSliderProjection.setEnabled(model.getMaxT() != 0);
        } else {
            tSlider.setEnabled(b);
            tSliderGrid.setEnabled(b);
            tSliderProjection.setEnabled(b);
        } 
	}
    
    /**
     * Sets the specified channel to active.
     * 
     * @param index   The channel's index.
     * @param uiIndex One of the following constants 
     * 				  {@link ImViewerUI#GRID_ONLY} and 
	 * 				  {@link ImViewerUI#ALL_VIEW}.
     */
    void setChannelActive(int index, int uiIndex)
    {
    	Iterator i;
        ChannelButton button;
        switch (uiIndex) {
			case ImViewerUI.GRID_ONLY:
				 i = channelButtonsGrid.iterator();
			        while (i.hasNext()) {
			            button = (ChannelButton) i.next();
			            if (index == button.getChannelIndex()) 
			                button.setSelected(true);
			        }
				break;
			case ImViewerUI.ALL_VIEW:
				i = channelButtons.iterator();
				while (i.hasNext()) {
					button = (ChannelButton) i.next();
					if (index == button.getChannelIndex()) 
						button.setSelected(true);
				}
				i = channelButtonsGrid.iterator();
				while (i.hasNext()) {
					button = (ChannelButton) i.next();
					if (index == button.getChannelIndex()) 
						button.setSelected(true);
				}
				i = channelButtonsProjection.iterator();
				while (i.hasNext()) {
					button = (ChannelButton) i.next();
					if (index == button.getChannelIndex()) 
						button.setSelected(true);
				}
		}       
	}
    
    /**
     * Returns the collection of active channels in the grid view.
     * 
     * @return See above.
     */
    List getActiveChannelsInGrid()
    {
    	List<Integer> active = new ArrayList<Integer>();
    	Iterator i = channelButtonsGrid.iterator();
    	ChannelButton button;
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            if (button.isSelected()) active.add(button.getChannelIndex());
        }
        return active;
    }
    
    /**
     * Returns the collection of active channels in the projection view.
     * 
     * @return See above.
     */
    List getActiveChannelsInProjection()
    {
    	List<Integer> active = new ArrayList<Integer>();
    	Iterator i = channelButtonsProjection.iterator();
    	ChannelButton button;
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            if (button.isSelected()) active.add(button.getChannelIndex());
        }
        return active;
    }
    
    /**
     * Updates UI components when a zooming factor is selected.
     * 
     * @param zoomIndex The index of the selected zoomFactor.
     */
    void setZoomFactor(int zoomIndex)
    {
    	if (ratioSlider.getMinimum() > zoomIndex || 
    		ratioSlider.getMaximum() < zoomIndex)
    		return;
    	ratioSlider.removeChangeListener(this);
    	ratioSlider.setValue(zoomIndex);
    	ratioSlider.addChangeListener(this);
    	projectionRatioSlider.removeChangeListener(this);
    	projectionRatioSlider.setValue(zoomIndex);
    	projectionRatioSlider.addChangeListener(this);
    }
    
    /**
	 * Returns the lower bound of the z-section to project.
	 * 
	 * @return See above.
	 */
	int getProjectionStartZ() { return projectionRange.getStartValue()-1; }
	
	/**
	 * Returns the lower bound of the z-section to project.
	 * 
	 * @return See above.
	 */
	int getProjectionEndZ() { return projectionRange.getEndValue()-1; }
	
    /**
     * Updates UI components when a zooming factor for the grid
     * is selected.
     * 
     * @param zoomIndex The index of the selected zoomFactor.
     */
    void setGridMagnificationFactor(int zoomIndex)
    {
    	if (gridRatioSlider.getMinimum() > zoomIndex || 
    			gridRatioSlider.getMaximum() < zoomIndex)
    		return;
    	gridRatioSlider.removeChangeListener(this);
    	gridRatioSlider.setValue(zoomIndex);
    	gridRatioSlider.addChangeListener(this);
    }
    
    /**
	 * Returns the stepping used for the projection.
	 * 
	 * @return See above.
	 */
	int getProjectionStepping()
	{
		return (Integer) projectionFrequency.getValue();
	}

	/**
	 * Returns the type of projection.
	 * 
	 * @return See above.
	 */
	int getProjectionType()
	{
		int index = projectionTypesBox.getSelectedIndex();
		return projectionTypes.get(index);
	}
    
	/**
	 * Returns a textual version of the type of projection.
	 * 
	 * @return See above.
	 */
	String getProjectionTypeName()
	{
		int index = projectionTypesBox.getSelectedIndex();
		return projections.get(index);
	}
	
    /**
     * Reacts to the selection of an item in the projection box
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	int index = Integer.parseInt(e.getActionCommand());
    	if (index == FREQUENCY) {
    		JComponent comp = projectionFrequency.getEditor();
    	    if (comp instanceof JSpinner.NumberEditor) {
    	    	JFormattedTextField field = 
    	    		((JSpinner.NumberEditor) comp).getTextField();
    	    	String value = field.getText();
    	    	int v = -1;
    	    	try {
					v = Integer.parseInt(value);
				} catch (Exception ex) {}
				if (v == -1 || v > model.getMaxZ() || v < 1) return;
				projectionFrequency.setValue(v);
    	    }
    	}
        controller.setProjectionRange(true);
    }
    
    /**
     * Reacts to selection of a new plane.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        Object object = e.getSource();
        if (object instanceof JSlider) {
        	if (object == gridRatioSlider) {
        		double r = (double) gridRatioSlider.getValue()/10;
        		controller.setGridMagnificationFactor(r);
        		return;
        	} else if (object == ratioSlider) {
        		controller.setZoomFactor(ratioSlider.getValue());
        	} else if (object == projectionRatioSlider) {
        		controller.setZoomFactor(projectionRatioSlider.getValue());
        	}
        	if (object == zSlider || object == tSlider || 
        			object == lifetimeSlider)
        		setSelectedXYPlane(zSlider.getValue(), tSlider.getValue());
        	else if (object == zSliderGrid || object == tSliderGrid)
        		setSelectedXYPlane(zSliderGrid.getValue(), 
                        tSliderGrid.getValue());
        	else if (object == tSliderProjection) {
        		//Only if knob is released.
        		if (!tSliderProjection.getValueIsAdjusting()) {
        			try {
        				setSelectedXYPlane(-1, tSliderProjection.getValue()-1);
        				controller.setProjectionRange(true);
					} catch (Exception ex) {}
        		}
        	} 
        } else if (object == projectionFrequency)
			controller.setProjectionRange(true);
    }
    
    /**
     * Reacts to wheels moved event related to the {@link #zSlider},
     * {@link #tSlider}, {@link #zSliderGrid} and {@link #zSliderGrid}.
     * @see MouseWheelListener#mouseWheelMoved(MouseWheelEvent)
     */
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        Object source = e.getSource();
        if (source == zSlider && zSlider.isEnabled()) mouseWheelMovedZ(e);
        else if (source == tSlider && tSlider.isEnabled())
            mouseWheelMovedT(e);
        else if (source == zSliderGrid && zSliderGrid.isEnabled()) 
        	mouseWheelMovedZ(e);
        else if (source == tSliderGrid && tSliderGrid.isEnabled())
            mouseWheelMovedT(e);
        else if (source == lifetimeSlider && lifetimeSlider.isEnabled())
            mouseWheelMovedLifetime(e);
    }

    /**
     * Notifies that the projection range has been modified.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (TwoKnobsSlider.RIGHT_MOVED_PROPERTY.equals(name) ||
			TwoKnobsSlider.LEFT_MOVED_PROPERTY.equals(name))
			controller.setProjectionRange(false);
		else if (TwoKnobsSlider.KNOB_RELEASED_PROPERTY.equals(name))
			controller.setProjectionRange(true);
	}

}

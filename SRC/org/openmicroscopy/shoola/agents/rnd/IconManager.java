/*
 * org.openmicroscopy.shoola.agents.rnd.IconManager
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.rnd;


//Java imports
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class IconManager
{
	/** Contains icon objects to be retrieved through the icon IDs. */
	private Icon[]				icons;

	/** ID of the OME logo icon. */
	public static final int     OME = 0; 
	
	/** ID of the information icon. */
	public static final int     INFO = 1;   
  
	/** ID of the histogram icon. */
	public static final int     HISTOGRAM = 2;
	
	/** ID of the contrast stretching icon. */
	public static final int     STRETCHING = 3;
	
	/** ID of the plane slicing icon. */
	public static final int     SLICING = 4;
	
	/** ID of the greyscale icon. */
	public static final int		GREYSCALE = 5;
	
	/** ID of the greyscale icon. */
	public static final int		RGB = 6;
		
	/** ID of the greyscale icon. */
	public static final int		HSB = 7;
	
	/** ID of the codomain icon. */
	public static final int		CODOMAIN = 8;
	
	/** ID of the save icon. */
	public static final int		SAVE_SETTINGS = 9;
	
	/** 
	 * The maximum ID used for the icon IDs.
	 * Allows to correctly build arrays for direct indexing. 
	 */
	private static int          MAX_ID = 9;
	
	/** Paths of the icon files. */
	private static String[]     relPaths = new String[MAX_ID+1];
		
	static {
		relPaths[OME] = "OME16.png";
		relPaths[INFO] = "information16.png";
		relPaths[HISTOGRAM] = "histogram16.png";
		relPaths[STRETCHING] = "contrastStretching16.png";
		relPaths[SLICING] = "planeSlicing16.png";
		relPaths[GREYSCALE] = "grayscale.png";
		relPaths[RGB] = "rgb16.png";
		relPaths[HSB] = "hsb16.png";
		relPaths[CODOMAIN] = "codomain16.png";
		relPaths[SAVE_SETTINGS] = "save_DB16.png";
	}
	
	/** The sole instance that provides. */
	private static IconManager	singleton;
	
	/** Returns the <code>IconManager</code> object. */
	public static IconManager getInstance(Registry registry)
	{
		if (singleton == null) {
			try {	
				singleton = new IconManager(registry);
			} catch (Exception e) {
				throw new RuntimeException("Can't create the IconManager", e);
			}
		}
		return singleton;
	}
	
	private IconFactory 		factory;
	
	/**
	 * Creates a new instance and configures the parameters.
	 * 
	 * @param registry	Reference to the registry.
	 */
	private IconManager(Registry registry)
	{
		factory = (IconFactory) registry.lookup("/resources/icons/Factory");
		icons = new Icon[MAX_ID+1];
	}

	/** 
	 * Retrieves the icon specified by the icon <code>ID</code>.
	 *
	 * @param ID    Must be one of the IDs defined by this class.
	 * @return The specified icon. The retuned value is meant to be READ-ONLY.
	 */    
	public Icon getIcon(int ID)
	{
		if (icons[ID] == null) icons[ID] = factory.getIcon(relPaths[ID]);
		return icons[ID];
	}
	
}

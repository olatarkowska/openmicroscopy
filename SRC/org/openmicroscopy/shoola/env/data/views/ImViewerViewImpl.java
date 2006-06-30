/*
 * org.openmicroscopy.shoola.env.data.views.ImViewerViewImpl
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

package org.openmicroscopy.shoola.env.data.views;



//Java imports

//Third-party libraries

//Application-internal dependencies
import omeis.providers.re.data.PlaneDef;

import org.openmicroscopy.shoola.env.data.views.calls.ChannelMetadataLoader;
import org.openmicroscopy.shoola.env.data.views.calls.ImageRenderer;
import org.openmicroscopy.shoola.env.data.views.calls.RenderingControlLoader;
import org.openmicroscopy.shoola.env.event.AgentEventListener;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ImViewerViewImpl
    implements ImViewerView
{

    /**
     * Implemented as specified by the view interface.
     * @see ImViewerView#loadChannelMetadata(long, AgentEventListener)
     */
    public CallHandle loadChannelMetadata(long imageID,
                                        AgentEventListener observer)
    {
        BatchCallTree cmd = new ChannelMetadataLoader(imageID);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see ImViewerView#loadRenderingControl(long, AgentEventListener)
     */
    public CallHandle loadRenderingControl(long pixelsID, 
                                        AgentEventListener observer)
    {
        BatchCallTree cmd = new RenderingControlLoader(pixelsID);
        return cmd.exec(observer);
    }

    /**
     * Implemented as specified by the view interface.
     * @see ImViewerView#render(long, PlaneDef, AgentEventListener)
     */
    public CallHandle render(long pixelsID, PlaneDef pd, 
                        AgentEventListener observer)
    {
        BatchCallTree cmd = new ImageRenderer(pixelsID, pd);
        return cmd.exec(observer);
    }

}

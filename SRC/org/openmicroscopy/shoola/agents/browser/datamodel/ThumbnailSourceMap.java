/*
 * org.openmicroscopy.shoola.agents.browser.datamodel.ThumbnailSourceMap
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.datamodel;

import java.util.*;

import org.openmicroscopy.ds.dto.Image;

/**
 * Specifies a mapping between image thumbnails and Image DTOs.  This can be
 * lazily initialized (and is so by design); that is, one can specify the
 * Images and then, after hitting the image server, can get the corresponding
 * Thumbnails.  Both are linked (implied) by the same image IDs.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class ThumbnailSourceMap
{
    private Map thumbnailMap;
    private Map sourceMap;

    /**
     * Creates a Thumbnail-to-data source mappping.
     */
    public ThumbnailSourceMap()
    {
        thumbnailMap = new HashMap();
        sourceMap = new HashMap();
    }

    /**
     * Adds image data to the mapping.  The image is bound to the corresponding
     * thumbnail by the image ID (that should be the same for both).  This
     * is an implied relation.
     * 
     * @param image The image to add.  Will accomplish nothing if it is null.
     */
    public void putImageData(Image image)
    {
        if(image != null)
        {
            sourceMap.put(new Integer(image.getID()),image);
        }
    }
    
    /**
     * Return the concrete image metadata associated with the image with
     * the specified ID.  If no mapping exists, will return null.
     * @param imageID The Image to access.
     * @return See above.
     */
    public Image getImageData(int imageID)
    {
        return (Image)sourceMap.get(new Integer(imageID));
    }
}

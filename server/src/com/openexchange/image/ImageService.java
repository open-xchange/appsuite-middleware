/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.image;

import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataSource;
import com.openexchange.image.internal.ImageData;
import com.openexchange.session.Session;

/**
 * {@link ImageService} - Service for storing/retrieving images
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ImageService {

    /**
     * Checks if this registry contains images for the specified session.
     * 
     * @param session The session
     * @param uniqueId The unique ID
     * @return <code>true</code> if this registry contains images for the specified session; otherwise <code>false</code>
     */
    public boolean containsImageData(Session session, String uniqueId);

    /**
     * Checks if this registry contains images for the specified context.
     * 
     * @param contextId The context ID
     * @param uniqueId The unique ID
     * @return <code>true</code> if this registry contains images for the specified context; otherwise <code>false</code>
     */
    public boolean containsImageData(int contextId, String uniqueId);

    /**
     * Adds specified data source and data arguments as image data to this registry if no matching image data is already contained in
     * registry.
     * 
     * @param session The session to which the image data shall be bound
     * @param imageSource The image source
     * @param imageArguments The image arguments
     * @param timeToLive The time-to-live for the new image data
     * @return Either the new image data from specified data source and data arguments or the existing one if already contained in registry.
     */
    public ImageData addImageData(Session session, DataSource imageSource, DataArguments imageArguments, int timeToLive);

    /**
     * Adds specified data source and data arguments as image data to this registry if no matching image data is already contained in
     * registry.
     * 
     * @param contextId The ID of the context to which the image data shall be bound
     * @param imageSource The image source
     * @param imageArguments The image arguments
     * @param timeToLive The time-to-live for the new image data
     * @return Either the new image data from specified data source and data arguments or the existing one if already contained in registry.
     */
    public ImageData addImageData(int contextId, DataSource imageSource, DataArguments imageArguments, int timeToLive);

    /**
     * Adds specified data source and data arguments as image data to this registry if no matching image data is already contained in
     * registry.
     * 
     * @param session The session to which the image data shall be bound
     * @param imageSource The image source
     * @param imageArguments The image arguments
     * @return Either the new image data from specified data source and data arguments or the existing one if already contained in registry.
     */
    public ImageData addImageData(Session session, DataSource imageSource, DataArguments imageArguments);

    /**
     * Adds specified data source and data arguments as image data to this registry if no matching image data is already contained in
     * registry.
     * 
     * @param contextId The ID of the context to which the image data shall be bound
     * @param imageSource The image source
     * @param imageArguments The image arguments
     * @return Either the new image data from specified data source and data arguments or the existing one if already contained in registry.
     */
    public ImageData addImageData(int contextId, DataSource imageSource, DataArguments imageArguments);

    /**
     * Adds specified image data to this registry.
     * 
     * @param contextId The ID of the context to which the image data shall be bound
     * @param imageData The image data
     */
    public void addImageData(int contextId, ImageData imageData);

    /**
     * Adds specified image data to this registry.
     * 
     * @param session The session to which the image data shall be bound
     * @param imageData The image data
     */
    public void addImageData(Session session, ImageData imageData);

    /**
     * Removes all images bound to specified session.
     * 
     * @param session The session to clean images from
     */
    public void removeImageData(Session session);

    /**
     * Removes all images bound to specified session.
     * 
     * @param contextId The ID of the context to clean images from
     */
    public void removeImageData(int contextId);

    /**
     * Removes the images with specified unique ID bound to specified context.
     * 
     * @param contextId The ID of the context to clean images from
     * @param uniqueId The unique ID
     * @return <code>true</code> if corresponding image was successfully deleted; otherwise <code>false</code> if not found
     */
    public boolean removeImageData(int contextId, String uniqueId);

    /**
     * Gets all images bound to specified session.
     * 
     * @param session The session to get images from
     * @return All images bound to specified session as an array of {@link ImageData}; an empty array is returned if no images are held for
     *         specified session
     */
    public ImageData[] getImageData(Session session);

    /**
     * Gets all images bound to specified context.
     * 
     * @param contextId The ID of the context to get images from
     * @return All images bound to specified context as an array of {@link ImageData}; an empty array is returned if no images are held for
     *         specified context
     */
    public ImageData[] getImageData(int contextId);

    /**
     * Gets the image data bound to specified session and registered to specified unique ID.
     * 
     * @param session The session to which the image data is bound
     * @param uniqueId The image data's unique ID
     * @return The image data bound to specified session and registered to specified unique ID, or <code>null</code> if none present
     */
    public ImageData getImageData(Session session, String uniqueId);

    /**
     * Gets the image data bound to specified context and registered to specified unique ID.
     * 
     * @param contextId The ID of the context to which the image data is bound
     * @param uniqueId The image data's unique ID
     * @return The image data bound to specified context and registered to specified unique ID, or <code>null</code> if none present
     */
    public ImageData getImageData(int contextId, String uniqueId);

    /**
     * Clears the registry.
     */
    public void clearRegistry();
}

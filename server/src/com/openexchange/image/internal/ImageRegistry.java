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

package com.openexchange.image.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataSource;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link ImageRegistry} - The image registry which bounds images to a session or a context. A heart-beat to session service/context storage
 * checks if session/context is still valid.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImageRegistry {

    private static final int DELAY = 30000;

    private static final int INITIAL_DELAY = 1000;

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(ImageRegistry.class);

    private static final ImageRegistry INSTANCE = new ImageRegistry();

    /**
     * Gets the instance of {@link ImageRegistry}.
     * 
     * @return The instance of {@link ImageRegistry}
     */
    public static ImageRegistry getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<String, ConcurrentMap<String, ImageData>> sessionBoundImagesMap;

    private final ConcurrentMap<Integer, ConcurrentMap<String, ImageData>> contextBoundImagesMap;

    private ScheduledTimerTask[] tasks;

    /**
     * Initializes a new {@link ImageRegistry}.
     */
    private ImageRegistry() {
        super();
        sessionBoundImagesMap = new ConcurrentHashMap<String, ConcurrentMap<String, ImageData>>();
        contextBoundImagesMap = new ConcurrentHashMap<Integer, ConcurrentMap<String, ImageData>>();
    }

    /**
     * Starts the heart-beat.
     */
    void startHeartbeat() {
        /*
         * Schedule tasks for specified period
         */
        final TimerService timer = ServerServiceRegistry.getInstance().getService(TimerService.class);
        if (null != timer) {
            tasks = new ScheduledTimerTask[2];
            tasks[0] = timer.scheduleWithFixedDelay(new SessionBoundImagesCleaner(sessionBoundImagesMap), INITIAL_DELAY, DELAY);
            tasks[1] = timer.scheduleWithFixedDelay(new ContextBoundImagesCleaner(contextBoundImagesMap), INITIAL_DELAY, DELAY);
        }
    }

    /**
     * Stops the heart-beat.
     */
    void stopHeartbeat() {
        if (tasks != null) {
            /*
             * Stop task and remove from timer
             */
            for (final ScheduledTimerTask task : tasks) {
                task.cancel(false);
            }
            tasks = null;
        }
        final TimerService timer = ServerServiceRegistry.getInstance().getService(TimerService.class);
        if (null != timer) {
            timer.purge();
        }
    }

    /**
     * Checks if this registry contains images for the specified session.
     * 
     * @param session The session
     * @param uniqueId The unique ID
     * @return <code>true</code> if this registry contains images for the specified session; otherwise <code>false</code>
     */
    public boolean containsImageData(final Session session, final String uniqueId) {
        return getImageData(session, uniqueId) != null;
    }

    /**
     * Checks if this registry contains images for the specified context.
     * 
     * @param contextId The context ID
     * @param uniqueId The unique ID
     * @return <code>true</code> if this registry contains images for the specified context; otherwise <code>false</code>
     */
    public boolean containsImageData(final int contextId, final String uniqueId) {
        return getImageData(contextId, uniqueId) != null;
    }

    /**
     * Adds specified data source and data arguments as image data to this registry if no matching image data is already contained in
     * registry.
     * 
     * @param session The session to which the image data shall be bound
     * @param imageSource The image source
     * @param imageArguments The image arguments
     * @return Either the new image data from specified data source and data arguments or the existing one if already contained in registry.
     */
    public ImageData addImageData(final Session session, final DataSource imageSource, final DataArguments imageArguments) {
        return addImageData(session, imageSource, imageArguments, ImageData.DEFAULT_TTL);
    }

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
    public ImageData addImageData(final Session session, final DataSource imageSource, final DataArguments imageArguments, final int timeToLive) {
        final String sessionId = session.getSessionID();
        ConcurrentMap<String, ImageData> m = sessionBoundImagesMap.get(sessionId);
        boolean check = true;
        if (m == null) {
            synchronized (sessionBoundImagesMap) {
                m = sessionBoundImagesMap.get(sessionId);
                if (m == null) {
                    m = new ConcurrentHashMap<String, ImageData>();
                    sessionBoundImagesMap.put(sessionId, m);
                    check = false;
                }
            }
        }
        ImageData imageData;
        if (check && (imageData = m.get(imageArguments.getID())) != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Image data fetched from registry for UID: " + imageArguments.getID());
            }
            return imageData.touch();
        }
        imageData = new ImageData(imageSource, imageArguments, timeToLive);
        m.put(imageArguments.getID(), imageData);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Image data put into registry with UID: " + imageArguments.getID());
        }
        return imageData;
    }

    /**
     * Adds specified data source and data arguments as image data to this registry if no matching image data is already contained in
     * registry.
     * 
     * @param contextId The ID of the context to which the image data shall be bound
     * @param imageSource The image source
     * @param imageArguments The image arguments
     * @return Either the new image data from specified data source and data arguments or the existing one if already contained in registry.
     */
    public ImageData addImageData(final int contextId, final DataSource imageSource, final DataArguments imageArguments) {
        return addImageData(contextId, imageSource, imageArguments, ImageData.DEFAULT_TTL);
    }

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
    public ImageData addImageData(final int contextId, final DataSource imageSource, final DataArguments imageArguments, final int timeToLive) {
        final Integer cid = Integer.valueOf(contextId);
        ConcurrentMap<String, ImageData> m = contextBoundImagesMap.get(cid);
        boolean check = true;
        if (m == null) {
            synchronized (contextBoundImagesMap) {
                m = contextBoundImagesMap.get(cid);
                if (m == null) {
                    m = new ConcurrentHashMap<String, ImageData>();
                    contextBoundImagesMap.put(cid, m);
                    check = false;
                }
            }
        }
        ImageData imageData;
        if (check && (imageData = m.get(imageArguments.getID())) != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Image data fetched from registry for UID: " + imageArguments.getID());
            }
            return imageData.touch();
        }
        imageData = new ImageData(imageSource, imageArguments, timeToLive);
        m.put(imageArguments.getID(), imageData);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Image data put into registry with UID: " + imageArguments.getID());
        }
        return imageData;
    }

    /**
     * Adds specified image data to this registry.
     * 
     * @param contextId The ID of the context to which the image data shall be bound
     * @param imageData The image data
     */
    public void addImageData(final int contextId, final ImageData imageData) {
        final Integer cid = Integer.valueOf(contextId);
        ConcurrentMap<String, ImageData> m = contextBoundImagesMap.get(cid);
        if (m == null) {
            synchronized (contextBoundImagesMap) {
                m = contextBoundImagesMap.get(cid);
                if (m == null) {
                    m = new ConcurrentHashMap<String, ImageData>();
                    contextBoundImagesMap.put(cid, m);
                }
            }
        }
        m.put(imageData.getUniqueId(), imageData);
    }

    /**
     * Adds specified image data to this registry.
     * 
     * @param session The session to which the image data shall be bound
     * @param imageData The image data
     */
    public void addImageData(final Session session, final ImageData imageData) {
        final String sessionId = session.getSessionID();
        ConcurrentMap<String, ImageData> m = sessionBoundImagesMap.get(sessionId);
        if (m == null) {
            synchronized (sessionBoundImagesMap) {
                m = sessionBoundImagesMap.get(sessionId);
                if (m == null) {
                    m = new ConcurrentHashMap<String, ImageData>();
                    sessionBoundImagesMap.put(sessionId, m);
                }
            }
        }
        m.put(imageData.getUniqueId(), imageData);
    }

    /**
     * Removes all images bound to specified session.
     * 
     * @param session The session to clean images from
     */
    public void removeImageData(final Session session) {
        sessionBoundImagesMap.remove(session.getSessionID());
    }

    /**
     * Removes all images bound to specified session.
     * 
     * @param contextId The ID of the context to clean images from
     */
    public void removeImageData(final int contextId) {
        contextBoundImagesMap.remove(Integer.valueOf(contextId));
    }

    /**
     * Removes the images with specified unique ID bound to specified context.
     * 
     * @param contextId The ID of the context to clean images from
     * @param uniqueId The unique ID
     * @return <code>true</code> if corresponding image was successfully deleted; otherwise <code>false</code> if not found
     */
    public boolean removeImageData(final int contextId, final String uniqueId) {
        final ConcurrentMap<String, ImageData> m = contextBoundImagesMap.get(Integer.valueOf(contextId));
        if (m == null) {
            return false;
        }
        return m.remove(uniqueId) != null;
    }

    /**
     * Gets all images bound to specified session.
     * 
     * @param session The session to get images from
     * @return All images bound to specified session as an array of {@link ImageData}; an empty array is returned if no images are held for
     *         specified session
     */
    public ImageData[] getImageData(final Session session) {
        final ConcurrentMap<String, ImageData> m = sessionBoundImagesMap.get(session.getSessionID());
        if (m == null) {
            return new ImageData[0];
        }
        final ImageData[] retval = m.values().toArray(new ImageData[m.size()]);
        for (final ImageData imageData : retval) {
            imageData.touch();
        }
        return retval;
    }

    /**
     * Gets all images bound to specified context.
     * 
     * @param contextId The ID of the context to get images from
     * @return All images bound to specified context as an array of {@link ImageData}; an empty array is returned if no images are held for
     *         specified context
     */
    public ImageData[] getImageData(final int contextId) {
        final ConcurrentMap<String, ImageData> m = contextBoundImagesMap.get(Integer.valueOf(contextId));
        if (m == null) {
            return new ImageData[0];
        }
        final ImageData[] retval = m.values().toArray(new ImageData[m.size()]);
        for (final ImageData imageData : retval) {
            imageData.touch();
        }
        return retval;
    }

    /**
     * Gets the image data bound to specified session and registered to specified unique ID.
     * 
     * @param session The session to which the image data is bound
     * @param uniqueId The image data's unique ID
     * @return The image data bound to specified session and registered to specified unique ID, or <code>null</code> if none present
     */
    public ImageData getImageData(final Session session, final String uniqueId) {
        final ConcurrentMap<String, ImageData> m = sessionBoundImagesMap.get(session.getSessionID());
        if (m == null) {
            return null;
        }
        final ImageData imageData = m.get(uniqueId);
        if (imageData == null) {
            return null;
        }
        return imageData.touch();
    }

    /**
     * Gets the image data bound to specified context and registered to specified unique ID.
     * 
     * @param contextId The ID of the context to which the image data is bound
     * @param uniqueId The image data's unique ID
     * @return The image data bound to specified context and registered to specified unique ID, or <code>null</code> if none present
     */
    public ImageData getImageData(final int contextId, final String uniqueId) {
        final ConcurrentMap<String, ImageData> m = contextBoundImagesMap.get(Integer.valueOf(contextId));
        if (m == null) {
            return null;
        }
        final ImageData imageData = m.get(uniqueId);
        if (imageData == null) {
            return null;
        }
        return imageData.touch();
    }

    /**
     * Clears the registry.
     */
    public void clearRegistry() {
        sessionBoundImagesMap.clear();
        contextBoundImagesMap.clear();
    }
}

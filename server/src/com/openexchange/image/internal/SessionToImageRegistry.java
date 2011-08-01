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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link SessionToImageRegistry} - This class provides the functionality to manage mappings between image id's and session id's.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SessionToImageRegistry {

    private final Map<String, String> imageIds2sessionIds;

    private final Map<String, Set<String>> sessionIds2imageIds;

    private final Lock lock;

    private static final SessionToImageRegistry INSTANCE = new SessionToImageRegistry();

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(SessionToImageRegistry.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();


    private SessionToImageRegistry() {
        super();
        imageIds2sessionIds = new HashMap<String, String>();
        sessionIds2imageIds = new HashMap<String, Set<String>>();
        lock = new ReentrantLock();
    }

    public static SessionToImageRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * This method maps a session id to an image id.
     *
     * @param sessionId The session id.
     * @param imageId The unique image id.
     */
    public void put(final String sessionId, final String imageId) {
        if (DEBUG) {
            LOG.debug("Adding mapping for session " + sessionId + " to image " + imageId + ".");
        }

        lock.lock();
        imageIds2sessionIds.put(imageId, sessionId);

        Set<String> imageIdList;
        if (sessionIds2imageIds.containsKey(sessionId)) {
            imageIdList = sessionIds2imageIds.get(sessionId);
        } else {
            imageIdList = new TreeSet<String>();
            sessionIds2imageIds.put(sessionId, imageIdList);
        }
        imageIdList.add(imageId);
        lock.unlock();
    }

    /**
     * Returns the session id that is mapped to an image id.
     * @param imageId The image id.
     * @return The session id or <code>null</code> if there is no mapping for the image id.
     */
    public String getSessionId(final String imageId) {
        return imageIds2sessionIds.get(imageId);
    }

    /**
     * Returns a list of image id's that is mapped to a session id.
     * @param sessionId The session id.
     * @return A list of image id's or <code>null</code> if there is no mapping for the session is.
     */
    public Set<String> getImageIds(final String sessionId) {
        return sessionIds2imageIds.get(sessionId);
    }

    /**
     * Removes all mappings of a session.
     * @param sessionId The session id.
     */
    public void removeBySessionId(final String sessionId) {
        if (DEBUG) {
            LOG.debug("Removing mappings for session " + sessionId + ".");
        }

        boolean error = false;

        lock.lock();
        final Set<String> imageIds = sessionIds2imageIds.remove(sessionId);

        if (imageIds != null) {
            for (final String imageId : imageIds) {
                final boolean removed = imageIds2sessionIds.remove(imageId) == null;
                error |= removed;

                if (removed) {
                    LOG.debug("Successfully removed image " + imageId + "from SessionToImageRegistry for session " + sessionId);
                } else {
                    LOG.debug("Failed to remove image " + imageId + "from SessionToImageRegistry for session " + sessionId);
                }
            }
        }
        lock.unlock();

        if (error) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Removed mapping for session ");
            sb.append(sessionId);
            sb.append(" from session->images map, but one or more images could not be removed from image->session map.");
            sb.append("\nTried to delete these images: ");
            for (final String imageId : imageIds) {
                sb.append("\n" + imageId);
            }

            LOG.error(sb.toString());
        }
    }

    /**
     * Removes a mapping of an image.
     * @param imageId
     */
    public void removeByImageId(final String imageId) {
        if (DEBUG) {
            LOG.debug("Removing mapping for image " + imageId + ".");
        }

        boolean error = false;

        lock.lock();
        final String sessionId = imageIds2sessionIds.remove(imageId);

        if (sessionId != null) {
            final Set<String> imageIdList = sessionIds2imageIds.get(sessionId);
            if (imageIdList == null) {
                error = true;
            } else {
                error = !imageIdList.remove(imageId);
            }
        }
        lock.unlock();

        if (error) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Removed mapping for image ");
            sb.append(imageId);
            sb.append(" from image->session map, but the image could not be removed from sessions->image map.");
            sb.append("\nTried to delete image ");
            sb.append(imageId);

            LOG.error(sb.toString());
        }
    }
}

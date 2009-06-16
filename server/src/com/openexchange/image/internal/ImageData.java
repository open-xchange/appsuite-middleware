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

import java.io.InputStream;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataException;
import com.openexchange.conversion.DataSource;
import com.openexchange.image.servlet.ImageServlet;
import com.openexchange.session.Session;

/**
 * {@link ImageData} - The image data.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImageData {

    static final int DEFAULT_TTL = 300000;

    private final String uniqueId;

    private final DataSource imageSource;

    private final DataArguments imageArguments;

    private final int hash;

    private final String url;

    private long lastAccessed;

    private final int timeToLive;

    /**
     * Initializes a new {@link ImageData} with its unique ID set to {@link DataArguments#getID()} and default time-to-live of 5 minutes.
     * 
     * @param imageSource The image data source
     * @param imageArguments The image arguments
     */
    ImageData(final DataSource imageSource, final DataArguments imageArguments) {
        this(imageSource, imageArguments, DEFAULT_TTL);
    }

    /**
     * Initializes a new {@link ImageData} with its unique ID set to {@link DataArguments#getID()}.
     * 
     * @param imageSource The image data source
     * @param imageArguments The image arguments
     * @param timeToLive The time-to-live in milliseconds; a value less than or equal to zero is an infinite time-to-live
     */
    ImageData(final DataSource imageSource, final DataArguments imageArguments, final int timeToLive) {
        super();
        if (imageArguments == null) {
            throw new IllegalArgumentException("image arguments are null");
        }
        uniqueId = imageArguments.getID();
        this.imageArguments = imageArguments;
        this.imageSource = imageSource;
        this.hash = hashCode0();
        url = new StringBuilder(ImageServlet.ALIAS.length() + ImageServlet.PARAMETER_UID.length() + uniqueId.length() + 3).append('/').append(
            ImageServlet.ALIAS).append('?').append(ImageServlet.PARAMETER_UID).append('=').append(uniqueId).toString();
        this.timeToLive = timeToLive;
        lastAccessed = System.currentTimeMillis();
    }

    /**
     * Checks if specified unique ID matches this image data's unique ID.
     * 
     * @param otherUniqueId The other unique ID to check against
     * @return <code>true</code> if specified unique ID matches this image data's unique ID; otherwise <code>false</code>
     */
    public boolean matchesUniqueID(final String otherUniqueId) {
        return uniqueId.equals(otherUniqueId);
    }

    /**
     * Gets the unique ID (actually {@link DataArguments#getID()} from formerly passed data arguments).
     * 
     * @return The unique ID
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Gets the image data.
     * 
     * @param session The session needed to obtain image's data
     * @return The image data
     * @throws DataException
     */
    public Data<InputStream> getImageData(final Session session) throws DataException {
        return imageSource.getData(InputStream.class, imageArguments, session);
    }

    /**
     * Gets the URL to this image; something like:
     * 
     * <pre>
     * &quot;/ajax/image?uid=1578300019-288076184-517459785&quot;
     * </pre>
     * 
     * @return The URL to this image
     */
    public String getImageURL() {
        return url;
    }

    /**
     * Gets this image data's time-to-live in milliseconds.
     * 
     * @return This image data's time-to-live in milliseconds
     */
    public int getTimeToLive() {
        return timeToLive;
    }

    private int hashCode0() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
        return result;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ImageData other = (ImageData) obj;
        if (uniqueId == null) {
            if (other.uniqueId != null) {
                return false;
            }
        } else if (!uniqueId.equals(other.uniqueId)) {
            return false;
        }
        return true;
    }

    /**
     * Touches the last-accessed time stamp.
     * 
     * @return This image data with its last-accessed time stamp touched
     */
    public ImageData touch() {
        lastAccessed = System.currentTimeMillis();
        return this;
    }

    /**
     * Gets the last-accessed time stamp.
     * 
     * @return The last-accessed time stamp
     */
    public long getLastAccessed() {
        return lastAccessed;
    }
}

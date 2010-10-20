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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.unitedinternet.smartdrive.client.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveDeadProperty;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveResource;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveThumbNail;

/**
 * {@link AbstractSmartDriveResource} - The abstract SmartDrive resource.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractSmartDriveResource implements SmartDriveResource {

    protected String name;

    protected Date creationDate;

    protected Date lastModified;

    protected String downloadToken;

    protected List<SmartDriveDeadProperty> deadProperties;

    protected Map<String, SmartDriveThumbNail> thumbNails;

    /**
     * Initializes a new {@link AbstractSmartDriveResource}.
     */
    protected AbstractSmartDriveResource() {
        super();
    }

    public String getName() {
        return name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public String getDownloadToken() {
        return downloadToken;
    }

    public List<SmartDriveDeadProperty> getDeadProperties() {
        return null == deadProperties ? null : Collections.unmodifiableList(deadProperties);
    }

    public Map<String, SmartDriveThumbNail> getThumbNails() {
        return null == thumbNails ? null : Collections.unmodifiableMap(thumbNails);
    }

    /**
     * Sets the name
     * 
     * @param name The name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the creation date
     * 
     * @param creationDate The creation date to set
     */
    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate == null ? null : new Date(creationDate.getTime());
    }

    /**
     * Sets the last modified
     * 
     * @param lastModified The last modified to set
     */
    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified == null ? null : new Date(lastModified.getTime());
    }

    /**
     * Sets the download token
     * 
     * @param downloadToken The download token to set
     */
    public void setDownloadToken(final String downloadToken) {
        this.downloadToken = downloadToken;
    }

    /**
     * Sets the dead properties
     * 
     * @param deadProperties The dead properties to set
     */
    public void setDeadProperties(final List<? extends SmartDriveDeadProperty> deadProperties) {
        if (null == deadProperties) {
            this.deadProperties = null;
        } else {
            final List<SmartDriveDeadProperty> thisDeadProperties = new ArrayList<SmartDriveDeadProperty>(deadProperties.size());
            for (final SmartDriveDeadProperty deadProperty : deadProperties) {
                thisDeadProperties.add(new SmartDriveDeadProperty(deadProperty));
            }
            this.deadProperties = thisDeadProperties;
        }
    }

    /**
     * Sets the thumb nails
     * 
     * @param thumbNails The thumb nails to set
     */
    public void setThumbNails(final Map<String, ? extends SmartDriveThumbNail> thumbNails) {
        if (null == thumbNails) {
            this.thumbNails = null;
        } else {
            final Map<String, SmartDriveThumbNail> thisMap = new HashMap<String, SmartDriveThumbNail>(thumbNails.size());
            for (final Map.Entry<String, ? extends SmartDriveThumbNail> entry : thumbNails.entrySet()) {
                thisMap.put(entry.getKey(), new SmartDriveThumbNailImpl(entry.getValue()));
            }
            this.thumbNails = thisMap;
        }
    }

}

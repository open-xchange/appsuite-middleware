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

package com.openexchange.unitedinternet.smartdrive.client;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * {@link SmartDriveResource} -
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SmartDriveResource {

    /**
     * Gets the name.
     * 
     * @return The name
     */
    String getName();

    /**
     * Gets the creation date.
     * 
     * @return The creation date
     */
    Date getCreationDate();

    /**
     * Gets the last-modified date.
     * 
     * @return The last-modified date
     */
    Date getLastModified();

    /**
     * Gets the download token.
     * 
     * @return The download token
     */
    String getDownloadToken();

    /**
     * Gets the dead properties.
     * 
     * @return The dead properties
     */
    List<SmartDriveDeadProperty> getDeadProperties();

    /**
     * Gets the <b>optional</b> map of thumb nails.
     * 
     * @return The <b>optional</b> map of thumb nails
     */
    Map<String, SmartDriveThumbNail> getThumbNails();
    
    /**
     * Find out whether this resource is a directory
     * @return <code>true</code> if this resource is a directory, <code>false</code> otherwise
     */
    boolean isDirectory();
    
    /**
     * Turns this resource into a directory. Check with {@link #isDirectory()} beforehand.
     * @return This resource as a directory
     * @throws SmartDriveException - If this resource is a file
     */
    SmartDriveDirectory toDirectory() throws SmartDriveException;
    
    /**
     * Turns this resource into a file. Check with {@link #isDirectory()} beforehand.
     * @return This resource as a file
     * @throws SmartDriveException - If this resource is a directory
     */
    SmartDriveFile toFile() throws SmartDriveException;

}

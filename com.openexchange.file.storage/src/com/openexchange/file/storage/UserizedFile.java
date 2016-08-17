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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.file.storage;


/**
 * A {@link File} is usually requested in the name of a certain user. Thus the contained
 * information is potentially a user-centric view of that file and not globally valid. In
 * some situations it can become necessary to access parts of the contained information
 * from a non-user-centric perspective. This interface is meant to provide access to these
 * parts.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface UserizedFile extends File {

    /**
     * If this file is contained in a virtual folder of the user in question, it's global
     * ID might differ from the user-centric one. This method returns the original (i.e.
     * globally valid) ID. The user-centric ID is returned by {@link #getId()}.
     *
     * @return The original file ID; may be the same as {@link #getId()}, if the user-centric
     * ID is non-virtual.
     */
    String getOriginalId();

    /**
     * Sets the original file ID if the one set via {@link #setId(String)} is virtual.
     *
     * @param id The original ID; not <code>null</code>
     */
    void setOriginalId(String id);

    /**
     * If this file is contained in a virtual folder of the user in question, the folder
     * ID returned via {@link #getFolderId()} is the one of the virtual folder, but not the
     * one of the original folder where the file is physically located. This method returns
     * the original (i.e. physical) folder ID.
     *
     * @return The original folder ID; may be the same as {@link #getFolderId()}, if the user-
     * centric folder ID is non-virtual.
     */
    String getOriginalFolderId();

    /**
     * Sets the original folder ID if the one set via {@link #setFolderId(String)} is virtual.
     *
     * @param id The original ID; not <code>null</code>
     */
    void setOriginalFolderId(String id);

}

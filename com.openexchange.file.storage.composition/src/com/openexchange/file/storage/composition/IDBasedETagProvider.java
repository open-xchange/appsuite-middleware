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

package com.openexchange.file.storage.composition;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link IDBasedETagProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Deprecated
public interface IDBasedETagProvider {

    /**
     * Gets a value indicating whether the ETags delivered by this storage can be assumed to be recursive or not. When being "recursive",
     * a changed ETag of a subfolder will result in changed ETags of all parent folders recursively.
     * <p/>
     * <b>Note: </b>Only available if {@link IDBasedETagProvider#supportsETags} is <code>true</code>.
     *
     * @param folderId The folder to check
     * @return <code>true</code> if ETags delivered by this storage are recursive, <code>false</code>, otherwise.
     * @throws OXException
     */
    boolean isRecursive(String folderId) throws OXException;

    /**
     * Gets the ETags for the supplied folders to quickly determine which folders contain changes. An updated ETag in a folder indicates a
     * change, for example a new, modified or deleted file. If {@link IDBasedETagProvider#isRecursive()} is <code>true</code>, an
     * updated ETag may also indicate a change in one of the folder's subfolders.
     * <p/>
     * <b>Note: </b>Only available if {@link IDBasedETagProvider#supportsETags} is <code>true</code>.
     *
     * @param folderIds A list of folder IDs to get the ETags for
     * @return A map holding the resulting ETags to each requested folder ID
     * @throws OXException
     */
    Map<String, String> getETags(List<String> folderIds) throws OXException;

    /**
     * Gets a value indicating whether sequence numbers are supported by the given folder.
     *
     * @param folderId The folder to check
     * @return <code>true</code> if sequence numbers are supported, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean supportsETags(String folderId) throws OXException;

}

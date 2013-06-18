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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.file.storage.json.actions.files;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link InfostoreRequest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface InfostoreRequest {

    /**
     * Gets the value mapped to given parameter name.
     * 
     * @param name The parameter name
     * @return The value mapped to given parameter name or <code>null</code> if not present
     * @throws NullPointerException If name is <code>null</code>
     */
    public String getParameter(final String name);

    /**
     * Gets the boolean value mapped to given parameter name.
     * 
     * @param name The parameter name
     * @return The boolean value mapped to given parameter name or <code>false</code> if not present
     * @throws NullPointerException If name is <code>null</code>
     */
    public boolean getBoolParameter(final String name);

    InfostoreRequest require(AbstractFileAction.Param...params) throws OXException ;

    InfostoreRequest requireBody() throws OXException;

    InfostoreRequest requireFileMetadata() throws OXException;

    public IDBasedFileAccess getFileAccess() throws OXException;

    String getId() throws OXException;

    String getVersion() throws OXException;

    String getFolderId() throws OXException;

    List<Field> getColumns() throws OXException;

    Field getSortingField() throws OXException;

    SortDirection getSortingOrder() throws OXException;

    TimeZone getTimezone() throws OXException;

    ServerSession getSession() throws OXException;

    long getTimestamp() throws OXException;

    Set<String> getIgnore() throws OXException;

    List<String> getIds() throws OXException;

    String getFolderForID(String id) throws OXException;

    String[] getVersions() throws OXException;

    long getDiff() throws OXException;

    String getSearchQuery() throws OXException;

    String getSearchFolderId() throws OXException;

    int getStart() throws OXException;

    int getEnd() throws OXException;

    File getFile() throws OXException;

    List<File.Field> getSentColumns() throws OXException;

    boolean hasUploads() throws OXException;

    InputStream getUploadedFileData() throws OXException;

    /**
     * Gets the upload stream. Retrieves the body of the request as binary data as an {@link InputStream}.
     *
     * @return The upload stream or <code>null</code> if not available
     * @throws OXException If an I/O error occurs
     */
    InputStream getUploadStream() throws OXException;

    int getAttachedId();

    int getModule();

    int getAttachment();

    AttachmentBase getAttachmentBase();

    String getFolderAt(int i);

    List<String> getFolders();

	boolean isForSpecificVersion();

    boolean extendedResponse() throws OXException;
}

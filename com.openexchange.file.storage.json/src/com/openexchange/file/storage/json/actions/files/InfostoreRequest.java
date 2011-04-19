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

package com.openexchange.file.storage.json.actions.files;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.json.actions.files.AbstractFileAction.Param;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link InfostoreRequest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface InfostoreRequest {

    InfostoreRequest require(Param...params) throws AjaxException ;
    
    InfostoreRequest requireBody() throws AjaxException;

    InfostoreRequest requireFileMetadata() throws AjaxException;

    public IDBasedFileAccess getFileAccess() throws AbstractOXException;

    String getId() throws AbstractOXException;

    int getVersion() throws AbstractOXException;
    
    String getFolderId() throws AbstractOXException;

    List<Field> getColumns() throws AbstractOXException;

    Field getSortingField() throws AbstractOXException;

    SortDirection getSortingOrder() throws AbstractOXException;
 
    TimeZone getTimezone() throws AbstractOXException;
    
    ServerSession getSession() throws AjaxException;

    long getTimestamp() throws AjaxException;

    Set<String> getIgnore() throws AjaxException;

    List<String> getIds() throws AjaxException;

    String getFolderForID(String id) throws AjaxException;

    int[] getVersions() throws AjaxException;

    long getDiff() throws AbstractOXException;

    String getSearchQuery() throws AbstractOXException;

    String getSearchFolderId() throws AbstractOXException;

    int getStart() throws AbstractOXException;

    int getEnd() throws AbstractOXException;

    File getFile() throws AbstractOXException;

    List<File.Field> getSentColumns() throws AbstractOXException;
    
    public boolean hasUploads() throws AbstractOXException;
    
    public InputStream getUploadedFileData() throws AbstractOXException;

    int getAttachedId();

    int getModule();

    int getAttachment();

    AttachmentBase getAttachmentBase();

    String getFolderAt(int i);

    List<String> getFolders();
}

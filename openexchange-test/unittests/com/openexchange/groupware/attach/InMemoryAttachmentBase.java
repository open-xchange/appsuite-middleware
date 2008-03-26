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
package com.openexchange.groupware.attach;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.api2.OXException;

import java.io.InputStream;
import java.util.*;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class InMemoryAttachmentBase implements AttachmentBase{
    private Map<Context, Map<Integer, AttachmentMetadata>> data = new HashMap<Context, Map<Integer, AttachmentMetadata>>();
    private Map<Context, List<AttachmentMetadata>> changes = new HashMap<Context, List<AttachmentMetadata>>();
    private Map<Context, List<AttachmentMetadata>> deletions = new HashMap<Context, List<AttachmentMetadata>>();
    
    public long attachToObject(AttachmentMetadata attachment, InputStream data, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public long detachFromObject(int folderId, int objectId, int moduleId, int[] ids, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public AttachmentMetadata getAttachment(int folderId, int objectId, int moduleId, int id, Context ctx, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public InputStream getAttachedFile(int folderId, int attachedId, int moduleId, int id, Context context, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public SortedSet<String> getAttachmentFileStoreLocationsperContext(Context ctx) {
        SortedSet<String> locations = new TreeSet<String>();
        for(AttachmentMetadata metadata : getCtxMap(ctx).values()) {
            locations.add(metadata.getFileId());
        }
        return locations;
    }

    public TimedResult getAttachments(int folderId, int attachedId, int moduleId, Context context, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public TimedResult getAttachments(int folderId, int attachedId, int moduleId, AttachmentField[] columns, AttachmentField sort, int order, Context context, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public TimedResult getAttachments(int folderId, int attachedId, int moduleId, int[] idsToFetch, AttachmentField[] fields, Context context, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public Delta getDelta(int folderId, int attachedId, int moduleId, long ts, boolean ignoreDeleted, Context context, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public Delta getDelta(int folderId, int attachedId, int moduleId, long ts, boolean ignoreDeleted, AttachmentField[] fields, AttachmentField sort, int order, Context context, User user, UserConfiguration userConfig) throws OXException {
        throw new UnsupportedOperationException();
    }

    public void registerAttachmentListener(AttachmentListener listener, int moduleId) {
        throw new UnsupportedOperationException();
    }

    public void removeAttachmentListener(AttachmentListener listener, int moduleId) {
        throw new UnsupportedOperationException();
    }

    public int[] removeAttachment(String file_id, Context ctx) throws OXException {
        for(AttachmentMetadata attachment : getCtxMap(ctx).values()) {
            String location = attachment.getFileId();
            if(location != null && location.equals(file_id)){
                deletions.get(ctx).add(attachment);
                return new int[]{1,1};
            }
        }
        return new int[]{1,1};
    }

    public int modifyAttachment(String file_id, String new_file_id, String new_comment, String new_mime, Context ctx) throws OXException {
        for(AttachmentMetadata attachment : getCtxMap(ctx).values()) {
            String location = attachment.getFileId();
            if(location != null && location.equals(file_id)){
                attachment.setFileId(new_file_id);
                attachment.setComment(new_comment);
                attachment.setFileMIMEType(new_mime);
                changes.get(ctx).add(attachment);
                return attachment.getId();
            }
        }
        return -1;
    }

    public void addAuthorization(AttachmentAuthorization authz, int moduleId) {
        throw new UnsupportedOperationException();
    }

    public void removeAuthorization(AttachmentAuthorization authz, int moduleId) {
        throw new UnsupportedOperationException();
    }

    public void deleteAll(Context context) throws OXException {
        throw new UnsupportedOperationException();
    }

    public void startTransaction() throws TransactionException {
        //IGNORE
    }

    public void commit() throws TransactionException {
        //IGNORE
    }

    public void rollback() throws TransactionException {
        throw new UnsupportedOperationException();
    }

    public void finish() throws TransactionException {
        //IGNORE
    }

    public void setTransactional(boolean transactional) {
        //IGNORE
    }

    public void setRequestTransactional(boolean transactional) {
        throw new UnsupportedOperationException();
    }

    public void put(Context ctx, AttachmentMetadata attachment) {
        getCtxMap(ctx).put(attachment.getId(), attachment);
    }

    private Map<Integer, AttachmentMetadata> getCtxMap(Context ctx) {
        if(data.containsKey(ctx)) {
            return data.get(ctx);
        }
        Map<Integer, AttachmentMetadata> attachments = new HashMap<Integer, AttachmentMetadata>();
        data.put(ctx, attachments);
        return attachments;
    }

    public void forgetChanges(Context ctx) {
        changes.put(ctx, new ArrayList<AttachmentMetadata>());
    }

    public List<AttachmentMetadata> getChanges(Context ctx) {
        if(!changes.containsKey(ctx)) {
            return new ArrayList<AttachmentMetadata>();
        }
        return changes.get(ctx);
    }

    public void forgetDeletions(Context ctx) {
        deletions.put(ctx, new ArrayList<AttachmentMetadata>());
    }

    public List<AttachmentMetadata> getDeletions(Context ctx) {
        return deletions.get(ctx);
    }
}

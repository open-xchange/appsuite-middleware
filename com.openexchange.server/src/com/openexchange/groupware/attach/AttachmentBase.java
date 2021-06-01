/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.groupware.attach;

import java.io.InputStream;
import java.util.Date;
import java.util.SortedSet;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.tx.TransactionAware;
import com.openexchange.tx.TransactionException;
import com.openexchange.user.User;
import gnu.trove.map.TIntObjectMap;

public interface AttachmentBase extends TransactionAware {

    public static final int NEW = 0;

    public static final int ASC = 1;
    public static final int DESC = -1;

    /**
     * @return the timestamp of modified objects to which this attachment was attached
     * @throws TransactionException
     */
    public abstract long attachToObject(AttachmentMetadata attachment, InputStream data, Session session, Context ctx, User user, UserConfiguration userConfig) throws OXException;

    /**
     * @return the timestamp of modified objects to which these attachments were detached
     * @throws TransactionException
     */
    public abstract long detachFromObject(int folderId, int objectId, int moduleId, int[] ids, Session session, Context ctx, User user, UserConfiguration userConfig) throws OXException;

    public abstract AttachmentMetadata getAttachment(Session session, int folderId, int objectId, int moduleId, int id, Context ctx, User user, UserConfiguration userConfig) throws OXException;

    public abstract InputStream getAttachedFile(Session session, int folderId, int attachedId, int moduleId, int id, Context context, User user, UserConfiguration userConfig)  throws OXException;

    /**
     * This method is used to get all file_ids which are refered to by attachments. This is used by the consistency tool at the moment
     *
     * @param ctx The Context
     * @return A sorted set of the file_ids
     */
    public abstract SortedSet<String> getAttachmentFileStoreLocationsperContext(Context ctx) throws OXException;

    /**
     * This method is used to get all file_ids which are refered to by attachments. This is used by the consistency tool at the moment
     *
     * @param ctx The Context
     * @param user the user
     * @return A sorted set of the file_ids
     */
    public abstract SortedSet<String> getAttachmentFileStoreLocationsPerUser(Context ctx, User user) throws OXException;

    public abstract TimedResult<AttachmentMetadata> getAttachments(Session session, int folderId, int attachedId, int moduleId, Context context, User user, UserConfiguration userConfig) throws OXException;

    public abstract TimedResult<AttachmentMetadata> getAttachments(Session session, int folderId, int attachedId, int moduleId, AttachmentField[] columns, AttachmentField sort, int order, Context context, User user, UserConfiguration userConfig)  throws OXException;

    public abstract TimedResult<AttachmentMetadata> getAttachments(Session session, int folderId, int attachedId, int moduleId, int[] idsToFetch, AttachmentField[] fields, Context context, User user, UserConfiguration userConfig) throws OXException;

    public abstract Delta<AttachmentMetadata> getDelta(Session session, int folderId, int attachedId, int moduleId, long ts, boolean ignoreDeleted, Context context, User user, UserConfiguration userConfig) throws OXException;

    public abstract Delta<AttachmentMetadata> getDelta(Session session, int folderId, int attachedId, int moduleId, long ts, boolean ignoreDeleted, AttachmentField[] fields, AttachmentField sort, int order, Context context, User user, UserConfiguration userConfig) throws OXException;

    public abstract void registerAttachmentListener(AttachmentListener listener, int moduleId);

    public abstract void removeAttachmentListener(AttachmentListener listener, int moduleId);

    /**
     * This method removed the element with the given file_id in the given context. It doesn't store the deleted entries in the del_attachment table.
     * At the moment this method is used by the consistency tool only
     * @param file_id A String of the file identifier
     * @param ctx The Context
     * @return The number of inserted entries in del_attachment in int[0] and the number of removed entries from prg_attachment in int[1]
     * @throws OXException
     * @throws TransactionException
     */
    public abstract int[] removeAttachment(String file_id, Context ctx) throws OXException;

    /**
     * This method is used to modify the attachment with the given file_id in the given context. In this entry the new_* are updated. At the moment
     * this method is used by the consistency tool only
     * @param file_id A String of the file identifier
     * @param new_file_id The new file identifier to store
     * @param new_comment The new comment to store (Attention this is attached to the old one)
     * @param new_mime The new mimetype to store
     * @param ctx The Context
     * @return The number of changed entries
     * @throws OXException
     * @throws TransactionException
     */
    public abstract int modifyAttachment(String file_id, String new_file_id, String new_comment, String new_mime, Context ctx) throws OXException;

    public abstract void addAuthorization(AttachmentAuthorization authz, int moduleId);

    public abstract void removeAuthorization(AttachmentAuthorization authz, int moduleId);

    /**
     * Delete all Attachments in a Context.
     * @param context
     */
    public abstract void deleteAll(Context context) throws OXException;

    /**
     * @return the last modified date of the newest attachment or <code>null</code> if no attachments exist.
     * @throws OXException if some problem occurs.
     */
    public abstract Date getNewestCreationDate(Context ctx, int moduleId, int attachedId) throws OXException;

    public abstract TIntObjectMap<Date> getNewestCreationDates(Context ctx, int moduleId, int[] attachedIds) throws OXException;
}

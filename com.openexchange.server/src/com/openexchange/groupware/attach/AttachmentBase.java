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

package com.openexchange.groupware.attach;

import gnu.trove.map.TIntObjectMap;
import java.io.InputStream;
import java.util.Date;
import java.util.SortedSet;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.tx.TransactionAware;
import com.openexchange.tx.TransactionException;

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
     * This method is used to get alle file_ids which are refered to by attachments. This is used by the consistency tool at the moment
     * @param ctx The Context
     * @return A sorted set of the file_ids
     */
    public abstract SortedSet<String> getAttachmentFileStoreLocationsperContext(Context ctx) throws OXException;

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

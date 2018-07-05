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

package com.openexchange.groupware.delete;

import java.util.EventObject;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

/**
 * {@link DeleteEvent} - The event containing the entity to delete.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DeleteEvent extends EventObject {

    private static final long serialVersionUID = 2636570955675454470L;

    /**
     * USER type constant
     */
    public static final int TYPE_USER = 1;

    /**
     * GROUP type constant
     */
    public static final int TYPE_GROUP = 2;

    /**
     * RESOURCE type constant
     */
    public static final int TYPE_RESOURCE = 3;

    /**
     * RESOURCE_GROUP type constant TODO Remove because we do not have resource groups.
     */
    public static final int TYPE_RESOURCE_GROUP = 4;

    /**
     * CONTEXT type constant
     */
    public static final int TYPE_CONTEXT = 5;

    /**
     * The numerical constant for the anonymous guest users
     */
    public static final int SUBTYPE_ANONYMOUS_GUEST = 101;

    /**
     * The numerical constant for the invited guest users
     */
    public static final int SUBTYPE_INVITED_GUEST = 102;

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates a delete event for context deletion.
     *
     * @param source The source object, which invokes the deletion
     * @param contextId The context identifier
     * @param userIds The identifiers of the remaining users in the context
     * @return The delete event
     * @throws OXException If context cannot be loaded for given identifier
     */
    public static DeleteEvent createDeleteEventForContextDeletion(Object source, int contextId, List<Integer> userIds) throws OXException {
        return new DeleteEvent(source, contextId, TYPE_CONTEXT, 0, ContextStorage.getInstance().getContext(contextId), null, userIds);
    }

    /**
     * Creates a delete event for context deletion.
     *
     * @param source The source object, which invokes the deletion
     * @param context The context
     * @param userIds The identifiers of the remaining users in the context
     * @return The delete event
     */
    public static DeleteEvent createDeleteEventForContextDeletion(Object source, Context context, List<Integer> userIds) {
        return new DeleteEvent(source, context.getContextId(), TYPE_CONTEXT, 0, context, null, userIds);
    }

    /**
     * Creates a delete event for user deletion.
     *
     * @param source The source object, which invokes the deletion
     * @param userId The user identifier
     * @param context The context
     * @param destUserID The identifier of the user to reassign shared/public data to, <code>null</code> to reassign to the context administrator (default), or <code>0</code> to not reassign at all
     * @return The delete event
     */
    public static DeleteEvent createDeleteEventForUserDeletion(Object source, int userId, Context context) {
        return createDeleteEventForUserDeletion(source, userId, 0, context, null);
    }

    /**
     * Creates a delete event for user deletion.
     *
     * @param source The source object, which invokes the deletion
     * @param userId The user identifier
     * @param context The context
     * @param destUserID The identifier of the user to reassign shared/public data to, <code>null</code> to reassign to the context administrator (default), or <code>0</code> to not reassign at all
     * @return The delete event
     */
    public static DeleteEvent createDeleteEventForUserDeletion(Object source, int userId, Context context, Integer destUserID) {
        return createDeleteEventForUserDeletion(source, userId, 0, context, destUserID);
    }

    /**
     * Creates a delete event for user deletion.
     *
     * @param source The source object, which invokes the deletion
     * @param userId The user identifier
     * @param subType The object's subtype, or <code>0</code> if not specified
     * @param context The context
     * @param destUserID The identifier of the user to reassign shared/public data to, <code>null</code> to reassign to the context administrator (default), or <code>0</code> to not reassign at all
     * @return The delete event
     */
    public static DeleteEvent createDeleteEventForUserDeletion(Object source, int userId, int subType, Context context, Integer destUserID) {
        return new DeleteEvent(source, userId, TYPE_USER, subType, context, destUserID, null);
    }

    /**
     * Creates a delete event for group deletion.
     *
     * @param source The source object, which invokes the deletion
     * @param groupId The group identifier
     * @param contextId The context identifier
     * @return The delete event
     * @throws OXException If context cannot be loaded
     */
    public static DeleteEvent createDeleteEventForGroupDeletion(Object source, int groupId, int contextId) throws OXException {
        return new DeleteEvent(source, groupId, TYPE_GROUP, 0, ContextStorage.getInstance().getContext(contextId), null, null);
    }

    /**
     * Creates a delete event for group deletion.
     *
     * @param source The source object, which invokes the deletion
     * @param groupId The group identifier
     * @param context The context
     * @return The delete event
     */
    public static DeleteEvent createDeleteEventForGroupDeletion(Object source, int groupId, Context context) {
        return new DeleteEvent(source, groupId, TYPE_GROUP, 0, context, null, null);
    }

    /**
     * Creates a delete event for resource deletion.
     *
     * @param source The source object, which invokes the deletion
     * @param resourceId The resource identifier
     * @param contextId The context identifier
     * @return The delete event
     * @throws OXException If context cannot be loaded
     */
    public static DeleteEvent createDeleteEventForResourceDeletion(Object source, int resourceId, int contextId) throws OXException {
        return new DeleteEvent(source, resourceId, TYPE_RESOURCE, 0, ContextStorage.getInstance().getContext(contextId), null, null);
    }

    /**
     * Creates a delete event for resource deletion.
     *
     * @param source The source object, which invokes the deletion
     * @param resourceId The resource identifier
     * @param context The context
     * @return The delete event
     */
    public static DeleteEvent createDeleteEventForResourceDeletion(Object source, int resourceId, Context context) {
        return new DeleteEvent(source, resourceId, TYPE_RESOURCE, 0, context, null, null);
    }

    // ---------------------------------------------------------------------------------------------------------------

    private transient final Context ctx;
    protected int id;
    protected int type;
    protected int subType;
    protected Integer destUserID;
    private transient Session session;
    private final List<Integer> userIds;

    /**
     * Initializes a new {@link DeleteEvent}.
     *
     * @param source the object on which the Event initially occurred
     * @param id the object's ID
     * @param type the object's type; either <code>{@link #TYPE_USER}</code>, <code>{@link #TYPE_GROUP}</code>,
     *            <code>{@link #TYPE_RESOURCE}</code>, <code>{@value #TYPE_RESOURCE_GROUP}</code>, or <code>{@value #TYPE_CONTEXT}</code>
     * @param subType The object's subtype, or <code>0</code> if not specified
     * @param ctx the context
     * @param destUserID The identifier of the user to reassign shared/public data to, <code>null</code> to reassign to the context admin
     *        (default), or <code>0</code> to not reassign at all
     * @param The user identifiers in case of context deletion
     */
    private DeleteEvent(final Object source, final int id, final int type, int subType, final Context ctx, Integer destUserID, List<Integer> userIds) {
        super(source);
        this.id = id;
        this.type = type;
        this.subType = subType;
        this.ctx = ctx;
        this.destUserID = destUserID;
        this.userIds = userIds;
    }

    /**
     * Gets the user identifiers.
     * <p>
     * Only returns a valid value in case this event's type is {@link #TYPE_CONTEXT}.
     *
     * @return The user identifiers or <code>null</code>
     */
    public List<Integer> getUserIds() {
        return userIds;
    }

    /**
     * Getter for context.
     *
     * @return the context
     */
    public Context getContext() {
        return ctx;
    }

    /**
     * Getter for the unique ID of entity that shall be deleted.
     *
     * @return the unique ID of entity that shall be deleted
     * @see <code>getType()</code> to determine entity type
     */
    public int getId() {
        return id;
    }

    /**
     * Check return value against public constants <code>{@link #TYPE_USER}</code>, <code>{@link #TYPE_GROUP}</code>,
     * <code>{@link #TYPE_RESOURCE}</code>, <code>{@value #TYPE_RESOURCE_GROUP}</code>, and <code>{@value #TYPE_CONTEXT}</code>.
     *
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * After checking the type via {@link #getType()}, you may also check against the subtype constants declard in this class.
     *
     * @return The subtype, or <code>0</code> if not specified
     */
    public int getSubType() {
        return subType;
    }

    /**
     * Getter for the instance of {@link Session} belonging to context's admin.
     *
     * @return an instance of {@link Session} belonging to context's admin
     */
    public Session getSession() {
        if (session == null) {
            session = SessionObjectWrapper.createSessionObject(ctx.getMailadmin(), ctx, "DeleteEventSessionObject");
        }
        return session;
    }

    /**
     * Gets the identifier of the user that should be used as target account for preserved (e.g. shared or public) data of the deleted
     * user. If set to <code>null</code>, the context admin should be used as default, if set to <code>0</code>, no data should be
     * reassigned.
     *
     * @return The identifier of the user to reassign the data to, <code>null</code> to reassign to the context admin (default), or
     *         <code>0</code> to not reassign at all.
     */
    public Integer getDestinationUserID() {
        return destUserID;
    }

}

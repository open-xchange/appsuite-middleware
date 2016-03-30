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

package com.openexchange.resource.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceEventConstants;
import com.openexchange.resource.ResourceExceptionCode;
import com.openexchange.resource.storage.ResourceStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link ResourceUpdate} - Performs update of a {@link Resource resource}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResourceUpdate {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ResourceUpdate.class);

    private final User user;

    private final Context ctx;

    private final Resource resource;

    private final ResourceStorage storage;

    private final Date clientLastModified;

    private transient Resource orig;

    /**
     * Initializes a new {@link ResourceUpdate}
     *
     * @param ctx The context
     * @param resource The resource to update
     * @param clientLastModified The client last-modified timestamp; may be <code>null</code> to omit timestamp comparison
     */
    ResourceUpdate(final User user, final Context ctx, final Resource resource, final Date clientLastModified) {
        super();
        this.user = user;
        this.ctx = ctx;
        this.resource = resource;
        this.clientLastModified = clientLastModified;
        storage = ResourceStorage.getInstance();
    }

    private Resource getOrig() throws OXException {
        if (null == orig) {
            orig = storage.getResource(resource.getIdentifier(), ctx);
        }
        return orig;
    }

    /**
     * Performs the update.
     * <ol>
     * <li>At first all necessary checks are performed: data completeness, data validation, permission, and check for duplicate resources.</li>
     * <li>Then the transaction-bounded update in storage takes place</li>
     * <li>At last, the update is propagated to system (cache invalidation, etc.)</li>
     * </ol>
     *
     * @throws OXException If update fails
     */
    void perform() throws OXException {
        allow();
        check();
        update();
        propagate();
        sentEvent();
    }

    /**
     * Checks permission
     *
     * @throws OXException If permission is denied
     */
    private void allow() throws OXException {
        /*
         * At the moment security service is not used for timing reasons but is ought to be used later on
         */
        if (!UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), ctx).isEditResource()) {
            throw ResourceExceptionCode.PERMISSION.create(Integer.valueOf(ctx.getContextId()));
        }
        /*
         * TODO: Remove statements above and replace with commented call below
         */
        // checkBySecurityService();
    }

    /**
     * Checks permission: Invoke {@link BundleAccessSecurityService#checkPermission(String[], String) checkPermission()} on
     * {@link BundleAccessSecurityService security service}
     *
     * @throws OXException If permission is not granted
     */
    // private void checkBySecurityService() throws OXException {
    // final BundleAccessSecurityService securityService = ServerServiceRegistry.getInstance().getService(
    // BundleAccessSecurityService.class);
    // if (securityService == null) {
    // throw new OXException(new OXException(OXException.Code.SERVICE_UNAVAILABLE,
    // BundleAccessSecurityService.class.getName()));
    // }
    // final Set<String> permissions = user.getAttributes().get("permission");
    // try {
    // securityService.checkPermission(permissions == null ? null : permissions.toArray(new String[permissions
    // .size()]), PATH);
    // } catch (final BundleAccessException e) {
    // throw new OXException(e);
    // }
    // }
    /**
     * This method performs all necessary checks before updating a resource.
     *
     * @throws OXException If a problem was detected during checks.
     */
    private void check() throws OXException {
        if (null == resource) {
            throw ResourceExceptionCode.NULL.create();
        }
        /*
         * Check mandatory fields
         */
        if (!resource.isIdentifierSet() || -1 == resource.getIdentifier()) {
            throw ResourceExceptionCode.MANDATORY_FIELD.create();
        }
        /*
         * Check existence
         */
        getOrig();
        /*
         * Check timestamp
         */
        if (clientLastModified != null && clientLastModified.getTime() < getOrig().getLastModified().getTime()) {
            throw ResourceExceptionCode.CONCURRENT_MODIFICATION.create(resource.getSimpleName());
        }
        if (resource.isSimpleNameSet()) {
            if (com.openexchange.java.Strings.isEmpty(resource.getSimpleName())) {
                throw ResourceExceptionCode.MANDATORY_FIELD.create();
            }
            if (!ResourceTools.validateResourceIdentifier(resource.getSimpleName())) {
                throw ResourceExceptionCode.INVALID_RESOURCE_IDENTIFIER.create(resource.getSimpleName());
            }
            final Resource[] resources = storage.searchResources(resource.getSimpleName(), ctx);
            if (resources.length > 1) {
                throw ResourceExceptionCode.RESOURCE_CONFLICT.create(resource.getSimpleName());
            } else if (resources.length == 1 && resources[0].getIdentifier() != resource.getIdentifier()) {
                throw ResourceExceptionCode.RESOURCE_CONFLICT.create(resource.getSimpleName());
            }
        }
        if (resource.isMailSet()) {
            if (com.openexchange.java.Strings.isEmpty(resource.getMail())) {
                throw ResourceExceptionCode.MANDATORY_FIELD.create();
            }
            if (!ResourceTools.validateResourceEmail(resource.getMail())) {
                throw ResourceExceptionCode.INVALID_RESOURCE_MAIL.create(resource.getMail());
            }
            final Resource[] resources = storage.searchResourcesByMail(resource.getMail(), ctx);
            if (resources.length > 1) {
                throw ResourceExceptionCode.RESOURCE_CONFLICT_MAIL.create(resource.getMail());
            } else if (resources.length == 1 && resources[0].getIdentifier() != resource.getIdentifier()) {
                throw ResourceExceptionCode.RESOURCE_CONFLICT_MAIL.create(resource.getMail());
            }
        }
    }

    /**
     * Updates all data for the resource in database.
     *
     * @throws OXException
     */
    private void update() throws OXException {
        final Connection con = DBPool.pickupWriteable(ctx);
        try {
            con.setAutoCommit(false);
            update(con);
            con.commit();
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw ResourceExceptionCode.SQL_ERROR.create(e);
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (final SQLException e) {
                LOG.error("Problem setting autocommit to true.", e);
            }
            DBPool.closeWriterSilent(ctx, con);
        }
    }

    /**
     * Propagates insertion to system: Possible cache invalidation, etc.
     */
    private void propagate() {
        // TODO: Check if any caches should be invalidated
    }

    /**
     * This method calls the plain update methods.
     *
     * @param con writable database connection in transaction or not.
     * @throws OXException if some problem occurs.
     */
    void update(final Connection con) throws OXException {
        /*
         * Fill missing values to obtain a completely filled resource
         */
        resource.fill(getOrig());
        storage.updateResource(ctx, con, resource);
    }

    private void sentEvent() {
        final EventAdmin eventAdmin = ServerServiceRegistry.getInstance().getService(EventAdmin.class);
        if (null != eventAdmin) {
            final Dictionary<String, Object> dict = new Hashtable<String, Object>(4);
            dict.put(ResourceEventConstants.PROPERTY_CONTEXT_ID, Integer.valueOf(ctx.getContextId()));
            dict.put(ResourceEventConstants.PROPERTY_USER_ID, Integer.valueOf(user.getId()));
            dict.put(ResourceEventConstants.PROPERTY_RESOURCE_ID, Integer.valueOf(resource.getIdentifier()));
            eventAdmin.postEvent(new Event(ResourceEventConstants.TOPIC_UPDATE, dict));
        }

    }
}

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

package com.openexchange.admin.rmi.impl;

import org.osgi.framework.BundleContext;
import com.openexchange.admin.plugins.OXContextPluginInterface;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.SchemaSelectStrategy;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.PluginInterfaces;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.tools.GenericChecks;

public abstract class OXContextCommonImpl extends OXCommonImpl {

    private final static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OXContextCommonImpl.class);

    /** The bundle context */
    protected final BundleContext context;

    /**
     * Initializes a new {@link OXContextCommonImpl}.
     *
     * @param context The bundle context
     */
    protected OXContextCommonImpl(final BundleContext context) {
        super();
        this.context = context;
    }

    protected void createchecks(final Context ctx, final User admin_user, final OXToolStorageInterface tool) throws StorageException, ContextExistsException, InvalidDataException {

        try {
            Boolean ret = null;

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface oxContextPlugin : pluginInterfaces.getContextPlugins().getServiceList()) {
                        ret = oxContextPlugin.checkMandatoryMembersContextCreate(ctx);
                    }
                }
            }

            if (ret == null || (ret != null && ret.booleanValue())) {
                if (!ctx.mandatoryCreateMembersSet()) {
                    throw new InvalidDataException("Mandatory fields in context not set: " + ctx.getUnsetMembers());
                }
            }
        } catch (final EnforceableDataObjectException e) {
            throw new InvalidDataException(e.getMessage());
        } catch (final PluginException e) {
            throw StorageException.wrapForRMI(e);
        }

        try {
            if (!admin_user.mandatoryCreateMembersSet()) {
                throw new InvalidDataException("Mandatory fields in admin user not set: " + admin_user.getUnsetMembers());
            }
        } catch (final EnforceableDataObjectException e) {
            throw new InvalidDataException(e.getMessage());
        }

        GenericChecks.checkValidMailAddress(admin_user.getPrimaryEmail());
    }

    protected abstract Context createmaincall(final Context ctx, final User admin_user, Database db, UserModuleAccess access, final Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws StorageException, InvalidDataException, ContextExistsException;

    protected SchemaSelectStrategy getDefaultSchemaSelectStrategy() {
        return SchemaSelectStrategy.getDefault();
    }

    protected Context createcommon(final Context ctx, final User admin_user, final Database db, final UserModuleAccess access, final Credentials auth, SchemaSelectStrategy schemaSelectStrategy) throws InvalidCredentialsException, ContextExistsException, InvalidDataException, StorageException {
        try {
            doNullCheck(ctx, admin_user);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Context or user not correct");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        new BasicAuthenticator(context).doAuthentication(auth);

        LOGGER.debug("{} - {}", ctx, admin_user);

        try {
            final OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            Context ret = ctx;
            if (isAnyPluginLoaded()) {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXContextPluginInterface contextInterface : pluginInterfaces.getContextPlugins().getServiceList()) {
                        try {
                            ret = contextInterface.preCreate(ret, admin_user, auth);
                        } catch (PluginException e) {
                            LOGGER.error("", e);
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }

            createchecks(ret, admin_user, tool);

            // Ensure context identifier is contained in login mappings
            {
                final String sContextId = ret.getIdAsString();
                if (null != sContextId) {
                    ret.addLoginMapping(sContextId);
                }
            }

            // Ensure context name is contained in login mappings
            {
                final String name = ret.getName();
                if (null != name) {
                    // Add the name of the context to the login mappings and the id
                    ret.addLoginMapping(name);
                }
            }

            final Context retval = createmaincall(ret, admin_user, db, access, auth, schemaSelectStrategy);
            if (retval.getName() == null) {
                retval.setName(String.valueOf(retval.getId()));
            }
            return retval;
        } catch (final ContextExistsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (StorageException e) {
            LOGGER.error("", e);
            // Eliminate nested root cause exceptions. These are mostly unknown to clients.
            throw new StorageException(e.getMessage());
        }
    }

    /**
     * @return
     * @throws StorageException
     */
    protected boolean isAnyPluginLoaded() throws StorageException {
        final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
        return null != pluginInterfaces && false == pluginInterfaces.getContextPlugins().getServiceList().isEmpty();
    }
}

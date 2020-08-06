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

import static com.openexchange.java.Autoboxing.I;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.plugins.OXResourcePluginInterface;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.AbstractAdminRmiException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.RemoteExceptionUtils;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.PluginInterfaces;
import com.openexchange.admin.storage.interfaces.OXResourceStorageInterface;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.GenericChecks;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.exception.LogLevel;

public class OXResource extends OXCommonImpl implements OXResourceInterface {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OXResource.class);

    // ---------------------------------------------------------------------------------------------------------- //

    private final BasicAuthenticator basicauth;
    private final OXResourceStorageInterface oxRes;
    private final AdminCache cache;
    private final PropertyHandler prop;

    public OXResource() throws RemoteException, StorageException {
        super();
        try {
            oxRes = OXResourceStorageInterface.getInstance();
        } catch (StorageException e) {
            log(LogLevel.ERROR, LOGGER, null, e, null);
            throw new RemoteException(e.getMessage());
        }
        cache = ClientAdminThread.cache;
        prop = cache.getProperties();
        log(LogLevel.INFO, LOGGER, null, null, "Class loaded: {}", this.getClass().getName());
        basicauth = BasicAuthenticator.createPluginAwareAuthenticator();
    }

    private void logAndEnhanceException(Throwable t, final Credentials credentials, final Context ctx, final Resource res) {
        logAndEnhanceException(t, credentials, null != ctx ? ctx.getIdAsString() : null, null != res ? String.valueOf(res.getId()) : null);
    }

    private void logAndEnhanceException(Throwable t, final Credentials credentials, final String contextId, String resourceId) {
        if (t instanceof AbstractAdminRmiException) {
            logAndReturnException(LOGGER, ((AbstractAdminRmiException) t), credentials, contextId, resourceId);
        } else if (t instanceof RemoteException) {
            RemoteException remoteException = (RemoteException) t;
            String exceptionId = AbstractAdminRmiException.generateExceptionId();
            RemoteExceptionUtils.enhanceRemoteException(remoteException, exceptionId);
            logAndReturnException(LOGGER, remoteException, exceptionId, credentials, contextId, resourceId);
        } else if (t instanceof Exception) {
            RemoteException remoteException = RemoteExceptionUtils.convertException((Exception) t);
            String exceptionId = AbstractAdminRmiException.generateExceptionId();
            RemoteExceptionUtils.enhanceRemoteException(remoteException, exceptionId);
            logAndReturnException(LOGGER, remoteException, exceptionId, credentials, contextId, resourceId);
        }
    }

    @Override
    public void change(final Context ctx, final Resource res, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchResourceException {
        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            try {
                doNullCheck(res);
            } catch (InvalidDataException e3) {
                final InvalidDataException invalidDataException = new InvalidDataException("One of the given arguments for change is null");
                log(LogLevel.ERROR, LOGGER, credentials, invalidDataException, "");
                throw invalidDataException;
            }

            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }

            basicauth.doAuthentication(auth, ctx);

            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(res.getId()), null, "{} - {} - {}", ctx, res, auth);

            try {
                setIdOrGetIDFromNameAndIdObject(ctx, res);
            } catch (NoSuchObjectException e) {
                throw new NoSuchResourceException(e);
            }

            res.testMandatoryCreateFieldsNull();

            final int resource_ID = res.getId().intValue();

            checkContextAndSchema(ctx);

            if (!tool.existsResource(ctx, resource_ID)) {
                throw new NoSuchResourceException("Resource with this id does not exists");
            }

            if (res.getName() != null && tool.existsResourceName(ctx, res)) {
                throw new InvalidDataException("Resource " + res.getName() + " already exists in this context");
            }

            if (res.getEmail() != null && tool.existsResourceAddress(ctx, res.getEmail(), res.getId())) {
                throw new InvalidDataException("Resource with this email address already exists");
            }

            tool.primaryMailExists(ctx, res.getEmail());

            if ((null != res.getName()) && prop.getResourceProp(AdminProperties.Resource.AUTO_LOWERCASE, true)) {
                final String rid = res.getName().toLowerCase();
                res.setName(rid);
            }

            if ((null != res.getName()) && prop.getResourceProp(AdminProperties.Resource.CHECK_NOT_ALLOWED_CHARS, true)) {
                validateResourceName(res.getName());
            }

            final String resmail = res.getEmail();
            if (resmail != null && resmail.trim().length() > 0 && !GenericChecks.isValidMailAddress(resmail)) {
                throw new InvalidDataException("Invalid email address");
            }
            oxRes.change(ctx, res);

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXResourcePluginInterface oxresource : pluginInterfaces.getResourcePlugins().getServiceList()) {
                        final String bundlename = oxresource.getClass().getName();
                        try {
                            log(LogLevel.INFO, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(resource_ID), null, "Calling change for plugin: {}", bundlename);
                            oxresource.change(ctx, res, auth);
                        } catch (PluginException e) {
                            log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(resource_ID), e, "Error while calling change for plugin: {}", bundlename);
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx, res);
            throw e;
        }
    }

    @Override
    public Resource create(final Context ctx, final Resource res, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            try {
                doNullCheck(res);
                doNullCheck(res.getName());
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for create is null");
                throw e3;
            }

            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }

            basicauth.doAuthentication(auth, ctx);

            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(res.getId()), null, "{} - {} - {}", ctx, res, auth);

            checkContextAndSchema(ctx);

            if (tool.existsResourceName(ctx, res.getName())) {
                throw new InvalidDataException("Resource " + res.getName() + " already exists in this context");
            }

            if (res.getEmail() != null && tool.existsResourceAddress(ctx, res.getEmail())) {
                throw new InvalidDataException("Resource with this email address already exists");
            }

            tool.primaryMailExists(ctx, res.getEmail());

            if (!res.mandatoryCreateMembersSet()) {
                throw new InvalidDataException("Mandatory fields not set: " + res.getUnsetMembers());
                // TODO: cutmasta look here
            }

            if (prop.getResourceProp(AdminProperties.Resource.AUTO_LOWERCASE, true)) {
                final String uid = res.getName().toLowerCase();
                res.setName(uid);
            }

            if (prop.getResourceProp(AdminProperties.Resource.CHECK_NOT_ALLOWED_CHARS, true)) {
                validateResourceName(res.getName());
            }

            final String resmail = res.getEmail();
            if (resmail != null && !GenericChecks.isValidMailAddress(resmail)) {
                throw new InvalidDataException("Invalid email address");
            }
            final int retval = oxRes.create(ctx, res);
            res.setId(I(retval));

            final ArrayList<OXResourcePluginInterface> interfacelist = new ArrayList<OXResourcePluginInterface>();

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXResourcePluginInterface oxresource : pluginInterfaces.getResourcePlugins().getServiceList()) {
                        final String bundlename = oxresource.getClass().getName();
                        try {
                            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(res.getId()), null, "Calling create for plugin: {}", bundlename);
                            oxresource.create(ctx, res, auth);
                            interfacelist.add(oxresource);
                        } catch (PluginException e) {
                            log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(res.getId()), e, "Error while calling create for plugin: {}", bundlename);
                            log(LogLevel.INFO, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(res.getId()), null, "Now doing rollback for everything until now...");
                            for (final OXResourcePluginInterface oxresourceinterface : interfacelist) {
                                try {
                                    oxresourceinterface.delete(ctx, res, auth);
                                } catch (PluginException e1) {
                                    log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(res.getId()), e1, "Error doing rollback for plugin: {}", bundlename);
                                }
                            }
                            try {
                                oxRes.delete(ctx, res);
                            } catch (StorageException e1) {
                                log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(res.getId()), e1, "Error doing rollback for creating resource in database");
                            }
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }

            return res;
        } catch (EnforceableDataObjectException e) {
            RemoteException remoteException = RemoteExceptionUtils.convertException(e);
            logAndReturnException(LOGGER, remoteException, e.getExceptionId(), credentials, ctx.getIdAsString(), String.valueOf(res.getId()));
            throw remoteException;
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx, res);
            throw e;
        }
    }

    @Override
    public void delete(final Context ctx, final Resource res, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchResourceException {
        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            try {
                doNullCheck(res);
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for delete is null");
                throw e3;
            }

            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }

            basicauth.doAuthentication(auth, ctx);

            try {
                setIdOrGetIDFromNameAndIdObject(ctx, res);
            } catch (NoSuchObjectException e) {
                throw new NoSuchResourceException(e);
            }
            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(res.getId()), null, "{} - {} - {}", ctx, res, auth);
            checkContextAndSchema(ctx);
            if (!tool.existsResource(ctx, res.getId().intValue())) {
                throw new NoSuchResourceException("Resource with this id does not exist");
            }
            final ArrayList<OXResourcePluginInterface> interfacelist = new ArrayList<OXResourcePluginInterface>();

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXResourcePluginInterface oxresource : pluginInterfaces.getResourcePlugins().getServiceList()) {
                        final String bundlename = oxresource.getClass().getName();
                        try {
                            log(LogLevel.INFO, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(res.getId()), null, "Calling delete for plugin: {}", bundlename);
                            oxresource.delete(ctx, res, auth);
                            interfacelist.add(oxresource);
                        } catch (PluginException e) {
                            log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(res.getId()), e, "Error while calling delete for plugin: {}", bundlename);
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }

            oxRes.delete(ctx, res);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx, res);
            throw e;
        }
    }

    @Override
    public Resource getData(final Context ctx, final Resource res, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchResourceException {
        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            try {
                doNullCheck(res);
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for get is null");
                throw e3;
            }

            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }

            basicauth.doAuthentication(auth, ctx);

            try {
                setIdOrGetIDFromNameAndIdObject(ctx, res);
            } catch (NoSuchObjectException e) {
                throw new NoSuchResourceException(e);
            }

            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(res.getId()), null, "{} - {} - {}", ctx, res.getId(), auth);

            checkContextAndSchema(ctx);

            final int resource_id = res.getId().intValue();
            if (!tool.existsResource(ctx, resource_id)) {
                throw new NoSuchResourceException("resource with with this id does not exist");
            }

            Resource retres;
            retres = oxRes.getData(ctx, res);

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXResourcePluginInterface oxresource : pluginInterfaces.getResourcePlugins().getServiceList()) {
                        final String bundlename = oxresource.getClass().getName();
                        log(LogLevel.INFO, LOGGER, credentials, ctx.getIdAsString(), String.valueOf(res.getId()), null, "Calling getData for plugin: {}", bundlename);
                        retres = oxresource.get(ctx, retres, auth);
                    }
                }
            }

            return retres;
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx, res);
            throw e;
        }
    }

    @Override
    public Resource[] getData(final Context ctx, final Resource[] resources, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchResourceException, DatabaseUpdateException {
        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            try {
                doNullCheck((Object[]) resources);
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for getData is null");
                throw e3;
            }

            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }

            basicauth.doAuthentication(auth, ctx);

            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), getObjectIds(resources), null, "{} - {} - {}", ctx, Arrays.toString(resources), auth);

            checkContextAndSchema(ctx);

            // check if all resources exists
            for (final Resource resource : resources) {
                if (resource.getId() != null && !tool.existsResource(ctx, resource.getId().intValue())) {
                    throw new NoSuchResourceException("No such resource " + resource.getId().intValue());
                }
                if (resource.getName() != null && !tool.existsResourceName(ctx, resource.getName())) {
                    throw new NoSuchResourceException("No such resource " + resource.getName());
                }
                if (resource.getName() == null && resource.getId() == null) {
                    throw new InvalidDataException("Resourcename and resourceid missing!Cannot resolve resource data");
                }

                if (resource.getName() == null) {
                    // resolv name by id
                    resource.setName(tool.getResourcenameByResourceID(ctx, resource.getId().intValue()));
                }
                if (resource.getId() == null) {
                    resource.setId(I(tool.getResourceIDByResourcename(ctx, resource.getName())));
                }
            }
            final ArrayList<Resource> retval = new ArrayList<Resource>();
            for (final Resource resource : resources) {
                // not nice, but works ;)
                final Resource tmp = oxRes.getData(ctx, resource);
                retval.add(tmp);
            }

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXResourcePluginInterface oxresource : pluginInterfaces.getResourcePlugins().getServiceList()) {
                        final String bundlename = oxresource.getClass().getName();
                        log(LogLevel.INFO, LOGGER, credentials, ctx.getIdAsString(), getObjectIds(resources), null, "Calling get for plugin: {}", bundlename);
                        for (Resource resource : retval) {
                            resource = oxresource.get(ctx, resource, auth);
                        }
                    }
                }
            }
            return retval.toArray(new Resource[retval.size()]);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx.getIdAsString(), getObjectIds(resources));
            throw e;
        }
    }

    @Override
    public Resource[] list(final Context ctx, final String pattern, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        try {
            Credentials auth = credentials == null ? new Credentials("", "") : credentials;
            try {
                doNullCheck(pattern);
            } catch (InvalidDataException e3) {
                log(LogLevel.ERROR, LOGGER, credentials, e3, "One of the given arguments for list is null");
                throw e3;
            }

            if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                auth.setLogin(auth.getLogin().toLowerCase());
            }

            try {
                if (pattern.length() == 0) {
                    throw new InvalidDataException("Invalid pattern!");
                }

                basicauth.doAuthentication(auth, ctx);
            } catch (InvalidDataException e) {
                log(LogLevel.ERROR, LOGGER, credentials, ctx.getIdAsString(), e, "");
                throw e;
            }

            log(LogLevel.DEBUG, LOGGER, credentials, ctx.getIdAsString(), null, "{} - {} - {}", ctx, pattern, auth);

            checkContextAndSchema(ctx);

            return oxRes.list(ctx, pattern);
        } catch (Throwable e) {
            logAndEnhanceException(e, credentials, ctx, null);
            throw e;
        }
    }

    @Override
    public Resource[] listAll(final Context ctx, final Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        return list(ctx, "*", auth);
    }

    private void validateResourceName(final String resName) throws InvalidDataException {
        if (resName == null || resName.trim().length() == 0) {
            throw new InvalidDataException("Invalid resource name");
        }
        // Check for allowed chars:
        // abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 _-+.%$@
        final String resource_check_regexp = prop.getResourceProp("CHECK_RES_UID_REGEXP", "[ $@%\\.+a-zA-Z0-9_-]");
        final String illegal = resName.replaceAll(resource_check_regexp, "");
        if (illegal.length() > 0) {
            throw new InvalidDataException("Illegal chars: \"" + illegal + "\"");
        }
    }
}

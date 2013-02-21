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

package com.openexchange.contacts.ldap.osgi;

import java.util.Hashtable;
import java.util.Map.Entry;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.contacts.ldap.contacts.LdapContactInterfaceProvider;
import com.openexchange.contacts.ldap.folder.LdapGlobalFolderCreator;
import com.openexchange.contacts.ldap.folder.LdapGlobalFolderCreator.FolderIDAndAdminID;
import com.openexchange.contacts.ldap.folder.LdapUserFolderCreator;
import com.openexchange.contacts.ldap.property.ContextProperties;
import com.openexchange.contacts.ldap.property.FolderProperties;
import com.openexchange.contacts.ldap.property.PropertyHandler;
import com.openexchange.contacts.ldap.storage.DelegatingLdapStorage;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.timer.TimerService;

/**
 * {@link LdapActivator}
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public final class LdapActivator extends HousekeepingActivator {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(LdapActivator.class));

    /**
     * Initializes a new {@link LdapActivator}
     */
    public LdapActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
                ConfigurationService.class, ContextService.class, TimerService.class };
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        /*
         * Never stop the server even if a needed service is absent
         */
        if (LOG.isWarnEnabled()) {
            LOG.warn("Absent service: " + clazz.getName());
        }
        LDAPServiceRegistry.getInstance().removeService(clazz);
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Re-available service: " + clazz.getName());
        }
        LDAPServiceRegistry.getInstance().addService(clazz, getService(clazz));
    }

    @Override
    public void startBundle() throws Exception {
        try {
            /*
             * (Re-)Initialize service registry with available services
             */
            {
                final LDAPServiceRegistry registry = LDAPServiceRegistry.getInstance();
                registry.clearRegistry();
                final Class<?>[] classes = getNeededServices();
                for (int i = 0; i < classes.length; i++) {
                    final Object service = getService(classes[i]);
                    if (null != service) {
                        registry.addService(classes[i], service);
                    }
                }
            }
            if (!started.compareAndSet(false, true)) {
                /*
                 * Don't start the bundle again. A duplicate call to
                 * startBundle() is probably caused by temporary absent
                 * service(s) whose re-availability causes to trigger this
                 * method again.
                 */
                LOG.info("A temporary absent service is available again");
                return;
            }
            registerService(LoginHandlerService.class, new LdapUserFolderCreator(), null);

            final PropertyHandler instance = PropertyHandler.getInstance();
            instance.loadProperties();
            for (final Entry<Integer, ContextProperties> entry : instance.getContextdetails().entrySet()) {
                final Integer ctx = entry.getKey();
                final ContextProperties contextProperties = entry.getValue();
                for (final FolderProperties folderprop : contextProperties.getFolderproperties()) {
                    final FolderIDAndAdminID createGlobalFolder = LdapGlobalFolderCreator.createGlobalFolder(
                        new ContextImpl(ctx.intValue()),
                        folderprop);
                    final int folderid = createGlobalFolder.getFolderid();
                    final String stringfolderid = String.valueOf(folderid);
                    /*
                     * Register LDAP contact storage provider for current context-ID/folder-ID pair
                     */
                    LdapContactInterfaceProvider provider = new LdapContactInterfaceProvider(
                        folderprop, createGlobalFolder.getAdminid(), folderid, ctx.intValue());
                    registerService(ContactStorage.class, new DelegatingLdapStorage(ctx.intValue(), provider, folderprop.getStoragePriority()),
                        getHashtableWithFolderID(stringfolderid, ctx.toString()));
                    LOG.info(new StringBuilder("Registered global LDAP contact storage for folder \"").append(folderprop.getFoldername()).append(
                        "\" with id \"").append(stringfolderid).append("\" for context: ").append(ctx).toString());
                }
            }
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        try {
            cleanUp();
            /*
             * Clear service registry
             */
            LDAPServiceRegistry.getInstance().clearRegistry();
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        } finally {
            started.set(false);
        }
    }

    private Hashtable<String, String> getHashtableWithFolderID(final String folderid, final String contextId) {
        final Hashtable<String, String> hashTable = new Hashtable<String, String>();
        hashTable.put(ContactInterface.OVERRIDE_FOLDER_ATTRIBUTE, folderid);
        hashTable.put(ContactInterface.OVERRIDE_CONTEXT_ATTRIBUTE, contextId);
        return hashTable;
    }

}

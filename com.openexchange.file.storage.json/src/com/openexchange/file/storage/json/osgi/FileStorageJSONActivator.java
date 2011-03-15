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

package com.openexchange.file.storage.json.osgi;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheService;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.json.FileMetadataParser;
import com.openexchange.file.storage.json.actions.accounts.AccountActionFactory;
import com.openexchange.file.storage.json.multiple.AccountMultipleHandler;
import com.openexchange.file.storage.json.multiple.FileMultipleAdapter;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.file.storage.json.servlets.AccountServlet;
import com.openexchange.file.storage.json.servlets.FileServlet;
import com.openexchange.file.storage.parse.FileMetadataParserService;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.i18n.I18nService;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.server.osgiservice.DeferredActivator;

/**
 * {@link FileStorageJSONActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileStorageJSONActivator extends DeferredActivator {

    private static final Log LOG = LogFactory.getLog(FileStorageJSONActivator.class);

    private static final Class<?>[] NEEDED_SERVICES =
        new Class[] { FileStorageServiceRegistry.class, HttpService.class, CacheService.class, IDBasedFileAccessFactory.class, AttachmentBase.class };

    private final List<ServiceTracker> trackers = new LinkedList<ServiceTracker>();

    private final List<ServiceRegistration> registrations = new LinkedList<ServiceRegistration>();

    private HttpService httpService;

    private FileStorageServiceRegistry registry;

    // private MessagingMessageParser parser;

    // private MessagingMessageWriter writer;

    private CacheService cacheService;

    private boolean cacheConfigured;

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        try {
            register();
        } catch (final CacheException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    protected void handleUnavailability(final Class<?> clazz) {
        if (clazz == FileStorageServiceRegistry.class) {
            hide();
        }
    }

    private void hide() {
        if (null != httpService) {
            httpService.unregister("/ajax/filestorage/account");
            httpService.unregister("/ajax/filestorage/message");
            httpService.unregister("/ajax/filestorage/service");
            httpService.unregister("/ajax/infostore");
               
        }

        for (final ServiceRegistration registration : registrations) {
            registration.unregister();
        }
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            Services.LOOKUP = this;
            // parser = new MessagingMessageParser();

            // trackers.add(new ContentParserTracker(context, parser));
            // trackers.add(new HeaderParserTracker(context, parser));

            // writer = new MessagingMessageWriter();
            // trackers.add(new ContentWriterTracker(context, writer));
            // trackers.add(new HeaderWriterTracker(context, writer));
            trackers.add(new ServiceTracker(context, I18nService.class.getName(), new I18nServiceCustomizer(context)));

            for (final ServiceTracker tracker : trackers) {
                tracker.open();
            }

            register();
        } catch (final Exception x) {
            LOG.error(x.getMessage(), x);
            throw x;
        }
    }

    private void register() throws CacheException {

        registry = getService(FileStorageServiceRegistry.class);
        httpService = getService(HttpService.class);
        cacheService = getService(CacheService.class);

        if (!allAvailable()) {
            return;
        }

        AccountActionFactory.INSTANCE = new AccountActionFactory(registry);
        // MessagingActionFactory.INSTANCE = new MessagingActionFactory(registry, writer, parser, getCache());
        // ServicesActionFactory.INSTANCE = new ServicesActionFactory(registry);

        try {
            httpService.registerServlet("/ajax/filestorage/account", new AccountServlet(), null, null);
            httpService.registerServlet("/ajax/infostore", new FileServlet(), null, null);
            // httpService.registerServlet("/ajax/filestorage/message", new MessagesServlet(), null, null);
            // httpService.registerServlet("/ajax/filestorage/service", new ServicesServlet(), null, null);
        } catch (final ServletException e) {
            LOG.error(e.getMessage(), e);
        } catch (final NamespaceException e) {
            LOG.error(e.getMessage(), e);
        }

        registrations.add(context.registerService(MultipleHandlerFactoryService.class.getName(), new AccountMultipleHandler(), null));
        registrations.add(context.registerService(MultipleHandlerFactoryService.class.getName(), new FileMultipleAdapter(), null));
        // registrations.add(context.registerService(MultipleHandlerFactoryService.class.getName(), new MessagesMultipleHandler(), null));
        // registrations.add(context.registerService(MultipleHandlerFactoryService.class.getName(), new ServicesMultipleHandler(), null));
        registrations.add(context.registerService(FileMetadataParserService.class.getName(), FileMetadataParser.getInstance(), null));
    }

    private Cache getCache() throws CacheException {
        final String regionName = "com.openexchange.file.storage.json.fileCache";
        if (!cacheConfigured) {
            cacheConfigured = true;
            final byte[] ccf = ("jcs.region." + regionName + "=LTCP\n"
                + "jcs.region." + regionName + ".cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes\n"
                + "jcs.region." + regionName + ".cacheattributes.MaxObjects=10000000\n"
                + "jcs.region." + regionName + ".cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache\n"
                + "jcs.region." + regionName + ".cacheattributes.UseMemoryShrinker=true\n"
                + "jcs.region." + regionName + ".cacheattributes.MaxMemoryIdleTimeSeconds=180\n"
                + "jcs.region." + regionName + ".cacheattributes.ShrinkerIntervalSeconds=60\n"
                + "jcs.region." + regionName + ".elementattributes=org.apache.jcs.engine.ElementAttributes\n"
                + "jcs.region." + regionName + ".elementattributes.IsEternal=false\n"
                + "jcs.region." + regionName + ".elementattributes.MaxLifeSeconds=300\n"
                + "jcs.region." + regionName + ".elementattributes.IdleTime=180\n"
                + "jcs.region." + regionName + ".elementattributes.IsSpool=false\n"
                + "jcs.region." + regionName + ".elementattributes.IsRemote=false\n"
                + "jcs.region." + regionName + ".elementattributes.IsLateral=false\n").getBytes();
            cacheService.loadConfiguration(new ByteArrayInputStream(ccf));
        }
        return cacheService.getCache(regionName);
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            for (final ServiceTracker tracker : trackers) {
                tracker.close();
            }
            trackers.clear();
            for (final ServiceRegistration registration : registrations) {
                registration.unregister();
            }
            registrations.clear();
        } catch (final Exception x) {
            LOG.error(x.getMessage(), x);
            throw x;
        }
    }

}

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

package com.openexchange.apps.manifests.json.osgi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.json.JSONArray;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.apps.manifests.json.ManifestActionFactory;
import com.openexchange.apps.manifests.json.ManifestBuilder;
import com.openexchange.apps.manifests.json.values.UIVersion;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.userconfiguration.osgi.PermissionRelevantServiceAddedTracker;
import com.openexchange.java.Streams;
import com.openexchange.passwordchange.PasswordChangeService;
import com.openexchange.serverconfig.ComputedServerConfigValueService;
import com.openexchange.serverconfig.ServerConfigService;

/**
 * {@link ManifestJSONActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class ManifestJSONActivator extends AJAXModuleActivator implements ForcedReloadable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ManifestJSONActivator.class);

    private volatile ManifestBuilder manifestBuilder;

    /**
     * Initializes a new {@link ManifestJSONActivator}.
     */
    public ManifestJSONActivator() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, ServerConfigService.class, SimpleConverter.class };
    }

    @Override
    protected void stopBundle() throws Exception {
        this.manifestBuilder = null;
        UIVersion.UIVERSION.set("");
        super.stopBundle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startBundle() throws Exception {
        // Add tracker to identify if a PasswordChangeService was registered. If so, add to PermissionAvailabilityService
        rememberTracker(new PermissionRelevantServiceAddedTracker<PasswordChangeService>(context, PasswordChangeService.class));

        // Read manifests from files
        JSONArray initialManifests = readManifests(getService(ConfigurationService.class));

        // And track ManifestContributors
        ManifestContributorTracker manifestContributors = new ManifestContributorTracker(context);
        rememberTracker(manifestContributors);
        trackService(HostnameService.class);
        openTrackers();

        // Enhance computed server configuration by adding UIVersion
        UIVersion.UIVERSION.set(context.getBundle().getVersion().toString());
        registerService(ComputedServerConfigValueService.class, new UIVersion());

        // Register as Reloadable
        registerService(Reloadable.class, this);

        ManifestBuilder manifestBuilder = new ManifestBuilder(initialManifests, manifestContributors);
        this.manifestBuilder = manifestBuilder;
        manifestContributors.setManifestBuilder(manifestBuilder);
        registerModule(new ManifestActionFactory(this, manifestBuilder), "apps/manifests");
    }

    private JSONArray readManifests(ConfigurationService configService) {
        String[] paths;
        {
            String property = configService.getProperty("com.openexchange.apps.manifestPath");
            if (null == property) {
                property = configService.getProperty("com.openexchange.apps.path");
                if (null == property) {
                    return new JSONArray(0);
                }
                paths = property.split(":");
                for (int i = 0; i < paths.length; i++) {
                    paths[i] += "/manifests";
                }
            } else {
                paths = property.split(":");
            }
        }

        JSONArray manifests = new JSONArray(paths.length << 1);
        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                for (File f : file.listFiles()) {
                    read(f, manifests);
                }
            }
        }

        return manifests;
    }

    private void read(File f, JSONArray manifests) {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(f));
            JSONArray fileManifests = new JSONArray(r);
            for (int i = 0, size = fileManifests.length(); i < size; i++) {
                manifests.put(fileManifests.get(i));
            }
        } catch (Exception e) {
            LOG.error("", e);
        } finally {
            Streams.close(r);
        }
    }

    // --------------------------------------------------------------------------------------------------

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        ManifestBuilder manifestBuilder = this.manifestBuilder;
        if (null != manifestBuilder) {
            // Read manifests from files
            JSONArray initialManifests = readManifests(configService);

            // Reinitialize manifests builder
            manifestBuilder.reinitialize(initialManifests);
        }
    }

    @Override
    public Interests getInterests() {
        return Reloadables.getInterestsForAll();
    }

}

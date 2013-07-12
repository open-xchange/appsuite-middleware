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

package com.openexchange.apps.manifests.json.osgi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.osgi.framework.BundleContext;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.apps.manifests.ComputedServerConfigValueService;
import com.openexchange.apps.manifests.ServerConfigMatcherService;
import com.openexchange.apps.manifests.json.ManifestActionFactory;
import com.openexchange.apps.manifests.json.values.UIVersion;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityFilter;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.groupware.userconfiguration.AvailabilityChecker;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.TrackerAvailabilityChecker;
import com.openexchange.java.Streams;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.NearRegistryServiceTracker;
import com.openexchange.passwordchange.PasswordChangeService;

/**
 * {@link ManifestJSONActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ManifestJSONActivator extends AJAXModuleActivator {

    private static final Log LOG = LogFactory.getLog(ManifestJSONActivator.class);

    private volatile AvailabilityChecker editPasswordChecker;

    /**
     * Initializes a new {@link ManifestJSONActivator}.
     */
    public ManifestJSONActivator() {
        super();
    }

	@Override
	protected Class<?>[] getNeededServices() {
		return new Class<?>[]{ConfigurationService.class, CapabilityService.class, SimpleConverter.class};
	}

	@Override
	protected void startBundle() throws Exception {
	    final BundleContext context = this.context;

        UIVersion.UIVERSION = context.getBundle().getVersion().toString();

        final AvailabilityChecker editPasswordChecker = TrackerAvailabilityChecker.getAvailabilityCheckerFor(PasswordChangeService.class, true, context);
        this.editPasswordChecker = editPasswordChecker;
        final String editPasswordName = Permission.EDIT_PASSWORD.name().toLowerCase();
        final CapabilityFilter capabilityFilter = new CapabilityFilter() {

            @Override
            public boolean accept(final Capability capability) {
                return (editPasswordChecker.isAvailable() || !editPasswordName.equals(capability.getId()));
            }
        };

	    final NearRegistryServiceTracker<ServerConfigMatcherService> matcherTracker = new NearRegistryServiceTracker<ServerConfigMatcherService>(context, ServerConfigMatcherService.class);
	    rememberTracker(matcherTracker);
	    final NearRegistryServiceTracker<ComputedServerConfigValueService> computedValueTracker = new NearRegistryServiceTracker<ComputedServerConfigValueService>(context, ComputedServerConfigValueService.class);
	    rememberTracker(computedValueTracker);

		registerModule(new ManifestActionFactory(this, readManifests(), new ServerConfigServicesLookup() {

			@Override
			public List<ServerConfigMatcherService> getMatchers() {
				return Collections.unmodifiableList(matcherTracker.getServiceList());
			}

			@Override
			public List<ComputedServerConfigValueService> getComputed() {
				return Collections.unmodifiableList(computedValueTracker.getServiceList());
			}
		}, capabilityFilter), "apps/manifests");

		openTrackers();
	}

	@Override
	protected void stopBundle() throws Exception {
	    final AvailabilityChecker editPasswordChecker = this.editPasswordChecker;
	    if (null != editPasswordChecker) {
            editPasswordChecker.close();
            this.editPasswordChecker = null;
        }
	    super.stopBundle();
	}

    private JSONArray readManifests() {
        String[] paths;
        {
            final ConfigurationService conf = getService(ConfigurationService.class);
            String property = conf.getProperty("com.openexchange.apps.manifestPath");
            if (null == property) {
                property = conf.getProperty("com.openexchange.apps.path");
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

        final JSONArray array = new JSONArray(paths.length << 1);
        for (final String path : paths) {
            final File file = new File(path);
            if (file.exists()) {
                for (final File f : file.listFiles()) {
                    read(f, array);
                }
            }
        }

        return array;
    }

    private void read(File f, JSONArray array) {
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(f));
            final JSONArray fileContent = new JSONArray(r);
            final int length = fileContent.length();
            for (int i = 0, size = length; i < size; i++) {
                array.put(fileContent.get(i));
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            Streams.close(r);
        }
    }

}

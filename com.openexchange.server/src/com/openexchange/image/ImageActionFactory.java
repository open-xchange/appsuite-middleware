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

package com.openexchange.image;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.conversion.ConversionService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthModule;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link ImageActionFactory}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
@OAuthModule
public class ImageActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AJAXActionService> actions;

    public static final ConcurrentMap<String, String> regName2Alias = new ConcurrentHashMap<String, String>(8, 0.9f, 1);

    public static final ConcurrentMap<String, String> alias2regName = new ConcurrentHashMap<String, String>(8, 0.9f, 1);

    /**
     * The image servlet's alias
     */
    public static final String ALIAS_APPENDIX = ImageDataSource.ALIAS_APPENDIX;

    /**
     * Adds specified mapping
     *
     * @param registrationName The registration name
     * @param alias The alias
     */
    public static void addMapping(final String registrationName, final String alias) {
        regName2Alias.put(registrationName, alias);
        alias2regName.put(alias, registrationName);
    }

    /**
     * Gets the image data source by given registration name
     *
     * @param registrationName The registration name to look-up by
     * @return The image data source or <code>null</code>
     */
    public static ImageDataSource getImageDataSourceByRegistrationName(String registrationName) {
        if (Strings.isEmpty(registrationName)) {
            return null;
        }

        ConversionService conversionService = ServerServiceRegistry.getInstance().getService(ConversionService.class);
        if (null == conversionService) {
            return null;
        }
        return (ImageDataSource) conversionService.getDataSource(registrationName);
    }

    /**
     * Gets the registration name for given URL.
     *
     * @param url The URL
     * @return The associated registration name or <code>null</code>
     */
    public static String getRegistrationNameFor(final String url) {
        if (null == url) {
            return null;
        }
        String dispatcherPrefix = ImageUtility.getDispatcherPrefix();
        return getRegistrationNameFor(url, dispatcherPrefix);
    }

    private static String getRegistrationNameFor(final String url, final String prefix) {
        String s = url;
        {
            String path = prefix + ALIAS_APPENDIX;
            int pos = s.indexOf(path);
            if (pos >= 0) {
                s = s.substring(pos + path.length());
            } else {
                path = "/appsuite/api/" + ALIAS_APPENDIX;
                pos = s.indexOf(path);
                if (pos >= 0) {
                    s = s.substring(pos + path.length());
                }
            }
        }
        for (final Entry<String, String> entry : alias2regName.entrySet()) {
            final String alias = entry.getKey();
            if (s.startsWith(alias)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Initializes a new {@link ImageActionFactory}.
     */
    public ImageActionFactory(final ServiceLookup services) {
        super();
        actions = new ConcurrentHashMap<String, AJAXActionService>();
        actions.put("GET", new ImageGetAction(services));
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }
}

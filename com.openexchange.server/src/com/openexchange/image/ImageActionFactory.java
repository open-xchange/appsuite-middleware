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

package com.openexchange.image;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ImageActionFactory}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
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
            final String path = prefix + ALIAS_APPENDIX;
            final int pos = s.indexOf(path);
            if (pos >= 0) {
                s = s.substring(pos + path.length());
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

    @Override
    public Collection<? extends AJAXActionService> getSupportedServices() {
        return java.util.Collections.unmodifiableCollection(actions.values());
    }

}

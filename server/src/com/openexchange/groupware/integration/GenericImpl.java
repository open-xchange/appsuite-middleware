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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.integration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.openexchange.configuration.ConfigurationException;
import com.openexchange.groupware.configuration.GenericImplConfig;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;

/**
 * Generic implementation of a config jump. Replaces some tags in the URL of
 * the configjump.properties and sends the link to the GUI.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class GenericImpl extends SetupLink {

    /**
     * Default constructor.
     */
    public GenericImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getLink(final Object... values) throws SetupLinkException {
        final String userLogin = getUserLogin(values);
        final String password = getPassword(values);
        String url = GenericImplConfig.getProperty(GenericImplConfig.Property
            .URL);
        url = url.replace("%u", userLogin);
        url = url.replace("%p", password);
        try {
            final Context ctx = ContextStorage.getInstance().getContext(
                getContextId(values));
            final String[] loginInfos = ctx.getLoginInfo();
            url = url.replace("%c", extract(loginInfos, ctx.getContextId()));
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new SetupLinkException(SetupLinkException.Code.MALFORMED_URL,
                e, url);
        } catch (ContextException e) {
            throw new SetupLinkException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() throws SetupLinkException {
        try {
            GenericImplConfig.init();
        } catch (ConfigurationException e) {
            throw new SetupLinkException(e);
        }
    }

    private String extract(final String[] loginInfos, final int contextId) {
        final Set<String> infos = new HashSet<String>();
        infos.addAll(Arrays.asList(loginInfos));
        if (infos.size() > 1) {
            infos.remove(String.valueOf(contextId));
        }
        return infos.toArray(new String[infos.size()])[0];
    }
}

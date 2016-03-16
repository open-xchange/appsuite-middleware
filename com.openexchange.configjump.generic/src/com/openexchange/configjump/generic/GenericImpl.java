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

package com.openexchange.configjump.generic;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.configjump.ConfigJumpExceptionCode;
import com.openexchange.configjump.ConfigJumpService;
import com.openexchange.configjump.Replacements;
import com.openexchange.exception.OXException;

/**
 * Generic implementation of a config jump. Replaces some tags in the URL of
 * the configjump.properties and sends the link to the GUI.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class GenericImpl implements ConfigJumpService {

    private final String url;
    /**
     * Default constructor.
     */
    public GenericImpl(final String url) {
        super();
        this.url = url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URL getLink(final Replacements values) throws OXException {
        final String username = values.getUsername();
        final String password = values.getPassword();
        String replacedUrl = url.replace("%u", username);
        replacedUrl = replacedUrl.replace("%p", password);
        try {
            final int contextId = values.getContextId();
            final String[] loginInfos = values.getContextInfos();
            replacedUrl = replacedUrl.replace("%c", extract(loginInfos, contextId));
            return new URL(replacedUrl);
        } catch (final MalformedURLException e) {
            throw ConfigJumpExceptionCode.MALFORMED_URL.create(e, replacedUrl);
        }
    }

    private String extract(final String[] loginInfos, final int contextId) {
        final Set<String> infos = new HashSet<String>();
        infos.addAll(Arrays.asList(loginInfos));
        if (infos.size() > 1) {
            infos.remove(Integer.toString(contextId));
        }
        return infos.toArray(new String[infos.size()])[0];
    }
}

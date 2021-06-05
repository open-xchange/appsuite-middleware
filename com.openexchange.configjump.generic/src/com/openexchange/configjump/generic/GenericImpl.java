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
        } catch (MalformedURLException e) {
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

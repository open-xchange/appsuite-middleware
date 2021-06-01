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

package com.openexchange.mail.autoconfig.sources.staticsource;

import com.openexchange.exception.OXException;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.DefaultAutoconfig;
import com.openexchange.mail.autoconfig.sources.ConfigSource;

/**
 * {@link KnownStaticConfigSource} - An enumeration of known static sources.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public enum KnownStaticConfigSource implements ConfigSource {

    /**
     * The static config source for <code>gmail.com</code>.
     */
    GMAIL(new GMailConfigSource()),
    /**
     * The static config source for <code>outlook.com</code>.
     */
    OUTLOOK_COM(new OutlookComConfigSource()),
    /**
     * The static config source for <code>gmx.com</code> and <code>gmx.de</code>.
     */
    GMX(new GmxConfigSource()),
    /**
     * The static config source for <code>zoho.com</code>.
     */
    ZOHO(new ZohoConfigSource()),
    /**
     * The static config source for <code>icloud.com</code>.
     */
    ICLOUD(new ICloudConfigSource()),
    /**
     * The static config source for <code>aol.com</code>.
     */
    AOL(new AolConfigSource()),

    ;

    private final AbstractStaticConfigSource configSource;

    private KnownStaticConfigSource(AbstractStaticConfigSource configSource) {
        this.configSource = configSource;
    }

    // ---------------------------------------------- Delegate methods ---------------------------------------------------------------------

    @Override
    public Autoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, int userId, int contextId) throws OXException {
        return configSource.getAutoconfig(emailLocalPart, emailDomain, password, userId, contextId);
    }

    @Override
    public DefaultAutoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, int userId, int contextId, boolean forceSecure) throws OXException {
        return configSource.getAutoconfig(emailLocalPart, emailDomain, password, userId, contextId, forceSecure);
    }

}

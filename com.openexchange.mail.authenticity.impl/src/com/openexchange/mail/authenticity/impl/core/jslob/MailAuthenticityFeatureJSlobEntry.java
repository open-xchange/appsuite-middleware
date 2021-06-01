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

package com.openexchange.mail.authenticity.impl.core.jslob;

import static com.openexchange.java.Autoboxing.B;
import java.util.Map;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlobEntry;
import com.openexchange.jslob.JSlobKeys;
import com.openexchange.mail.authenticity.MailAuthenticityProperty;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link MailAuthenticityFeatureJSlobEntry}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailAuthenticityFeatureJSlobEntry implements JSlobEntry {

    private static final String NAME = "features/authenticity";

    private final ServiceLookup services;

    /**
     * Initialises a new {@link MailAuthenticityFeatureJSlobEntry}.
     */
    public MailAuthenticityFeatureJSlobEntry(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getKey() {
        return JSlobKeys.MAIL;
    }

    @Override
    public String getPath() {
        return NAME;
    }

    @Override
    public boolean isWritable(Session session) throws OXException {
        return false;
    }

    @Override
    public Object getValue(Session session) throws OXException {
        LeanConfigurationService configService = services.getService(LeanConfigurationService.class);
        return B(configService.getBooleanProperty(session.getUserId(), session.getContextId(), MailAuthenticityProperty.ENABLED));
    }


    @Override
    public void setValue(Object value, Session session) throws OXException {
        // not writable
    }

    @Override
    public Map<String, Object> metadata(Session session) throws OXException {
        // nope
        return null;
    }
}

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

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlobEntry;
import com.openexchange.jslob.JSlobKeys;
import com.openexchange.session.Session;

/**
 * {@link MailAuthenticityLevelJSlobEntry}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class MailAuthenticityLevelJSlobEntry implements JSlobEntry {

    private static final String NAME = "authenticity/level";
    private static final String VALUE = "fail_suspicious_pass_trusted";

    /**
     * Initializes a new {@link MailAuthenticityLevelJSlobEntry}.
     */
    public MailAuthenticityLevelJSlobEntry() {
        super();
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
        // Nope
        return false;
    }

    @Override
    public Object getValue(Session sessiond) throws OXException {
        return VALUE;
    }

    @Override
    public void setValue(Object value, Session sessiond) throws OXException {
        // Read-only
    }

    @Override
    public Map<String, Object> metadata(Session session) throws OXException {
        // Nope
        return null;
    }
}

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

package com.openexchange.chronos.provider.internal.config;

import static com.openexchange.osgi.Tools.requireService;
import org.json.JSONObject;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * 
 * {@link AllowChangeOfOrganizer}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.2
 */
public class AllowChangeOfOrganizer extends ReadOnlyChronosJSlobEntry {

    private static final String CHANGE_ORGANIZER_PROPERTY = "com.openexchange.calendar.allowChangeOfOrganizer";

    /**
     * Initializes a new {@link AllowChangeOfOrganizer}.
     *
     * @param services A service lookup reference
     */
    public AllowChangeOfOrganizer(ServiceLookup services) {
        super(services);
    }

    @Override
    public String getPath() {
        return "chronos/allowChangeOfOrganizer";
    }

    @Override
    protected Object getValue(ServerSession session, JSONObject userConfig) throws OXException {
        ConfigViewFactory configViewFactory = requireService(ConfigViewFactory.class, services);
        ConfigView view = configViewFactory.getView(session.getUserId(), session.getContextId());
        if (null != view) {
            Boolean changeOrganizer = view.get(CHANGE_ORGANIZER_PROPERTY, Boolean.class);
            return null == changeOrganizer ? Boolean.FALSE : changeOrganizer;
        }
        return Boolean.FALSE;
    }

}

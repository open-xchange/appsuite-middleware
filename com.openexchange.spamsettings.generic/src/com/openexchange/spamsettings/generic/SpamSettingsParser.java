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


package com.openexchange.spamsettings.generic;

import java.util.Map;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.json.FormContentParser;
import com.openexchange.exception.OXException;
import com.openexchange.spamsettings.generic.osgi.SpamSettingsServiceRegistry;
import com.openexchange.spamsettings.generic.service.SpamSettingService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SpamSettingsParser}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class SpamSettingsParser {

    public Map<String, Object> parse(final ServerSession session, final JSONObject json) throws OXException {
        // final FormContentParser formContentParser = new FormContentParser();
        final SpamSettingService service = SpamSettingsServiceRegistry.getServiceRegistry().getService(SpamSettingService.class);
        final DynamicFormDescription formDescription = service.getFormDescription(session);
        return FormContentParser.parse(json, formDescription);
    }
}

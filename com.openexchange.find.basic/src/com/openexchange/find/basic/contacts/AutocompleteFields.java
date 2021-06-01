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

package com.openexchange.find.basic.contacts;

import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.find.basic.Services;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link AutocompleteFields}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class AutocompleteFields implements PreferencesItemService {

    private static final String NAME = "autocompleteFields";
    private static final String AUTOCOMPLETE_CONFIGURATION = "com.openexchange.contact.autocomplete.fields";

    /**
     * Default constructor.
     */
    public AutocompleteFields() {
        super();
    }

    @Override
    public String[] getPath() {
        return new String[] { NAME };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {

            @Override
            public boolean isAvailable(UserConfiguration userConfig) {
                return true;
            }

            @Override
            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
                ConfigurationService confServ = Services.getConfigurationService();
                List<String> fields = confServ.getProperty(AUTOCOMPLETE_CONFIGURATION, "GIVEN_NAME, SUR_NAME, DISPLAY_NAME, EMAIL1, EMAIL2, EMAIL3", ",");
                StringBuilder result = new StringBuilder();
                boolean first = true;
                for (String field : fields) {
                    if (first) {
                        result.append(ContactMapper.getInstance().get(ContactField.valueOf(field)).getAjaxName());
                        first = false;
                    } else {
                        result.append(",").append(ContactMapper.getInstance().get(ContactField.valueOf(field)).getAjaxName());
                    }
                }
                setting.setSingleValue(result.toString());
            }
        };
    }

}

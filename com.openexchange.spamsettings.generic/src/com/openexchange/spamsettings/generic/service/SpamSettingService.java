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


package com.openexchange.spamsettings.generic.service;

import java.util.Map;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SpamSettingService}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public interface SpamSettingService {

    /**
     * Provides the Form Description to be displayed in the Configuration part of the User Interface.
     *
     * @param session a Session
     * @return The Form Description
     */
    public DynamicFormDescription getFormDescription(ServerSession session) throws OXException;

    /**
     * The current settings of the Spam Configuration.
     *
     * @param session a Session
     * @return The setting pairs
     */
    public Map<String, Object> getSettings(ServerSession session) throws OXException;

    /**
     * Writes the settings of the Spam Configuration.
     *
     * @param session a Session
     * @param settings The setting pairs to be written
     */
    public void writeSettings(ServerSession session, Map<String, Object> settings) throws OXException;

}

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

package com.openexchange.authentication.application.impl.api;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.authentication.application.ApplicationPassword;
import com.openexchange.authentication.application.storage.history.AppPasswordLogin;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ListApplicationPassword}
 * Get list of existing application specific passwords
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class ListApplicationPassword extends AbstractAppPasswordAction {

    /**
     * Initializes a new {@link ListApplicationPassword}.
     * 
     * @param lookup The service lookup
     */
    public ListApplicationPassword(ServiceLookup lookup) {
        super(lookup);
    }

    @Override
    public AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException {
        List<ApplicationPassword> applicationPasswords = getService().getList(session);
        if (null == applicationPasswords || applicationPasswords.isEmpty()) {
            return new AJAXRequestResult(Collections.<Entry<ApplicationPassword, AppPasswordLogin>> emptyList(), ApplicationPasswordResultConverter.INPUT_FORMAT);
        }
        Map<String, AppPasswordLogin> lastLogins = getService().getLastLogins(session);
        List<Entry<ApplicationPassword, AppPasswordLogin>> passwordInfos = new ArrayList<Map.Entry<ApplicationPassword, AppPasswordLogin>>(applicationPasswords.size());
        for (ApplicationPassword applicationPassword : applicationPasswords) {
            AppPasswordLogin passwordLogin = lastLogins.get(applicationPassword.getGUID());
            passwordInfos.add(new AbstractMap.SimpleEntry<ApplicationPassword, AppPasswordLogin>(applicationPassword, passwordLogin));
        }
        return new AJAXRequestResult(passwordInfos, ApplicationPasswordResultConverter.INPUT_FORMAT);
    }

}

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

package com.openexchange.messaging.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingService;


/**
 * {@link SimMessagingServiceRegistry}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Firstname Lastname</a>
 */
public class SimMessagingServiceRegistry implements MessagingServiceRegistry{
    private final Map<String, MessagingService> services = new HashMap<String, MessagingService>();

    private OXException exception;

    @Override
    public List<MessagingService> getAllServices(int user, int context) throws OXException {
        exception();
        return new ArrayList<MessagingService>(services.values());
    }

    @Override
    public MessagingService getMessagingService(final String id, int user, int context) throws OXException {
        exception();
        return services.get(id);
    }

    private void exception() throws OXException {
        if (exception != null) {
            throw exception;
        }
    }

    public void add(final MessagingService service) {
        services.put(service.getId(), service);
    }

    public void setException(final OXException exception) {
        this.exception = exception;
    }

    @Override
    public boolean containsMessagingService(final String id, int user, int context) {
        return services.containsKey(id);
    }
}

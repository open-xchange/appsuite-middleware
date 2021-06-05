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

package com.openexchange.importexport.importers;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.importexport.Importer;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * This class contains basic helper methods needed by all importers.
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public abstract class AbstractImporter implements Importer {

    private static final String CONTACT_LIMIT = "com.openexchange.import.contacts.limit".intern();

    protected ServiceLookup services;

    protected AbstractImporter(ServiceLookup services) {
        super();
        this.services = services;
    }

    protected int getLimit(Session session) throws OXException {
        if (services == null) {
            return 0;
        }
        return services.getService(ConfigViewFactory.class).getView(session.getUserId(), session.getContextId()).opt(CONTACT_LIMIT, int.class, I(0)).intValue();
    }
}

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

package com.openexchange.microsoft.graph.contacts.impl;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.microsoft.graph.api.MicrosoftGraphContactsAPI;
import com.openexchange.microsoft.graph.contacts.MicrosoftGraphContactsService;
import com.openexchange.microsoft.graph.contacts.parser.ContactParser;

/**
 * {@link MicrosoftGraphContactsServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class MicrosoftGraphContactsServiceImpl implements MicrosoftGraphContactsService {

    private final MicrosoftGraphContactsAPI api;

    /**
     * Initialises a new {@link MicrosoftGraphContactsServiceImpl}.
     */
    public MicrosoftGraphContactsServiceImpl(MicrosoftGraphContactsAPI api) {
        super();
        this.api = api;
    }

    @Override
    public List<Contact> getContacts(String accessToken) throws OXException {
        return new ContactParser(api, accessToken).parseFeed(api.getContacts(accessToken));
    }

    @Override
    public List<Contact> getContacts(String accessToken, int startOffset, int skip) throws OXException {
        return new ContactParser(api, accessToken).parseFeed(api.getContacts(accessToken, startOffset, skip));
    }
}

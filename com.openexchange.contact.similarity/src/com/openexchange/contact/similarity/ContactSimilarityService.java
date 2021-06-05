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

package com.openexchange.contact.similarity;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.session.Session;

/**
 * {@link ContactSimilarityService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public interface ContactSimilarityService {

    /**
     * Tests if there is any contact which is similar to the given contact and retrieves it.
     * A contact is similar if the similarity score is equal or greater than <code>maxSimilarity</code>.
     * 
     * @param session The user session
     * @param contact The contact
     * @param maxSimilarity The max similarity score. Values: [0,1]
     * @return a similar contact or null otherwise
     * @throws OXException
     */
    public Contact getSimilar(Session session, Contact contact, float maxSimilarity) throws OXException;

}

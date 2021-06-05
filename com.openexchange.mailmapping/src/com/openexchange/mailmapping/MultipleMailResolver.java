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

package com.openexchange.mailmapping;

import com.openexchange.exception.OXException;

/**
 * A {@link MultipleMailResolver} knows how to resolve mail addresses to context and user identifiers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MultipleMailResolver extends MailResolver {

    /**
     * Resolves specified E-Mail addresses.
     *
     * @param mails The E-Mail addresses to resolve
     * @return The resolved E-Mail addresses; contains <code>null</code> elements for those which could not be resolved
     * @throws OXException If resolve operation fails
     */
    ResolvedMail[] resolveMultiple(String... mails) throws OXException;

}

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

package com.openexchange.messaging.generic.internal;

import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;


/**
 * {@link Modifier} - Modifies incoming/outgoing {@link MessagingAccount} instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Modifier {

    /**
     * Modifies account intended for incoming actions.
     *
     * @param account The account
     * @return The modified account
     * @throws OXException If modifying fails
     */
    public MessagingAccount modifyIncoming(MessagingAccount account) throws OXException;

    /**
     * Modifies account intended for outgoing actions.
     *
     * @param account The account
     * @return The modified account
     * @throws OXException If modifying fails
     */
    public MessagingAccount modifyOutgoing(MessagingAccount account) throws OXException;

}

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

package com.openexchange.share.core;

import java.util.Collections;
import java.util.Map;
import com.openexchange.share.CreatedShares;
import com.openexchange.share.ShareInfo;
import com.openexchange.share.recipient.ShareRecipient;


/**
 * Result object for created shares.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class CreatedSharesImpl implements CreatedShares {

    private final Map<ShareRecipient, ShareInfo> shares;

    public CreatedSharesImpl(Map<ShareRecipient, ShareInfo> createdShares) {
        super();
        this.shares = createdShares;
    }

    /**
     * Gets an iterable of all recipients for who one or more shares have been created.
     *
     * @return An immutable iterable
     */
    @Override
    public Iterable<ShareRecipient> getRecipients() {
        return Collections.unmodifiableSet(shares.keySet());
    }

    /**
     * Gets the created share for the passed recipient. If the recipient is not part of
     * {@link #getRecipients()}, <code>null</code> is returned.
     *
     * @param recipient The recipient
     * @return The share
     */
    @Override
    public ShareInfo getShare(ShareRecipient recipient) {
        return shares.get(recipient);
    }

    /**
     * Gets the number of different recipients for who shares have been created.
     *
     * @return The number of recipients
     */
    @Override
    public int size() {
        return shares.size();
    }

}

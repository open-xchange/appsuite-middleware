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

package com.openexchange.mail.compose.mailstorage.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * {@link MissingDraftException} - Thrown when draft mails are missing.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class MissingDraftException extends Exception {

    private static final long serialVersionUID = 1719728029758878520L;

    private final List<MailStorageId> mailStorageIds;

    /**
     * Initializes a new {@link MissingDraftException}.
     *
     * @param mailStorageIds The identifiers of draft mails that are missing
     */
    public MissingDraftException(MailStorageId... mailStorageIds) {
        this(Arrays.asList(mailStorageIds));
    }

    /**
     * Initializes a new {@link MissingDraftException}.
     *
     * @param mailStorageIds The identifiers of the draft mails that are missing
     */
    public MissingDraftException(Collection<? extends MailStorageId> mailStorageIds) {
        super();
        this.mailStorageIds = new ArrayList<>(mailStorageIds);
    }

    /**
     * Gets the identifiers of draft mails that are missing.
     *
     * @return The identifiers of draft mails that are missing
     */
    public List<MailStorageId> getMailStorageIds() {
        return mailStorageIds;
    }

    /**
     * Gets the first identifier from the {@link #getMailStorageIds() listing} of all identifiers of draft mails that are missing.
     *
     * @return The first identifier
     */
    public MailStorageId getFirstMailStorageId() {
        return mailStorageIds.get(0);
    }

}

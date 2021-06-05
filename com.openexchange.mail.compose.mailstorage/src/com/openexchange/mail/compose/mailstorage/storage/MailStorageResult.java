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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;

/**
 * {@link MailStorageResult} - A result from a call to {@link IMailStorage mail storage}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class MailStorageResult<R> {

    /**
     * Gets the appropriate instance for given result object w/o warnings.
     *
     * @param <R> The type of the result
     * @param mailStorageId The recent mail storage ID of the respective draft
     * @param result The result object
     * @param validated Whether draft message has been validated
     * @return The appropriate instance
     */
    public static <R> MailStorageResult<R> resultFor(MailStorageId mailStorageId, R result, boolean validated) {
        return new MailStorageResult<R>(mailStorageId, result, validated, null);
    }

    /**
     * Gets the appropriate instance for given result object taking over warnings from specified mail access.
     *
     * @param <R> The type of the result
     * @param mailStorageId The recent mail storage ID of the respective draft
     * @param result The result object
     * @param validated Whether draft message has been validated
     * @param mailAccess The mail access in use
     * @return The appropriate instance
     */
    public static <R> MailStorageResult<R> resultFor(MailStorageId mailStorageId, R result, boolean validated, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) {
        return resultFor(mailStorageId, result, validated, mailAccess, null);
    }

    /**
     * Gets the appropriate instance for given result object taking over warnings from specified mail access.
     *
     * @param <R> The type of the result
     * @param mailStorageId The recent mail storage ID of the respective draft
     * @param result The result object
     * @param validated Whether draft message has been validated
     * @param mailAccess The mail access in use
     * @param processor The mail message processor maybe providing warnings
     * @return The appropriate instance
     */
    public static <R> MailStorageResult<R> resultFor(MailStorageId mailStorageId, R result, boolean validated, MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, MailMessageProcessor processor) {
        Collection<OXException> warnings;
        if (mailAccess == null) {
            warnings = processor == null ? Collections.emptyList() : processor.getWarnings();
        } else {
            if (processor == null) {
                warnings = mailAccess.getWarnings();
            } else {
                warnings = new ArrayList<>(mailAccess.getWarnings());
                warnings.addAll(processor.getWarnings());
            }
        }
        return new MailStorageResult<R>(mailStorageId, result, validated, warnings);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final MailStorageId mailStorageId;
    private final R result;
    private final boolean validated;
    private final List<OXException> warnings;

    /**
     * Initializes a new {@link MailStorageResult}.
     *
     * @param mailStorageId The recent mail storage ID of the respective draft
     * @param result The result
     * @param validated Whether draft message has been validated
     * @param warnings The listing of warnings; may be <code>null</code> to indicate no warnings
     */
    public MailStorageResult(MailStorageId mailStorageId, R result, boolean validated, Collection<? extends OXException> warnings) {
        super();
        this.mailStorageId = mailStorageId;
        this.result = result;
        this.validated = validated;
        this.warnings = warnings == null ? Collections.emptyList() : ImmutableList.copyOf(warnings);
    }

    /**
     * Gets the mail storage identifier
     *
     * @return The identifier
     */
    public MailStorageId getMailStorageId() {
        return mailStorageId;
    }

    /**
     * Checks if draft message has been validated.
     *
     * @return <code>true</code> if draft message has been validated; otherwise <code>false</code>
     */
    public boolean isValidated() {
        return validated;
    }

    /**
     * Gets the result.
     *
     * @return The result
     */
    public R getResult() {
        return result;
    }

    /**
     * Gets the warnings as an immutable list.
     *
     * @return The warnings or an empty listing; never <code>null</code>
     */
    public List<OXException> getWarnings() {
        return warnings;
    }

}

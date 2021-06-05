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

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import com.openexchange.mail.MailPath;

/**
 * {@link LookUpOutcome} - The result for a look-up of draft mails associated with a composition space.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class LookUpOutcome {

    /** The empty look-up result */
    public static final LookUpOutcome EMPTY = new LookUpOutcome(Collections.emptyMap(), Collections.emptyMap());

    private final Map<MailPath, UUID> draftPath2CompositionSpaceId;
    private final Map<MailPath, UUID> duplicateCompositionSpaces;

    /**
     * Initializes a new {@link LookUpOutcome}.
     *
     * @param draftPath2compositionSpaceId The mapping of draft path to composition space identifier
     * @param duplicateCompositionSpaces The paths of those draft messages which are considered as duplicates of a composition space
     */
    public LookUpOutcome(Map<MailPath, UUID> draftPath2compositionSpaceId, Map<MailPath, UUID> duplicateCompositionSpaces) {
        super();
        this.draftPath2CompositionSpaceId = draftPath2compositionSpaceId;
        this.duplicateCompositionSpaces = duplicateCompositionSpaces;
    }

    /**
     * Gets the mapping of draft path to composition space identifier.
     *
     * @return The mapping of draft path to composition space identifier
     */
    public Map<MailPath, UUID> getDraftPath2CompositionSpaceId() {
        return draftPath2CompositionSpaceId;
    }

    /**
     * Gets the paths of those draft messages which are considered as duplicates of a composition space.
     *
     * @return The paths of those draft messages which are considered as duplicates of a composition space
     */
    public Map<MailPath, UUID> getDuplicateCompositionSpaces() {
        return duplicateCompositionSpaces;
    }

}

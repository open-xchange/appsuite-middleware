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

package com.openexchange.consistency.internal.solver;

import static com.openexchange.java.Autoboxing.I;
import java.util.Set;
import com.openexchange.consistency.Entity;
import com.openexchange.consistency.osgi.ConsistencyServiceLookup;
import com.openexchange.contact.vcard.storage.VCardStorageMetadataStore;
import com.openexchange.exception.OXException;

/**
 * {@link DeleteBrokenVCardReferencesSolver}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class DeleteBrokenVCardReferencesSolver implements ProblemSolver {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteBrokenVCardReferencesSolver.class);

    @Override
    public void solve(Entity entity, Set<String> problems) throws OXException {
        VCardStorageMetadataStore vCardStorageMetadataStore = ConsistencyServiceLookup.getOptionalService(VCardStorageMetadataStore.class);
        if (vCardStorageMetadataStore == null) {
            LOG.warn("Required service VCardStorageMetadataStore absent. Unable to solve VCard related consistency issues on storage.");
            return;
        }
        if (problems.size() > 0) {
            vCardStorageMetadataStore.removeByRefId(entity.getContext().getContextId(), problems);
            LOG.info("Deleted {} broken VCard references.", I(problems.size()));
        }
    }

    @Override
    public String description() {
        return "delete broken VCard references";
    }
}

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

import java.util.List;
import com.openexchange.data.conversion.ical.TruncationInfo;
import com.openexchange.groupware.importexport.ImportResult;

/**
 * {@link DefaultImportResults}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class DefaultImportResults implements ImportResults {

    private final List<ImportResult> importResults;
    private final TruncationInfo truncationInfo;

    /**
     * Initializes a new {@link DefaultImportResults}.
     *
     * @param importResults The import results
     * @param truncationInfo Possible truncation info
     */
    public DefaultImportResults(List<ImportResult> importResults) {
        this(importResults, null);
    }

    /**
     * Initializes a new {@link DefaultImportResults}.
     *
     * @param importResults The import results
     * @param truncationInfo Possible truncation info
     */
    public DefaultImportResults(List<ImportResult> importResults, TruncationInfo truncationInfo) {
        super();
        this.importResults = importResults;
        this.truncationInfo = truncationInfo;
    }

    @Override
    public List<ImportResult> getImportResults() {
        return importResults;
    }

    @Override
    public TruncationInfo getTruncationInfo() {
        return truncationInfo;
    }

}

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

package com.openexchange.pgp.core;

import java.util.List;

/**
 * {@link PGPDecryptionResult}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.0
 */
public class PGPDecryptionResult {

    List<PGPSignatureVerificationResult> signatureVerificationResults;
    MDCVerificationResult                mdcVerificationResult;

    /**
     * Initializes a new {@link PGPDecryptionResult}.
     *
     * @param signatureVerificationResults results of the signature verifications
     * @param mdcVerificationResult  result of the data integrity check
     */
    public PGPDecryptionResult(List<PGPSignatureVerificationResult> signatureVerificationResults,
        MDCVerificationResult mdcVerificationResult) {
        this.signatureVerificationResults = signatureVerificationResults;
        this.mdcVerificationResult = mdcVerificationResult;
    }

    public List<PGPSignatureVerificationResult> getSignatureVerificationResults() {
        return this.signatureVerificationResults;
    }

    public void setSignatureVerificationResults(List<PGPSignatureVerificationResult> signatureVerificationResults) {
        this.signatureVerificationResults = signatureVerificationResults;
    }

    public MDCVerificationResult getMDCVerificationResult() {
        return mdcVerificationResult;
    }

    public void setMDCVerificationResult(MDCVerificationResult mdcVerificationResult) {
        this.mdcVerificationResult = mdcVerificationResult;
    }
}

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

import java.io.IOException;
import java.util.Objects;
import org.bouncycastle.openpgp.PGPException;
import com.openexchange.pgp.core.PGPDecrypter.PGPDataContainer;

/**
 * {@link MDCSignatureVerificationResult}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.0
 */
public class MDCVerificationResult {

    boolean isPresent;
    boolean verified;

    private MDCVerificationResult(boolean isPresent, boolean verified) {
        this.isPresent = isPresent;
        this.verified = verified;
    }

    public static MDCVerificationResult createFrom(PGPDataContainer data) throws PGPException, IOException {
        boolean isPresent = false;
        boolean verified = false;
        data = Objects.requireNonNull(data, "data must not be null");
        if (data.getData().isIntegrityProtected()) {
            isPresent = true;
            verified = data.getData().verify();
        }
        return new MDCVerificationResult(isPresent, verified);
    }

    public boolean isPresent() {
        return this.isPresent;
    }

    public boolean isVerified() {
       return this.verified;
    }
}

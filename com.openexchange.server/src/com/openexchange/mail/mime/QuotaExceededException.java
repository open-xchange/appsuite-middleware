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

package com.openexchange.mail.mime;

import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.Response;

/**
 * {@link QuotaExceededException} - Thrown to indicate that quota restrictions (either number of messages or storage size) were exceeded.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class QuotaExceededException extends CommandFailedException {

    private static final long serialVersionUID = 7896854480910619813L;

    /**
     * Initializes a new {@link QuotaExceededException}.
     */
    public QuotaExceededException() {
        super();
    }

    /**
     * Initializes a new {@link QuotaExceededException}.
     *
     * @param message The error message
     */
    public QuotaExceededException(String message) {
        super(message);
    }

    /**
     * Initializes a new {@link QuotaExceededException}.
     *
     * @param response The (NO) response; meaning {@link Response#isNO()} returns <code>true</code>
     */
    public QuotaExceededException(Response response) {
        super(response);
    }

}

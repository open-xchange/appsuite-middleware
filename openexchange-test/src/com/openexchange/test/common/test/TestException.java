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

package com.openexchange.test.common.test;

import com.openexchange.exception.OXException;

/**
 * Exception only for tests. This should be only used if some test is not able
 * to perform its normal operation. E.g. it does not find some folder.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TestException extends OXException {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -3793876245680533405L;

    public TestException() {
        super();
    }

    public TestException(final String message) {
        super(6667, message);
    }

    public TestException(final String message, final Exception exc) {
        super(6668, message, exc);
    }

    public TestException(final Exception exc) {
        super(exc);
    }
}

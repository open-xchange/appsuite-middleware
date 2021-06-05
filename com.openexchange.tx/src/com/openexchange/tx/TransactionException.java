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

package com.openexchange.tx;

import com.openexchange.exception.OXException;

/**
 * {@link TransactionException} - The transaction exception.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class TransactionException extends OXException {

    private static final long serialVersionUID = -8149589818960075908L;

    private final StackTraceElement[] elements;

    public TransactionException(final String logMessage, final StackTraceElement[] elements) {
        super();
        setLogMessage(logMessage);
        this.elements = elements;
    }

    public StackTraceElement[] getElements() {
        return elements;
    }

    public void printStarterTrace(){
        if (elements == null) {
            System.err.println("No Stack Trace recorded");
            return;
        }
        for(final StackTraceElement element : elements) {
            System.err.println(element);
        }
    }
}

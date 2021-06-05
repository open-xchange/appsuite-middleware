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

package com.openexchange.admin.rmi.exceptions;

import static com.openexchange.java.Autoboxing.I;

/**
 * Is thrown when user want to do an action in a context which doesn't exists in the system.
 *
 * @author cutmasta
 *
 */
public class NoSuchContextException extends AbstractAdminRmiException {

    private static final String MSG_WITH_PARAM = "Context %d does not exist";
    private static final String MSG = "Context does not exist";
    /**
     * For serialization
     */
    private static final long serialVersionUID = 1991615694615324164L;

    /**
     *
     */
    public NoSuchContextException() {
        super(MSG);
    }

    /**
     * @param ctxId
     */
    public NoSuchContextException(int ctxId) {
        super(String.format(MSG_WITH_PARAM, I(ctxId)));
    }

    /**
     * @param message
     */
    public NoSuchContextException(String message) {
        super(message);

    }

    /**
     * @param cause
     */
    public NoSuchContextException(Throwable cause) {
        super(cause);

    }

    /**
     * @param message
     * @param cause
     */
    public NoSuchContextException(String message, Throwable cause) {
        super(message, cause);
    }

}

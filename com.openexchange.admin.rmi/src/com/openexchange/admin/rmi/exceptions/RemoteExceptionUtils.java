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

import java.lang.reflect.Field;
import java.rmi.RemoteException;

/**
 * {@link RemoteExceptionUtils} - Utility class for handling of exceptions occurring during administrative RMI invocations.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public final class RemoteExceptionUtils {

    /**
     * Initializes a new {@link RemoteExceptionUtils}.
     */
    private RemoteExceptionUtils() {
        super();
    }

    /**
     * Converts given exception to a {@link RemoteException} for safe passing through RMI marshaling stack.
     *
     * @param e The exception to convert
     * @return The resulting instance of <code>RemoteException</code>
     */
    public static RemoteException convertException(Exception e) {
        if (e == null) {
            // Garbage in, garbage out...
            return null;
        }
        if (e instanceof RemoteException) {
            return (RemoteException) e;
        }
        if (e instanceof AbstractAdminRmiException) {
            AbstractAdminRmiException adminRmiException = (AbstractAdminRmiException) e;
            RemoteException cme = new RemoteException(adminRmiException.getBaseMessage(), e);
            String exceptionId = adminRmiException.getExceptionId();
            RemoteExceptionUtils.enhanceRemoteException(cme, exceptionId);
            return cme;
        }
        RemoteException cme = new RemoteException(e.getMessage());
        cme.setStackTrace(e.getStackTrace());
        return cme;
    }

    /**
     * Enhances given remote exception by a unique exception identifier.
     *
     * @param e The remote exception to enhance
     * @param exceptionId The exception identifier
     * @return The enhanced remote exception
     */
    public static RemoteException enhanceRemoteException(RemoteException e, String exceptionId) {
        Field fieldDetailMessage = FIELD_DETAIL_MESSAGE;
        if (fieldDetailMessage != null) {
            try {
                fieldDetailMessage.set(e, AbstractAdminRmiException.enhanceExceptionMessage(e.getMessage(), exceptionId));
            } catch (Exception e1) {
                // Ignore
            }
        }
        return e;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static final Field FIELD_DETAIL_MESSAGE;

    static {
        Field detailMessageField = null;
        try {
            detailMessageField = Throwable.class.getDeclaredField("detailMessage");
            detailMessageField.setAccessible(true);
        } catch (Exception e) {
            // Ignore
        }
        FIELD_DETAIL_MESSAGE = detailMessageField;
    }

}

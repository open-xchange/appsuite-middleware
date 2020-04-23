/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

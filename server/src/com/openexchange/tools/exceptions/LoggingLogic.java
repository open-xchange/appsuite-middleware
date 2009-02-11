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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.tools.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.groupware.AbstractOXException;

public abstract class LoggingLogic {

    public static LoggingLogic getLoggingLogic(final Class<?> klass) {
        // We could add hooks for custom logging logic for certain classes here, if needed.
        // For now everyone uses the default logic.

        return new DefaultLoggingLogic(LogFactory.getLog(klass));
    }

    public static LoggingLogic getLoggingLogic(final Class<?> klass, final Log log) {
        // We could add hooks for custom logging logic for certain classes here, if needed.
        // For now everyone uses the default logic.

        return new DefaultLoggingLogic(log);
    }

    protected final Log log;

    protected LoggingLogic(final Log log) {
        super();
        this.log = log;
    }

    public void log(final AbstractOXException aox) {
        switch (aox.getCategory()) {
        case CODE_ERROR:
            codeError(aox);
            break;
        case CONCURRENT_MODIFICATION:
            concurrentModification(aox);
            break;
        case EXTERNAL_RESOURCE_FULL:
            externalResourceFull(aox);
            break;
        case INTERNAL_ERROR:
            internalError(aox);
            break;
        case PERMISSION:
            permission(aox);
            break;
        case SETUP_ERROR:
            setupError(aox);
            break;
        case SOCKET_CONNECTION:
            socketConnection(aox);
            break;
        case SUBSYSTEM_OR_SERVICE_DOWN:
            subsystemOrServiceDown(aox);
            break;
        case TRUNCATED:
            truncated(aox);
            break;
        case TRY_AGAIN:
            tryAgain(aox);
            break;
        case USER_CONFIGURATION:
            userConfiguration(aox);
            break;
        case USER_INPUT:
            userInput(aox);
            break;
        case WARNING:
            warning(aox);
            break;
        default:
            unknownCategory(aox);
        }
    }

    public abstract void unknownCategory(final AbstractOXException aox);

    public abstract void warning(final AbstractOXException aox);

    public abstract void userInput(final AbstractOXException aox);

    public abstract void userConfiguration(final AbstractOXException aox);

    public abstract void tryAgain(final AbstractOXException aox);

    public abstract void truncated(final AbstractOXException aox);

    public abstract void subsystemOrServiceDown(final AbstractOXException aox);

    public abstract void socketConnection(final AbstractOXException aox);

    public abstract void setupError(final AbstractOXException aox);

    public abstract void permission(final AbstractOXException aox);

    public abstract void internalError(final AbstractOXException aox);

    public abstract void externalResourceFull(final AbstractOXException aox);

    public abstract void concurrentModification(final AbstractOXException aox);

    public abstract void codeError(final AbstractOXException aox);

}

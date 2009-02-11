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
import com.openexchange.groupware.AbstractOXException;

public class DefaultLoggingLogic extends LoggingLogic {

    public DefaultLoggingLogic(final Log log) {
        super(log);
    }

    @Override
    public void codeError(final AbstractOXException aox) {
        log.error("Coding Error: " + aox.toString(), aox);
    }

    @Override
    public void concurrentModification(final AbstractOXException aox) {
        log.debug("Concurrent Modification: " + aox.toString(), aox);
    }

    @Override
    public void externalResourceFull(final AbstractOXException aox) {
        log.error("External Resource is full: " + aox.toString(), aox);
    }

    @Override
    public void internalError(final AbstractOXException aox) {
        log.error("An internal error occurred: " + aox.toString(), aox);
    }

    @Override
    public void permission(final AbstractOXException aox) {
        log.debug("Permission Exception: " + aox.toString(), aox);
    }

    @Override
    public void setupError(final AbstractOXException aox) {
        log.error("Setup Error: " + aox.toString(), aox);
    }

    @Override
    public void socketConnection(final AbstractOXException aox) {
        log.error("Socket Connection Excpetion: " + aox.toString(), aox);
    }

    @Override
    public void subsystemOrServiceDown(final AbstractOXException aox) {
        log.error("Subsystem or service down: " + aox.toString(), aox);
    }

    @Override
    public void truncated(final AbstractOXException aox) {
        log.debug("Database truncated fields: " + aox.toString(), aox);
    }

    @Override
    public void tryAgain(final AbstractOXException aox) {
        log.error("Temporarily Disabled? " + aox.toString(), aox);
    }

    @Override
    public void unknownCategory(final AbstractOXException aox) {
        log.error("Unkown Category: " + aox.toString(), aox);
    }

    @Override
    public void userConfiguration(final AbstractOXException aox) {
        log.error("User Configuration Error: " + aox.toString(), aox);
    }

    @Override
    public void userInput(final AbstractOXException aox) {
        log.debug("User Input: " + aox.toString(), aox);
    }

    @Override
    public void warning(final AbstractOXException aox) {
        log.warn("Warning: " + aox.toString(), aox);
    }

}

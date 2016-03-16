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

package com.openexchange.sessiond.impl;

import com.openexchange.exception.OXException;
import com.openexchange.sessiond.SessionExceptionCodes;

/**
 * {@link SessionIdGenerator} - The session ID generator
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public abstract class SessionIdGenerator {

    /**
     * Initializes a new {@link SessionIdGenerator}
     */
    protected SessionIdGenerator() {
        super();
    }

    private static String implementingClassName;

    /**
     * Proxy attribute for the class implementing this interface.
     */
    private static Class<? extends SessionIdGenerator> implementingClass;

    /**
     * Creates a new instance implementing the group storage interface.
     *
     * @param context Context.
     * @return an instance implementing the group storage interface.
     * @throws OXException if the instance can't be created.
     */
    public static SessionIdGenerator getInstance() throws OXException {
        try {
            return getImplementingClass().getConstructor().newInstance();
        } catch (final Exception exc) {
            throw SessionExceptionCodes.SESSIOND_EXCEPTION.create(exc);
        }
    }

    /**
     * Proxy method to get the implementing class.
     *
     * @return the class implementing this interface.
     * @throws ClassNotFoundException if the class can't be loaded.
     */
    private synchronized static Class<? extends SessionIdGenerator> getImplementingClass() throws ClassNotFoundException {
        if (implementingClassName == null) {
            implementingClass = UUIDSessionIdGenerator.class;
        }

        if (null == implementingClass) {
            implementingClass = Class.forName(implementingClassName).asSubclass(SessionIdGenerator.class);
        }
        return implementingClass;
    }

    public static void setImplementClassName(final String implementClassName) {
        implementingClassName = implementClassName;
    }

    public abstract String createSessionId(String userId, String data) throws OXException;

    public abstract String createSecretId(String userId, String data) throws OXException;

    public abstract String createRandomId() throws OXException;
}

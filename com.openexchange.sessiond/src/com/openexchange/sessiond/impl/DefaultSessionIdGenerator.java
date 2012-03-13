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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.sessiond.SessionExceptionCodes;

/**
 * {@link DefaultSessionIdGenerator} - The default session ID generator.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DefaultSessionIdGenerator extends SessionIdGenerator {

    @Override
    public String createSessionId(final String userId, final String data) throws OXException {
        return getUniqueId(userId, data);
    }

    @Override
    public String createSecretId(final String userId, final String data) throws OXException {
        return getUniqueId(userId, data);
    }

    @Override
    public String createRandomId() {
        return UUID.randomUUID().toString();
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static String getUniqueId(final String userId, final String data) throws OXException {
        try {
            final StringBuilder builder = new StringBuilder(32);
            final byte[] digest;
            {
                final byte[] buf = builder.append(System.currentTimeMillis()).append(SECURE_RANDOM.nextLong()).append(userId).append('.').append(
                    data).toString().getBytes();
                builder.setLength(0);
                final MessageDigest algorithm = MessageDigest.getInstance("MD5");
                algorithm.reset();
                algorithm.update(buf);
                digest = algorithm.digest();
            }
            for (final byte element : digest) {
                final String hex = Integer.toHexString(element & 0xff);
                if (hex.length() < 2) {
                    builder.append('0');
                }
                builder.append(hex);
            }
            return builder.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw SessionExceptionCodes.SESSIOND_EXCEPTION.create(e);
        }
    }
}

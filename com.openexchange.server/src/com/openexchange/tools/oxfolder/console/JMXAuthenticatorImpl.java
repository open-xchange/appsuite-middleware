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

package com.openexchange.tools.oxfolder.console;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;
import org.apache.commons.codec.binary.Base64;

final class JMXAuthenticatorImpl implements JMXAuthenticator {

    private final String[] credentials;

    public JMXAuthenticatorImpl(final String[] credentials) {
        super();
        this.credentials = new String[credentials.length];
        System.arraycopy(credentials, 0, this.credentials, 0, credentials.length);
    }

    @Override
    public Subject authenticate(final Object credentials) {
        if (!(credentials instanceof String[])) {
            if (credentials == null) {
                throw new SecurityException("Credentials required");
            }
            throw new SecurityException("Credentials should be String[]");
        }
        final String[] creds = (String[]) credentials;
        if (creds.length != 2) {
            throw new SecurityException("Credentials should have 2 elements");
        }
        /*
         * Perform authentication
         */
        final String username = creds[0];
        final String password = creds[1];
        if ((this.credentials[0].equals(username)) && (this.credentials[1].equals(password))) {
            return new Subject(true, Collections.singleton(new JMXPrincipal(username)), Collections.EMPTY_SET, Collections.EMPTY_SET);
        }
        throw new SecurityException("Invalid credentials");

    }

    private static String makeSHAPasswd(final String raw) {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (final NoSuchAlgorithmException e) {
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(JMXAuthenticatorImpl.class)).error(e.getMessage(), e);
            return raw;
        }

        final byte[] salt = {};

        md.reset();
        md.update(raw.getBytes(com.openexchange.java.Charsets.UTF_8));
        md.update(salt);

        try {
            return Charset.forName("US-ASCII").decode(ByteBuffer.wrap(Base64.encodeBase64(md.digest()))).toString();
        } catch (final IllegalCharsetNameException e) {
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(JMXAuthenticatorImpl.class)).error(e.getMessage(), e);
        } catch (final UnsupportedCharsetException e) {
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(JMXAuthenticatorImpl.class)).error(e.getMessage(), e);
        }

        return null;

    }

}

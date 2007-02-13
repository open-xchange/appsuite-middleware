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



package com.openexchange.groupware.ldap;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Encoder;

import com.openexchange.groupware.Component;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException.Code;

/**
 * This class implements the authentication using a relational database instead
 * of a directory service.
 */
public class RdbAuthentication extends Authentication {

    /**
     * Reference to the context.
     */
    private final transient Context context;

    /**
     * Default constructor.
     * @param context Context.
     */
    public RdbAuthentication(final Context context) {
        super();
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    public Credentials authenticate(final String uid, final String password)
        throws LdapException {
        Credentials retval = null;
        if (null != password && password.length() > 0) {
            final UserStorage users = UserStorage.getInstance(context);
            final int userId = users.getUserId(uid);
            final User user = users.getUser(userId);
            final String propertyValue = LdapUtility.findProperty(
                "checkPassword", false);
            final boolean testPassword;
            if (null == propertyValue) {
                testPassword = true;
            } else {
                testPassword = Boolean.parseBoolean(propertyValue);
            }
            if (!testPassword || hashPassword(password)
                .equals(user.getUserPassword())) {
                retval = new RdbCredentials(userId);
            }
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPasswordExpired(final String uid) throws LdapException {
        final UserStorage users = UserStorage.getInstance(context);
        final int userId = users.getUserId(uid);
        final User user = users.getUser(userId);
        return user.getShadowLastChange() == 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUserEnabled(final String uid) throws LdapException {
        final UserStorage users = UserStorage.getInstance(context);
        final int userId = users.getUserId(uid);
        final User user = users.getUser(userId);
        return user.isMailEnabled();
    }

    /**
     * This method hashes a clear text password using the SHA-1 algorithm and
     * returns the base64 encoded value of the hash.
     * @param password clear text password.
     * @return the base64 encoded value of the hash.
     * @throws LdapException if the password can't be hashed.
     */
    protected String hashPassword(final String password) throws LdapException {
        String hashed = null;
        try {
            final MessageDigest sha = MessageDigest.getInstance("SHA-1");
            sha.update(password.getBytes("UTF-8"));
            final byte[] hash = sha.digest();
            hashed = new BASE64Encoder().encode(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new LdapException(Component.USER, Code.HASH_ALGORITHM, e,
                "SHA-1");
        } catch (UnsupportedEncodingException e) {
            throw new LdapException(Component.USER, Code.UNSUPPORTED_ENCODING,
                e, "UTF-8");
        }
        return hashed;
    }
}

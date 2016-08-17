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

package com.openexchange.passwordmechs;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import com.damienmiller.BCrypt;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.passwordmechs.mechs.SHACrypt;
import com.openexchange.passwordmechs.mechs.UnixCrypt;

/**
 * {@link PasswordMech} - An enumeration for supported password hashing mechanisms.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public enum PasswordMech implements IPasswordMech {

    /**
     * Encoder for CRYPT<br>
     * <br>
     * Do not use this enumeration directly to encode and check. Instead get the {@link IPasswordMech} by using com.openexchange.passwordmechs.PasswordMechFactory.get(com.openexchange.passwordmechs.IPasswordMech.CRYPT)
     */
    CRYPT(IPasswordMech.CRYPT, new Encoder() {

        @Override
        public String encode(String str) throws OXException {
            try {
                return UnixCrypt.crypt(str);
            } catch (UnsupportedEncodingException e) {
                LOG.error("Error encrypting password according to CRYPT mechanism", e);
                throw PasswordMechExceptionCode.UNSUPPORTED_ENCODING.create(e, e.getMessage());
            }
        }

        @Override
        public boolean check(String candidate, String encoded) throws OXException {
            if ((Strings.isEmpty(candidate)) && (Strings.isEmpty(encoded))) {
                return true;
            } else if ((Strings.isEmpty(candidate)) && (Strings.isNotEmpty(encoded))) {
                return false;
            } else if ((Strings.isNotEmpty(candidate)) && (Strings.isEmpty(encoded))) {
                return false;
            }
            try {
                return UnixCrypt.matches(encoded, candidate);
            } catch (UnsupportedEncodingException e) {
                LOG.error("Error checking password according to CRYPT mechanism", e);
                throw PasswordMechExceptionCode.UNSUPPORTED_ENCODING.create(e, e.getMessage());
            }
        }
    }),
    /**
     * Encoder for SHA<br>
     * <br>
     * Do not use this enumeration directly to encode and check. Instead get the {@link IPasswordMech} by using com.openexchange.passwordmechs.PasswordMechFactory.get(com.openexchange.passwordmechs.IPasswordMech.SHA)
     */
    SHA(IPasswordMech.SHA, new Encoder() {

        @Override
        public String encode(String str) throws OXException {
            try {
                return SHACrypt.makeSHAPasswd(str);
            } catch (NoSuchAlgorithmException e) {
                LOG.error("Error encrypting password according to SHA mechanism", e);
                throw PasswordMechExceptionCode.UNSUPPORTED_ENCODING.create(e, e.getMessage());
            }
        }

        @Override
        public boolean check(String candidate, String encoded) throws OXException {
            if ((Strings.isEmpty(candidate)) && (Strings.isEmpty(encoded))) {
                return true;
            } else if ((Strings.isEmpty(candidate)) && (Strings.isNotEmpty(encoded))) {
                return false;
            } else if ((Strings.isNotEmpty(candidate)) && (Strings.isEmpty(encoded))) {
                return false;
            }
            try {
                return SHACrypt.makeSHAPasswd(candidate).equals(encoded);
            } catch (NoSuchAlgorithmException e) {
                LOG.error("Error checking password according to SHA mechanism", e);
                throw PasswordMechExceptionCode.UNSUPPORTED_ENCODING.create(e, e.getMessage());
            }
        }
    }),
    /**
     * Encoder for BCRYPT<br>
     * <br>
     * Do not use this enumeration directly to encode and check. Instead get the {@link IPasswordMech} by using com.openexchange.passwordmechs.PasswordMechFactory.get(com.openexchange.passwordmechs.IPasswordMech.BCRYPT)
     */
    BCRYPT(IPasswordMech.BCRYPT, new Encoder() {

        @Override
        public String encode(String str) {
            return BCrypt.hashpw(str, BCrypt.gensalt());
        }

        @Override
        public boolean check(String candidate, String encoded) {
            if ((Strings.isEmpty(candidate)) && (Strings.isEmpty(encoded))) {
                return true;
            } else if ((Strings.isEmpty(candidate)) && (Strings.isNotEmpty(encoded))) {
                return false;
            } else if ((Strings.isNotEmpty(candidate)) && (Strings.isEmpty(encoded))) {
                return false;
            }
            return BCrypt.checkpw(candidate, encoded);
        }
    }),

    ;

    // --------------------------------------------------------------------------------------- //

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordMech.class);

    private final Encoder encoder;
    private final String identifier;

    /**
     * Initializes a new {@link PasswordMech}.
     */
    private PasswordMech(String identifier, Encoder encoder) {
        this.identifier = identifier;
        this.encoder = encoder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String encode(String str) throws OXException {
        if (null == str) {
            return null;
        }
        return encoder.encode(str);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean check(String toCheck, String encoded) throws OXException {
        return encoder.check(toCheck, encoded);
    }

    /**
     * {@inheritDoc}
     * {@link IPasswordMech}s within this class only provide one-way-enconding
     */
    @Override
    public String decode(String encodedPassword) throws OXException {
        throw PasswordMechExceptionCode.UNSUPPORTED_OPERATION.create(getIdentifier());
    }

    // --------------------------------------------------------------------------------------- //

    private static interface Encoder {

        String encode(String str) throws OXException;

        boolean check(String candidate, String encoded) throws OXException;
    }
}

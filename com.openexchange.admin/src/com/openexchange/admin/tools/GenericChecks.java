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

package com.openexchange.admin.tools;

import java.util.List;
import javax.mail.internet.AddressException;
import com.openexchange.admin.rmi.dataobjects.PasswordMechObject;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.password.mechanism.IPasswordMech;
import com.openexchange.password.mechanism.PasswordMechRegistry;

/**
 * @author choeger
 */
public class GenericChecks {

    //    ftp://ftp.rfc-editor.org/in-notes/rfc2822.txt
    //
    //        3.2.4. Atom
    //
    //        atext           =       ALPHA / DIGIT / ; Any character except controls,
    //                                "!" / "#" /     ;  SP, and specials.
    //                                "$" / "%" /     ;  Used for atoms
    //                                "&" / "'" /
    //                                "*" / "+" /
    //                                "-" / "/" /
    //                                "=" / "?" /
    //                                "^" / "_" /
    //                                "`" / "{" /
    //                                "|" / "}" /
    //                                "~"
    /**
     * This method checks if an address contains invalid characters
     *
     * @param address The address string to check
     */
    @SuppressWarnings("unused")
    public final static boolean isValidMailAddress(String address) {
        if (null == address) {
            return false;
        }
        try {
            new QuotedInternetAddress(address);
            return true;
        } catch (final AddressException e) {
            return false;
        }
    }

    /**
     * This method throws an exception if the address is != null and contains invalid characters
     *
     * @param address The address string to check
     * @throws InvalidDataException If given address string is not a valid email address
     */
    public final static void checkValidMailAddress(String address) throws InvalidDataException {
        if (Strings.isNotEmpty(address) && !isValidMailAddress(address)) {
            throw new InvalidDataException("Invalid email address");
        }
    }

    /**
     * Checks whether supplied password mech is a valid password mech
     * as specified in {@link PasswordMechObject}.
     *
     * Checks whether password is not an empty string.
     *
     * Checks checks whether mech has changed without supplying new
     * password string
     *
     * @param user
     * @throws InvalidDataException
     */
    public final static void checkChangeValidPasswordMech(PasswordMechObject user) throws InvalidDataException {
        checkCreateValidPasswordMech(user);
        if (user.getPasswordMech() != null && user.getPassword() == null) {
            throw new InvalidDataException("When changing password mechanism, the password string must also be supplied");
        }
    }

    /**
     * Checks whether supplied password mech is a valid password mech
     * as specified in {@link PasswordMechObject}.
     *
     * @param user
     * @throws InvalidDataException
     */
    public final static void checkCreateValidPasswordMech(PasswordMechObject user) throws InvalidDataException {
        final String mech = user.getPasswordMech();
        if (mech == null) {
            return;
        }

        try {
            PasswordMechRegistry mechFactory = AdminServiceRegistry.getInstance().getService(PasswordMechRegistry.class, true);
            List<String> knownIdentifiers = mechFactory.getApplicableIdentifiers();
            for (String identifier : knownIdentifiers) {
                if (identifier.equalsIgnoreCase(mech)) {
                    return;
                }
            }
            throw new InvalidDataException("Invalid PasswordMech: " + mech + ". Use one of the following: " + String.join(",", mechFactory.getIdentifiers()));
        } catch (OXException e) {
            throw new InvalidDataException("PasswordMechFactory not available. Did the server start properly?");
        }
    }

    /**
     * Authenticate the clear text password against the encrypted string using the
     * specified {@link IPasswordMech}
     *
     * @param crypted The encrypted password
     * @param clear The password in clear text
     * @param mech The password mechanism to use
     * @param salt The salt used for encoding
     * @return <code>true</code> if authentication succeeds and false if it fails
     */
    public final static boolean authByMech(String crypted, String clear, String mech, String salt) {
        if (Strings.isEmpty(mech)) {
            return false;
        }
        try {
            PasswordMechRegistry mechFactory = AdminServiceRegistry.getInstance().getService(PasswordMechRegistry.class, true);
            IPasswordMech passwordMech = mechFactory.get(mech);
            if (null != passwordMech) {
                return passwordMech.check(clear, crypted, salt);
            }
        } catch (OXException e) {
            // Ignore
        }
        return false;
    }
}

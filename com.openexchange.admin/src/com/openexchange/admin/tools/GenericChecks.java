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

package com.openexchange.admin.tools;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import javax.mail.internet.AddressException;
import org.slf4j.Logger;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.properties.PropertyScope;
import com.openexchange.admin.rmi.dataobjects.PasswordMechObject;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.password.mechanism.PasswordMech;
import com.openexchange.password.mechanism.PasswordMechRegistry;

/**
 * {@link GenericChecks}
 *
 * @author choeger
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class GenericChecks {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(GenericChecks.class);
    }

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
    public static boolean isValidMailAddress(String address) {
        if (null == address) {
            return false;
        }
        try {
            new QuotedInternetAddress(address);
            return true;
        } catch (AddressException e) {
            return false;
        }
    }

    /**
     * This method checks if an address contains invalid characters according to configured regular expression through {@link AdminProperties.User#ADDITIONAL_EMAIL_CHECK_REGEX} property.
     *
     * @param address The address string to check
     * @param propertyScope The scope to apply
     */
    public static boolean isValidMailAddress(String address, PropertyScope propertyScope) {
        if (null == address) {
            return false;
        }
        if (propertyScope == null) {
            return true;
        }

        String regex = AdminProperties.optScopedProperty(AdminProperties.User.ADDITIONAL_EMAIL_CHECK_REGEX, propertyScope, String.class);
        if (Strings.isEmpty(regex)) {
            return true;
        }

        try {
            return isValidMailAddress(address, Pattern.compile(regex));
        } catch (PatternSyntaxException e) {
            LoggerHolder.LOG.warn("Unable to compile the value of the '{}' property to a regular expression. E-Mail address cannot be further checked for validity.", AdminProperties.User.ADDITIONAL_EMAIL_CHECK_REGEX, e);
        }
        return true;
    }

    /**
     * This method checks if an address contains invalid characters according to given pattern.
     *
     * @param address The address string to check
     * @param pattern The compiled representation of the regular expression to validate the address
     */
    public static boolean isValidMailAddress(String address, Pattern pattern) {
        if (null == address) {
            return false;
        }
        try {
            return pattern.matcher(address).matches();
        } catch (@SuppressWarnings("unused") Exception e) {
            return false;
        }
    }

    /**
     * This method throws an exception if the address is != null and contains invalid characters
     *
     * @param address The address string to check
     * @throws InvalidDataException If given address string is not a valid email address
     */
    public static void checkValidMailAddress(String address) throws InvalidDataException {
        if (Strings.isNotEmpty(address) && false == isValidMailAddress(address)) {
            throw new InvalidDataException("Invalid email address: " + address);
        }
    }

    /**
     * Performs an e-mail address check based on configured regular expression through {@link AdminProperties.User#ADDITIONAL_EMAIL_CHECK_REGEX} property.
     *
     * @param address The address string to check
     * @param propertyScope The scope to apply
     * @throws InvalidDataException If given regular expression pattern does not match the given address string
     */
    public static void checkValidMailAddressRegex(String address, PropertyScope propertyScope) throws InvalidDataException {
        if (Strings.isEmpty(address) || propertyScope == null) {
            return;
        }

        String regex = AdminProperties.optScopedProperty(AdminProperties.User.ADDITIONAL_EMAIL_CHECK_REGEX, propertyScope, String.class);
        if (Strings.isEmpty(regex)) {
            return;
        }

        try {
            checkValidMailAddressRegex(address, Pattern.compile(regex));
        } catch (PatternSyntaxException e) {
            LoggerHolder.LOG.warn("Unable to compile the value of the '{}' property to a regular expression. E-Mail address cannot be further checked for validity.", AdminProperties.User.ADDITIONAL_EMAIL_CHECK_REGEX, e);
        }
    }

    /**
     * Performs an e-mail address check based on given pattern.
     *
     * @param address The address string to check
     * @param pattern The compiled representation of the regular expression to validate the address
     * @throws InvalidDataException If given regular expression pattern does not match the given address string
     */
    public static void checkValidMailAddressRegex(String address, Pattern pattern) throws InvalidDataException {
        if (Strings.isNotEmpty(address) && false == isValidMailAddress(address, pattern)) {
            throw new InvalidDataException("Invalid email address");
        }
    }

    /**
     * Checks whether supplied password mechanism is a valid password mechanism
     * as specified in {@link PasswordMechObject}.
     *
     * Checks whether password is not an empty string.
     *
     * Checks checks whether mechanism has changed without supplying new
     * password string.
     *
     * @param passwordMech The password mechanism
     * @throws InvalidDataException If password is absent in given password mechanism object
     */
    public static void checkChangeValidPasswordMech(PasswordMechObject passwordMech) throws InvalidDataException {
        checkCreateValidPasswordMech(passwordMech);
        if (passwordMech.getPasswordMech() != null && passwordMech.getPassword() == null) {
            throw new InvalidDataException("When changing password mechanism, the password string must also be supplied");
        }
    }

    /**
     * Checks whether supplied password mechanism is a valid password mechanism as specified in {@link PasswordMechObject}.
     *
     * @param passwordMech The password mechanism
     * @throws InvalidDataException If such a password mechanism is not available at runtime
     */
    public static void checkCreateValidPasswordMech(PasswordMechObject passwordMech) throws InvalidDataException {
        String mech = passwordMech.getPasswordMech();
        if (mech == null) {
            return;
        }

        try {
            PasswordMechRegistry mechFactory = AdminServiceRegistry.getInstance().getService(PasswordMechRegistry.class, true);
            List<String> identifiers = mechFactory.getIdentifiers();
            for (String identifier : identifiers) {
                if (identifier.equalsIgnoreCase(mech) || identifier.equalsIgnoreCase("{" + mech + "}")) {
                    return;
                }
            }
            String ids = identifiers.stream().map(i -> i.replaceAll("\\{", "")).map(i -> i.replaceAll("\\}", "")).collect(Collectors.joining(", ", "", ""));
            throw new InvalidDataException("Invalid PasswordMech: " + mech + ". Use one of the following: " + ids);
        } catch (@SuppressWarnings("unused") OXException e) {
            throw new InvalidDataException("PasswordMechFactory not available. Did the server start properly?");
        }
    }

    /**
     * Authenticate the clear text password against the encrypted string using the specified {@link PasswordMech}.
     *
     * @param crypted The encrypted password
     * @param clear The password in clear text
     * @param mech The password mechanism to use
     * @param salt The salt used for encoding
     * @return <code>true</code> if authentication succeeds and false if it fails
     */
    public static boolean authByMech(String crypted, String clear, String mech, byte[] salt) {
        if (Strings.isEmpty(mech)) {
            return false;
        }
        try {
            PasswordMechRegistry mechFactory = AdminServiceRegistry.getInstance().getService(PasswordMechRegistry.class, true);
            PasswordMech passwordMech = mechFactory.get(mech);
            if (null != passwordMech) {
                return passwordMech.check(clear, crypted, salt);
            }
        } catch (@SuppressWarnings("unused") OXException e) {
            // Ignore
        }
        return false;
    }

}

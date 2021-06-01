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

package com.openexchange.contact.storage.ldap.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ConfigurationServices;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.storage.ldap.LdapExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.l10n.SuperCollator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * {@link Tools}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Tools  {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(Tools.class);

    private static final String DIRECTORY_NAME = "contact-storage-ldap";

    private Tools() {
        super();
    }

    public static Properties loadProperties(String fileName) throws OXException {
        try {
            return ConfigurationServices.loadPropertiesFrom(getFile(fileName), true);
        } catch (FileNotFoundException e) {
            throw LdapExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw LdapExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

    private static File getFile(String fileName) throws OXException {
        File file = new File(fileName);
        if (false == file.isAbsolute()) {
            File directory = LdapServiceLookup.getService(ConfigurationService.class).getDirectory(DIRECTORY_NAME);
            return new File(directory, fileName);
        }
        return file;
    }

    public static Properties loadProperties(File file) throws OXException {
        try {
            return ConfigurationServices.loadPropertiesFrom(file, true);
        } catch (FileNotFoundException e) {
            throw LdapExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw LdapExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

    public static File[] listPropertyFiles() throws OXException {
        File directory = LdapServiceLookup.getService(ConfigurationService.class).getDirectory(DIRECTORY_NAME);
        return directory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return null != name && name.toLowerCase(Locale.US).endsWith(".properties");
            }
        });
    }

    public static void close(NamingEnumeration<?> namingEnumeration) {
        if (null != namingEnumeration) {
            try {
                namingEnumeration.close();
            } catch (NamingException e) {
                LOG.warn("Error closing naming enumeration", e);
            }
        }
    }

    public static void close(LdapContext ldapContext) {
        if (null != ldapContext) {
            try {
                ldapContext.close();
            } catch (NamingException e) {
                LOG.warn("Error closing LDAP conext", e);
            }
        }
    }

    public static void close(SearchIterator<?> searchIterator) {
        SearchIterators.close(searchIterator);
    }

    public static Locale getLocale(SortOptions sortOptions) {
        if (null != sortOptions.getCollation()) {
            SuperCollator collator = SuperCollator.get(sortOptions.getCollation());
            if (null != collator) {
                return collator.getJavaLocale();
            }
        }
        return null;
    }

    /**
     *
     * @param ldapfilter
     * @return
     */
    public static String escapeLDAPSearchFilter(final String ldapfilter) {
        // According to RFC2254 section 4 we escape the following chars so that no LDAP injection can be made:
        // Character       ASCII value
        // ---------------------------
        // *               0x2a
        // (               0x28
        // )               0x29
        // \               0x5c
        // NUL             0x00
        if (ldapfilter == null) {
            return "";
        }
        final StringBuilder sa = new StringBuilder();
        for (int i = 0; i < ldapfilter.length(); i++) {
            final char curChar = ldapfilter.charAt(i);
            switch (curChar) {
                case '\\':
                    sa.append("\\5c");
                    break;
// We always treat "*" as wildcard
//                case '*':
//                    sb.append("\\2a");
//                    break;
                case '(':
                    sa.append("\\28");
                    break;
                case ')':
                    sa.append("\\29");
                    break;
                case '\u0000':
                    sa.append("\\00");
                    break;
                case '?':
                    sa.append('*');
                    break;
                default:
                    sa.append(curChar);
            }
        }
        return sa.toString();
    }

    /**
     * Parses a numerical identifier from a string, wrapping a possible
     * NumberFormatException into an OXException.
     *
     * @param id the id string
     * @return the parsed identifier
     * @throws OXException
     */
    public static int parse(String id) throws OXException {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw ContactExceptionCodes.ID_PARSING_FAILED.create(e, id);
        }
    }


}

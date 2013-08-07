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

package com.openexchange.contacts.ldap.property;

import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationExceptionCode;
import com.openexchange.contacts.ldap.osgi.LDAPServiceRegistry;
import com.openexchange.exception.OXException;

/**
 * A class which will deal with all property related actions.
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class PropertyHandler {

    public static final String bundlename = "com.openexchange.contacts.ldap.";

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(PropertyHandler.class));

    private static PropertyHandler singleton = new PropertyHandler();

    private final Map<Integer, ContextProperties> contextdetails = new ConcurrentHashMap<Integer, ContextProperties>();

    private final AtomicBoolean loaded = new AtomicBoolean();

    public static PropertyHandler getInstance() {
        return singleton;
    }

    public final Map<Integer, ContextProperties> getContextdetails() {
        return contextdetails;
    }

    public void loadProperties() throws OXException {
        final StringBuilder logBuilder = new StringBuilder();

        final File[] dirs;
        {
            final String pathname = System.getProperty("openexchange.propdir") + "/contacts-ldap";
            dirs = directorylisting(new File(pathname));

            if (null == dirs) {
                throw LdapConfigurationExceptionCode.NOT_DIRECTORY.create(pathname);
            }
        }

        final ConfigurationService configuration = LDAPServiceRegistry.getInstance().getService(ConfigurationService.class);

        logBuilder.append("\nLoading Contacts-LDAP properties...\n");
        for (final File dir : dirs) {
            // First check if the foldername is a valid context id, so containing of an integer value
            final int contextid;
            try {
                contextid = Integer.parseInt(dir.getName());
            } catch (final NumberFormatException e) {
                // TODO right exception here
                throw LdapConfigurationExceptionCode.DIRECTORY_IS_NOT_A_CONTEXT_ID.create(dir.getName());
            }
            final ContextProperties contextprops = ContextProperties.getContextPropertiesFromDir(configuration, dir, contextid, logBuilder);
            this.contextdetails.put(contextid, contextprops);
        }

//        for (final Integer ctx : this.contexts) {
//            final String stringctx = String.valueOf(ctx);
//            final Properties file = configuration.getFile(stringctx + ".properties");
//            this.contextdetails.put(ctx, ContextProperties.getContextPropertiesFromProperties(file, stringctx));
//        }
        this.loaded.set(true);
        if (LOG.isInfoEnabled()) {
            LOG.info(logBuilder.toString());
        }
    }

    private static File[] directorylisting(final File file) {
        final File[] listFiles = file.listFiles(new FileFilter() {

            @Override
            public boolean accept(final File pathname) {
                return pathname.isDirectory();
            }

        });
        return listFiles;
    }

    public void reloadProperties() {

    }

    /**
     * @param props
     * @param propertyname
     * @return the propertyvalue if the property exists and is != null or null otherwise
     */
    public static String checkStringProperty(final Properties props, final String propertyname) {
        final String property = props.getProperty(propertyname);
        if (null != property && 0 != property.length()) {
            return property;
        } else {
            return null;
        }
    }

//    private List<Integer> getContexts(String name) throws OXException {
//        final String property = this.properties.getProperty(name);
//        if (null != property) {
//            final List<Integer> retval = new ArrayList<Integer>();
//            final String[] split = property.split(",");
//            for (final String ctx : split) {
//                try {
//                    retval.add(Integer.parseInt(ctx));
//                } catch (final NumberFormatException e) {
//                    throw new OXException(Code.NO_INTEGER_VALUE, ctx);
//                }
//            }
//            return retval;
//        } else {
//            throw new OXException(Code.PARAMETER_NOT_SET, name);
//        }
//    }
}

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

package com.openexchange.drive.restricted.loader.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.drive.restricted.loader.StringsProvider;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;

/**
 * {@link APNResourceLoader}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.2
 */
public class APNResourceLoader implements ResourceLoader {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(APNResourceLoader.class);
    }

    /** The type for the APN push */
    public static enum Type {
        iOS,
        macOS
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Type type;

    /**
     * Initializes a new {@link APNResourceLoader}.
     *
     * @param type The APN push type (either iOS or Mac OS)
     */
    public APNResourceLoader(Type type) {
        super();
        this.type = type;
    }

    private static String getAPNEnabledPropertyName(Type type, StringsProvider strings) {
        final String retval;
        switch (type) {
            case iOS:
                retval = strings.getAPNiOSEnabledPropertyName();
                break;
            case macOS:
                retval = strings.getAPNmacOSEnabledPropertyName();
                break;
            default:
                retval = null;
        }
        return retval;
    }

    private static String getAPNKeystorePropertyName(Type type, StringsProvider strings) {
        final String retval;
        switch (type) {
            case iOS:
                retval = strings.getAPNiOSKeystorePropertyName();
                break;
            case macOS:
                retval = strings.getAPNmacOSKeystorePropertyName();
                break;
            default:
                retval = null;
        }
        return retval;
    }

    private static String getAPNPasswordPropertyName(Type type, StringsProvider strings) {
        final String retval;
        switch (type) {
            case iOS:
                retval = strings.getAPNiOSPasswordPropertyName();
                break;
            case macOS:
                retval = strings.getAPNmacOSPasswordPropertyName();
                break;
            default:
                retval = null;
        }
        return retval;
    }

    private static String getAPNProductionPropertyName(Type type, StringsProvider strings) {
        final String retval;
        switch (type) {
            case iOS:
                retval = strings.getAPNiOSProductionPropertyName();
                break;
            case macOS:
                retval = strings.getAPNmacOSProductionPropertyName();
                break;
            default:
                retval = null;
        }
        return retval;
    }

    @Override
    public void load(ConfigView view, StringsProvider strings) {
        InputStream is = null;
        try {
            is = getClass().getClassLoader().getResourceAsStream(strings.getPropertiesFilename());
            if (null == is) {
                LoggerHolder.LOG.error("Could not load resource {} from com.openexchange.restricted fragment.", strings.getPropertiesFilename());
            } else {
                Properties props = new Properties();
                props.load(is);
                Streams.close(is);
                is = null;

                String keystorePropertyName = getAPNKeystorePropertyName(type, strings);
                if (null == keystorePropertyName) {
                    return;
                }
                String keystore = props.getProperty(keystorePropertyName);
                if (null == keystore) {
                    throw new IOException("Definition of keystore file is missing.");
                }
                String password = props.getProperty(getAPNPasswordPropertyName(type, strings));
                if (null == password) {
                    throw new IOException("Definition of password for keystore file is missing.");
                }
                Boolean production = Boolean.valueOf(props.getProperty(getAPNProductionPropertyName(type, strings), Boolean.TRUE.toString()));
                InputStream keystoreIS = null;
                try {
                    keystoreIS = getClass().getClassLoader().getResourceAsStream(keystore);
                    if (null == keystoreIS) {
                        throw new IOException("Unable to open keystore file " + keystore);
                    }
                    String bytes = new String(Streams.stream2bytes(keystoreIS));
                    Streams.close(keystoreIS);
                    keystoreIS = null;

                    String scope = strings.getConfigCascadeScope();
                    view.set(scope, getAPNEnabledPropertyName(type, strings), Boolean.TRUE);
                    view.set(scope, keystorePropertyName, bytes);
                    view.set(scope, getAPNPasswordPropertyName(type, strings), password);
                    view.set(scope, getAPNProductionPropertyName(type, strings), production);
                } finally {
                    Streams.close(keystoreIS);
                }
            }
        } catch (IOException e) {
            LoggerHolder.LOG.error("", e);
        } catch (OXException e) {
            LoggerHolder.LOG.error("", e);
        } finally {
            Streams.close(is);
        }
    }

    @Override
    public void unload(ConfigView view, StringsProvider strings) {
        try {
            String scope = strings.getConfigCascadeScope();
            view.set(scope, getAPNEnabledPropertyName(type, strings), null);
            view.set(scope, getAPNKeystorePropertyName(type, strings), null);
            view.set(scope, getAPNPasswordPropertyName(type, strings), null);
            view.set(scope, getAPNProductionPropertyName(type, strings), null);
        } catch (OXException e) {
            LoggerHolder.LOG.error(e.getMessage(), e);
        }
    }
}

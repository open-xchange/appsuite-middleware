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

package com.openexchange.imageserver.api;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link ImageServerConfig}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.8.0
 */
public class ImageServerConfig extends Properties {

    //--------------------------------------------------------------------------
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5909338749193333017L;

    //--------------------------------------------------------------------------
    /**
     * Property keys
     */

    private static final String PROP_DB_TYPE = "DB_TYPE";

    private static final String PROP_DB_TYPE_DEFAULT = "mysql";

    private static final String PROP_DB_HOST = "DB_HOST";

    private static final String PROP_DB_PORT = "DB_PORT";

    private static final String PROP_DB_USERNAME = "DB_USERNAME";

    private static final String PROP_DB_USERPASSWORD = "DB_USERPASSWORD";

    //--------------------------------------------------------------------------
    /**
     * Initializes a new {@link ImageServerConfig} singleton
     */
    private ImageServerConfig() {
        super();
    }

    //--------------------------------------------------------------------------
    /**
     * @return The FileServerConfig singleton
     */
    public static synchronized ImageServerConfig get() {
        ImageServerConfig config = m_instance.get();

        if (null == config) {
            m_instance.set(config = new ImageServerConfig());
        }

        return config;
    }

    // - Implementation --------------------------------------------------------

    //--------------------------------------------------------------------------
    public String getDBType() {
        return getProperty(PROP_DB_TYPE, PROP_DB_TYPE_DEFAULT);
    }

    //--------------------------------------------------------------------------
    public String getDBHost() {
        return getProperty(PROP_DB_HOST);
    }

    //--------------------------------------------------------------------------
    public String getDBPort() {
        return getProperty(PROP_DB_PORT);
    }

    //--------------------------------------------------------------------------
    public String getDBUserName() {
        return getProperty(PROP_DB_USERNAME);
    }

    //--------------------------------------------------------------------------
    public String getDBUserPassword() {
        return getProperty(PROP_DB_USERPASSWORD);
    }

    //- Members ----------------------------------------------------------------

    private static AtomicReference<ImageServerConfig> m_instance = new AtomicReference<>(null);
}

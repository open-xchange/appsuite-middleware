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

package com.openexchange.mail.smal.impl;

import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.AbstractProtocolProperties;

/**
 * {@link SmalStaticProperties} - The header cache properties.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SmalStaticProperties extends AbstractProtocolProperties {

    private static final SmalStaticProperties instance = new SmalStaticProperties();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static SmalStaticProperties getInstance() {
        return instance;
    }

    /*-
     * ---------------------------- Member section ----------------------------
     */

    private boolean enabled;

    private boolean externalOnly;

    private int defaultRefreshRate;

    /**
     * Initializes a new {@link SmalStaticProperties}.
     */
    private SmalStaticProperties() {
        super();
    }

    @Override
    protected void loadProperties0() throws OXException {
        final org.apache.commons.logging.Log logger = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(SmalStaticProperties.class));

        final StringBuilder logBuilder = new StringBuilder(256);
        logBuilder.append("\nLoading global SMAL properties...\n");

        final ConfigurationService configuration = SmalServiceLookup.getInstance().getService(ConfigurationService.class);
        {
            final String enabledStr = configuration.getProperty("com.openexchange.mail.headercache.enable", STR_TRUE).trim();
            enabled = Boolean.parseBoolean(enabledStr);
            logBuilder.append("\tEnabled: ").append(enabled).append('\n');
        }

        {
            final String extOnlyStr = configuration.getProperty("com.openexchange.mail.headercache.externalOnly", STR_TRUE).trim();
            externalOnly = Boolean.parseBoolean(extOnlyStr);
            logBuilder.append("\tExternal-only: ").append(externalOnly).append('\n');
        }

        {
            final String drrStr = configuration.getProperty("com.openexchange.mail.headercache.defaultRefreshRate", "60000").trim();
            try {
                defaultRefreshRate = Integer.parseInt(drrStr);
                logBuilder.append("\tDefault refresh rate: ").append(defaultRefreshRate).append('\n');
            } catch (final NumberFormatException e) {
                defaultRefreshRate = 60000;
                logBuilder.append("\tDefault refresh rate: Invalid value \"").append(drrStr).append("\". Setting to fallback: ").append(
                    defaultRefreshRate).append('\n');
            }
        }

        logBuilder.append("Global SMAL properties successfully loaded!");
        if (logger.isInfoEnabled()) {
            logger.info(logBuilder.toString());
        }
    }

    @Override
    protected void resetFields() {
        enabled = false;
        externalOnly = false;
        defaultRefreshRate = 0;
    }

    /**
     * Gets the enabled flag.
     *
     * @return The enabled flag
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the external-only flag.
     *
     * @return The external-only flag
     */
    public boolean isExternalOnly() {
        return externalOnly;
    }

    /**
     * Gets the default refresh rate.
     *
     * @return The default refresh rate
     */
    public int getDefaultRefreshRate() {
        return defaultRefreshRate;
    }

}

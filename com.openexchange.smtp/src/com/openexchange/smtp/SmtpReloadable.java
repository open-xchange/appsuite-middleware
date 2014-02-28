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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.smtp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.smtp.config.SMTPProperties;

/**
 * {@link SmtpReloadable} - Collects reloadables for SMTP module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public final class SmtpReloadable implements Reloadable {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SmtpReloadable.class);

    private static final SmtpReloadable INSTANCE = new SmtpReloadable();

    private static final String CONFIGFILE = "smtp.properties";

    private static final String[] PROPERTIES = new String[] {"all properties in file"};

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static SmtpReloadable getInstance() {
        return INSTANCE;
    }

    // --------------------------------------------------------------------------------------------------- //

    private final List<Reloadable> reloadables;

    /**
     * Initializes a new {@link SmtpReloadable}.
     */
    private SmtpReloadable() {
        super();
        reloadables = new CopyOnWriteArrayList<Reloadable>();
    }

    /**
     * Adds given {@link Reloadable} instance.
     *
     * @param reloadable The instance to add
     */
    public void addReloadable(final Reloadable reloadable) {
        reloadables.add(reloadable);
    }

    @Override
    public void reloadConfiguration(final ConfigurationService configService) {
        try {
            final SMTPProperties smtpProperties = SMTPProperties.getInstance();
            if (null != smtpProperties) {
                smtpProperties.resetProperties();
                smtpProperties.loadProperties();
            }
        } catch (final OXException e) {
            LOGGER.warn("Failed to reload SMTP properties", e);
        }

        for (final Reloadable reloadable : reloadables) {
            reloadable.reloadConfiguration(configService);
        }
    }

    @Override
    public Map<String, String[]> getConfigFileNames() {
        Map<String, String[]> map = new HashMap<String, String[]>(1);
        map.put(CONFIGFILE, PROPERTIES);
        return map;
    }

}

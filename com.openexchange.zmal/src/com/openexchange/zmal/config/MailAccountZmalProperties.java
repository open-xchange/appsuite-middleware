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

package com.openexchange.zmal.config;

import com.openexchange.mail.config.MailAccountProperties;
import com.openexchange.mailaccount.MailAccount;

/**
 * {@link MailAccountZmalProperties} - ZMAL properties read from mail account with fallback to properties read from properties file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountZmalProperties extends MailAccountProperties implements IZmalProperties {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MailAccountZmalProperties.class));

    /**
     * Initializes a new {@link MailAccountZmalProperties}.
     *
     * @param mailAccount The mail account
     * @throws IllegalArgumentException If provided mail account is <code>null</code>
     */
    public MailAccountZmalProperties(final MailAccount mailAccount) {
        super(mailAccount);
    }

    @Override
    public int getZmalConnectionTimeout() {
        final String tmp = properties.get("com.openexchange.zmal.zmalConnectionTimeout");
        if (null == tmp) {
            return ZmalProperties.getInstance().getZmalConnectionTimeout();
        }

        try {
            return Integer.parseInt(tmp.trim());
        } catch (final NumberFormatException e) {
            LOG.error("ZMAL Connection Timeout: Invalid value.", e);
            return ZmalProperties.getInstance().getZmalConnectionTimeout();
        }
    }

    @Override
    public int getZmalTimeout() {
        final String tmp = properties.get("com.openexchange.zmal.zmalTimeout");
        if (null == tmp) {
            return ZmalProperties.getInstance().getZmalTimeout();
        }

        try {
            return Integer.parseInt(tmp.trim());
        } catch (final NumberFormatException e) {
            LOG.error("ZMAL Timeout: Invalid value.", e);
            return ZmalProperties.getInstance().getZmalTimeout();
        }
    }

}

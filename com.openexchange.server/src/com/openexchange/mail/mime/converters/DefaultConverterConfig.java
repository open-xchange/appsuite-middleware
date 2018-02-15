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

package com.openexchange.mail.mime.converters;

import com.openexchange.mail.api.MailConfig;

/**
 * {@link DefaultConverterConfig} Default implementation of {@link ConverterConfig}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class DefaultConverterConfig implements ConverterConfig {

    private final boolean considerFolder;
    private final MailConfig mailConfig;
    private final boolean includeBody;

    /**
     * Initializes a new {@link DefaultConverterConfig}.
     *
     * @param mailConfig The mail config
     */
    public DefaultConverterConfig(MailConfig mailConfig) {
        this(mailConfig, true, false);
    }

    /**
     * Initializes a new {@link DefaultConverterConfig}.
     *
     * @param mailConfig The mail config
     * @param considerFolder Whether the folder is supposed to be considered during conversion
     * @param includeBody Whether the body should be included during conversion
     */
    public DefaultConverterConfig(MailConfig mailConfig, boolean considerFolder, boolean includeBody) {
        super();
        this.considerFolder = considerFolder;
        this.mailConfig = mailConfig;
        this.includeBody = includeBody;
    }

    @Override
    public boolean isConsiderFolder() {
        return considerFolder;
    }

    @Override
    public MailConfig getMailConfig() {
        return mailConfig;
    }

    @Override
    public boolean isIncludeBody() {
        return includeBody;
    }
}

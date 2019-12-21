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

package com.openexchange.push.dovecot;

import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;

/**
 * {@link DovecotPushConfiguration}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.4
 */
public class DovecotPushConfiguration {

    private final ConfigurationService configService;

    public DovecotPushConfiguration(ConfigurationService configService) {
        super();
        this.configService = configService;
    }

    public String getClusterLockMech() {
        return configService.getProperty("com.openexchange.push.dovecot.clusterLock", "hz").trim();
    }

    /**
     * Whether to use stateless implementation.
     *
     * @param services The service look-up to obtain required services from
     * @return <code>true</code> for stateless implementation; otherwise <code>false</code>
     * @throws OXException If property cannot be checked
     */
    public boolean useStatelessImpl() throws OXException {
        return configService.getBoolProperty("com.openexchange.push.dovecot.stateless", true);
    }

    /**
     * Checks whether to prefer Doveadm to issue METADATA commands.
     *
     * @param optionalServices The optional service look-up
     * @return <code>true</code> to prefer Doveadm; otherwise <code>false</code>
     */
    public boolean preferDoveadmForMetadata() {
        return configService.getBoolProperty("com.openexchange.push.dovecot.preferDoveadmForMetadata", false);
    }

}

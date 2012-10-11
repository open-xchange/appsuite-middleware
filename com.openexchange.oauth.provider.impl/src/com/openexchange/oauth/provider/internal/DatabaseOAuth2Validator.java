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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.internal;

import net.oauth.v2.OAuth2Validator;
import net.oauth.v2.SimpleOAuth2Validator;
import org.apache.commons.logging.Log;

/**
 * {@link DatabaseOAuth2Validator} - A simple {@link OAuth2Validator}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DatabaseOAuth2Validator extends SimpleOAuth2Validator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(DatabaseOAuth2Validator.class);

    /**
     * Initializes a new {@link DatabaseOAuth2Validator} that rejects messages more than five minutes old or with a OAuth version other than
     * <code>1.0</code>.
     */
    public DatabaseOAuth2Validator() {
        super();
    }

    /**
     * Initializes a new {@link DatabaseOAuth2Validator}.
     * 
     * @param maxTimestampAgeMsec The range of valid time stamps, in milliseconds into the past or future. So the total range of valid time
     *            stamps is twice this value, rounded to the nearest second.
     * @param maxVersion The maximum valid oauth_version
     */
    public DatabaseOAuth2Validator(final long maxTimestampAgeMsec, final double maxVersion) {
        super(maxTimestampAgeMsec, maxVersion);
    }

}

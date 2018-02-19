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

package com.openexchange.mail.authenticity.impl.core;

import java.util.Map;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlobEntry;
import com.openexchange.jslob.JSlobKeys;
import com.openexchange.mail.authenticity.MailAuthenticityProperty;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link MailAuthenticityJSlobEntry}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailAuthenticityJSlobEntry implements JSlobEntry {

    private static final String NAME = "features/authenticity";
    private final ServiceLookup services;

    /**
     * Initialises a new {@link MailAuthenticityJSlobEntry}.
     */
    public MailAuthenticityJSlobEntry(ServiceLookup services) {
        super();
        this.services = services;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.jslob.JSlobEntry#getKey()
     */
    @Override
    public String getKey() {
        return JSlobKeys.MAIL;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.jslob.JSlobEntry#getPath()
     */
    @Override
    public String getPath() {
        return NAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.jslob.JSlobEntry#isWritable(com.openexchange.session.Session)
     */
    @Override
    public boolean isWritable(Session session) throws OXException {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.jslob.JSlobEntry#getValue(com.openexchange.session.Session)
     */
    @Override
    public Object getValue(Session session) throws OXException {
        LeanConfigurationService configService = services.getService(LeanConfigurationService.class);
        return configService.getBooleanProperty(session.getUserId(), session.getContextId(), MailAuthenticityProperty.ENABLED);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.jslob.JSlobEntry#setValue(java.lang.Object, com.openexchange.session.Session)
     */
    @Override
    public void setValue(Object value, Session session) throws OXException {
        // not writable
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.jslob.JSlobEntry#metadata(com.openexchange.session.Session)
     */
    @Override
    public Map<String, Object> metadata(Session session) throws OXException {
        // nope
        return null;
    }
}

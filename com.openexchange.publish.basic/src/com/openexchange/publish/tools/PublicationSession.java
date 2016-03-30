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

package com.openexchange.publish.tools;

import java.util.HashSet;
import java.util.Set;
import com.openexchange.publish.Publication;
import com.openexchange.session.Session;

/**
 * {@link PublicationSession}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
/**
 * {@link PublicationSession}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class PublicationSession implements Session {

    private final Publication publication;

    private String localIp;

    /**
     * Initializes a new {@link PublicationSession}.
     *
     * @param publication
     */
    public PublicationSession(final Publication publication) {
        super();
        this.publication = publication;
    }

    @Override
    public int getContextId() {
        return publication.getContext().getContextId();
    }

    @Override
    public String getLocalIp() {
        return localIp;
    }

    @Override
    public String getLogin() {
        return null;
    }

    @Override
    public String getLoginName() {
        return null;
    }

    @Override
    public boolean containsParameter(final String name) {
        return PARAM_PUBLICATION.equals(name);
    }

    @Override
    public Object getParameter(final String name) {
        if (PARAM_PUBLICATION.equals(name)) {
            return Boolean.TRUE;
        }
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getRandomToken() {
        return null;
    }

    @Override
    public String getSecret() {
        return null;
    }

    @Override
    public String getSessionID() {
        return null;
    }

    @Override
    public int getUserId() {
        return publication.getUserId();
    }

    @Override
    public String getUserlogin() {
        return null;
    }

    @Override
    public void setParameter(final String name, final Object value) {
        // Nothing to remember.
    }

    @Override
    public String getAuthId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHash() {
        // Nothing to do
        return null;
    }

    @Override
    public void setLocalIp(final String ip) {
        this.localIp = ip;
    }

    @Override
    public void setHash(final String hash) {
        // Nothing to do
    }

    @Override
    public String getClient() {
        return null;
    }

    @Override
    public void setClient(final String client) {
        // Nothing to do.
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public Set<String> getParameterNames() {
        Set<String> retval = new HashSet<String>();
        retval.add(PARAM_PUBLICATION);
        return retval;
    }
}

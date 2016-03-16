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

package com.openexchange.subscribe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.groupware.generic.TargetFolderDefinition;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;


/**
 * {@link TargetFolderSession} - A {@link Session} based on a passed {@link TargetFolderDefinition} instance.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class TargetFolderSession implements Session {

    private final int contextId;
    private final int userId;
    private final Map<String, Object> params;
    private final Session session;

    public TargetFolderSession(final TargetFolderDefinition target) {
        super();
        contextId = target.getContext().getContextId();
        userId = target.getUserId();
        // Initialize
        final SessiondService service = SessiondService.SERVICE_REFERENCE.get();
        Session ses = null;
        if (null != service && null != (ses = service.getAnyActiveSessionForUser(target.getUserId(), target.getContext().getContextId()))) {
            session = ses;
            params = null;
        } else {
            session = null;
            params = new HashMap<String, Object>(8);
        }
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getLocalIp() {
        if (null == session) {
            throw new UnsupportedOperationException("TargetFolderSession.getLocalIp()");
        }
        return session.getLocalIp();
    }

    @Override
    public void setLocalIp(final String ip) {
        if (null == session) {
            throw new UnsupportedOperationException("TargetFolderSession.setLocalIp()");
        }
        session.setLocalIp(ip);
    }

    @Override
    public String getLoginName() {
        if (null == session) {
            throw new UnsupportedOperationException("TargetFolderSession.getLoginName()");
        }
        return session.getLoginName();
    }

    @Override
    public boolean containsParameter(final String name) {
        if (null != params) {
            return params.containsKey(name);
        }
        return session.containsParameter(name);
    }

    @Override
    public Object getParameter(final String name) {
        if (null != params) {
            return params.get(name);
        }
        return session.getParameter(name);
    }

    @Override
    public String getPassword() {
        if (null == session) {
            throw new UnsupportedOperationException("TargetFolderSession.getPassword()");
        }
        return session.getPassword();
    }

    @Override
    public String getRandomToken() {
        if (null == session) {
            throw new UnsupportedOperationException("TargetFolderSession.getRandomToken()");
        }
        return session.getRandomToken();
    }

    @Override
    public String getSecret() {
        if (null == session) {
            throw new UnsupportedOperationException("TargetFolderSession.getSecret()");
        }
        return session.getSecret();
    }

    @Override
    public String getSessionID() {
        if (null == session) {
            throw new UnsupportedOperationException("TargetFolderSession.getSessionID()");
        }
        return session.getSessionID();
    }

    @Override
    public String getUserlogin() {
        if (null == session) {
            throw new UnsupportedOperationException("TargetFolderSession.getUserlogin()");
        }
        return session.getUserlogin();
    }

    @Override
    public String getLogin() {
        if (null == session) {
            throw new UnsupportedOperationException("TargetFolderSession.getLogin()");
        }
        return session.getLogin();
    }

    @Override
    public void setParameter(final String name, final Object value) {
        if (null != params) {
            if (null == value) {
                params.remove(name);
            } else {
                params.put(name, value);
            }
        } else {
            session.setParameter(name, value);
        }
    }

    @Override
    public String getAuthId() {
        if (null == session) {
            throw new UnsupportedOperationException("TargetFolderSession.getAuthId()");
        }
        return session.getAuthId();
    }

    @Override
    public String getHash() {
        if (null == session) {
            throw new UnsupportedOperationException("TargetFolderSession.getHash()");
        }
        return session.getHash();
    }

    @Override
    public void setHash(final String hash) {
        if (null != session) {
            session.setHash(hash);
        }
    }

    @Override
    public String getClient() {
        if (null == session) {
            throw new UnsupportedOperationException("TargetFolderSession.getClient()");
        }
        return session.getClient();
    }

    @Override
    public void setClient(final String client) {
        if (null != session) {
            session.setClient(client);
        }
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public Set<String> getParameterNames() {
        Set<String> retval = new HashSet<String>();
        if (null != params) {
            retval.addAll(params.keySet());
        }
        retval.addAll(session.getParameterNames());
        return retval;
    }
}

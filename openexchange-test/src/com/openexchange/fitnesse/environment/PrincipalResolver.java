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

package com.openexchange.fitnesse.environment;

import java.io.IOException;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.group.GroupResolver;
import com.openexchange.ajax.user.UserResolver;
import com.openexchange.fitnesse.exceptions.FitnesseException;
import com.openexchange.group.Group;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.User;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;

/**
 * {@link PrincipalResolver}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PrincipalResolver {

    private static final String EXTERNAL = "external:";

    private static final String USER = "user:";

    private static final String GROUP = "group:";

    private AJAXClient client;

    private UserResolver userResolver;

    private GroupResolver groupResolver;

    public PrincipalResolver(AJAXClient client) {
        this.client = client;
        this.userResolver = new UserResolver(client);
        this.groupResolver = new GroupResolver(client);
    }

    public Participant resolveEntity(String entity) throws FitnesseException {
        if (entity.equals("myself")) {
            try {
                return new UserParticipant(client.getValues().getUserId());
            } catch (AjaxException e) {
                throw new FitnesseException(e);
            } catch (IOException e) {
                throw new FitnesseException(e);
            } catch (SAXException e) {
                throw new FitnesseException(e);
            } catch (JSONException e) {
                throw new FitnesseException(e);
            }
        }
        if (entity.startsWith(GROUP)) {
            return notNull(resolveGroup(entity.substring(GROUP.length())), entity);
        } else if (entity.startsWith(USER)) {
            return notNull(resolveUser(entity.substring(USER.length())), entity);
        } else if (entity.startsWith(EXTERNAL)) {
            String email = entity.substring(EXTERNAL.length());
            return new ExternalUserParticipant(email);
        } else {
            UserParticipant participant = resolveUser(entity);
            if (participant == null) {
                GroupParticipant group = resolveGroup(entity);
                if (group != null) {
                    return group;
                } else {
                    return new ExternalUserParticipant(entity);
                }
            } else {
                return notNull(participant, entity);
            }
        }
    }

    /**
     * @param resolveGroup
     * @return
     * @throws FitnesseException
     */
    private Participant notNull(Participant participant, String entity) throws FitnesseException {
        if (participant == null) {
            throw new FitnesseException("Could not resolve: " + entity);
        }
        return participant;
    }

    /**
     * @param substring
     * @param permission
     * @return
     */
    private UserParticipant resolveUser(String searchPattern) throws FitnesseException {
        try {
            User[] users = userResolver.resolveUser("*" + searchPattern + "*");
            if (users.length == 0) {
                return null;
            }
            return new UserParticipant(users[0].getId());
        } catch (AjaxException e) {
            throw new FitnesseException(e);
        } catch (IOException e) {
            throw new FitnesseException(e);
        } catch (SAXException e) {
            throw new FitnesseException(e);
        } catch (JSONException e) {
            throw new FitnesseException(e);
        }
    }

    /**
     * @param substring
     * @param permission
     * @return
     * @throws FitnesseException
     */
    private GroupParticipant resolveGroup(String searchPattern) throws FitnesseException {
        try {
            Group[] groups = groupResolver.resolveGroup("*" + searchPattern + "*");
            if (groups.length == 0) {
                return null;
            }
            return new GroupParticipant(groups[0].getIdentifier());
        } catch (AjaxException e) {
            throw new FitnesseException(e);
        } catch (OXJSONException e) {
            throw new FitnesseException(e);
        } catch (IOException e) {
            throw new FitnesseException(e);
        } catch (SAXException e) {
            throw new FitnesseException(e);
        } catch (JSONException e) {
            throw new FitnesseException(e);
        }
    }
}

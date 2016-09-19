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

package com.openexchange.oauth.linkedin;

import java.util.List;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link LinkedInService}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public interface LinkedInService {

    public List<Contact> getContacts(Session session, int user, int contextId, int accountId) throws OXException;

    public String getAccountDisplayName(Session session, int user, int contextId, int accountId);

    /**
     * @return all data on a contact identified by e-mail (special feature, only available with extended API keys)
     */
	public JSONObject getFullProfileByEMail(List<String> email, Session session, int user, int contextId, int accountId) throws OXException;
    public JSONObject getFullProfileByFirstAndLastName(String firstName, String lastName, ServerSession session, int uid, int cid, int id) throws OXException;

    /**
     * @return all data on a contact identified by id
     */
	public JSONObject getProfileForId(String id, Session session, int user, int contextId, int accountId) throws OXException;

	/**
	 * @return all data of all connections a user has
	 */
	public JSONObject getConnections(Session session, int user, int contextId,	int accountId) throws OXException;

	/**
	 * @return the IDs of all connections a user has (so you can query them separately)
	 */
	public List<String> getUsersConnectionsIds(Session session, int user, int contextId, int accountId) throws OXException;

	/**
	 * @return A list of contacts that list the targeted user to the current user
	 */
	public JSONObject getRelationToViewer(String id, Session session, int user, int contextId, int accountId) throws OXException;

	/**
	 * @return A chronologically sorted list of all events that happened in a users network
	 */
	public JSONObject getNetworkUpdates(Session session, int user, int contextId, int accountId) throws OXException;

	/**
	 * @return The messages in the user's inbox
	 */
	public JSONObject getMessageInbox(Session session, int i, int j, int k) throws OXException;

}

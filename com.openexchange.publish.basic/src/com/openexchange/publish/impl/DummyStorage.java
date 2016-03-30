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

package com.openexchange.publish.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.publish.Entity;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationStorage;


/**
 * {@link DummyStorage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class DummyStorage implements PublicationStorage {

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationStorage#forgetPublication(com.openexchange.publish.Publication)
     */
    @Override
    public void forgetPublication(Publication publication) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationStorage#getPublication(com.openexchange.groupware.contexts.Context, int)
     */
    @Override
    public Publication getPublication(Context ctx, int publicationId) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationStorage#getPublications(com.openexchange.groupware.contexts.Context, java.lang.String, int)
     */
    @Override
    public List<Publication> getPublications(Context ctx, String module, String entityId) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationStorage#getPublications(com.openexchange.groupware.contexts.Context, java.lang.String)
     */
    @Override
    public List<Publication> getPublications(Context ctx, String publicationTarget) throws OXException {
        // Nothing to do
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationStorage#rememberPublication(com.openexchange.publish.Publication)
     */
    @Override
    public void rememberPublication(Publication publication) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationStorage#search(com.openexchange.groupware.contexts.Context, java.lang.String, java.util.Map)
     */
    @Override
    public Collection<Publication> search(Context ctx, String targetId, Map<String, Object> query) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public void updatePublication(Publication publication) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationStorage#deletePublicationsOfUser(int, com.openexchange.groupware.contexts.Context)
     */
    @Override
    public void deletePublicationsOfUser(int userID, Context context) throws OXException {
        // Nothing to do

    }

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationStorage#deletePublicationsInContext(int, com.openexchange.groupware.contexts.Context)
     */
    @Override
    public void deletePublicationsInContext(int contextId, Context ctx) {
        // Nothing to do

    }

	@Override
    public List<Publication> getPublicationsOfUser(Context ctx, int userId) throws OXException {
		// Nothing to do
		return null;
	}

	@Override
    public List<Publication> getPublicationsOfUser(Context ctx, int userId,
			String module) throws OXException {
		// Nothing to do
		return null;
	}

	@Override
	public Map<Entity, Boolean> isPublished(List<Entity> entities, Context ctx)
			throws OXException {
		// Nothing to do
		return null;
	}

}

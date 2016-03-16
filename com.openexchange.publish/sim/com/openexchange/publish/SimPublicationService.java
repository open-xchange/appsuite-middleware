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

package com.openexchange.publish;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link SimPublicationService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimPublicationService implements PublicationService {

    private PublicationTarget target;

    private int newId;

    private int updatedId;

    private final Set<Integer> deletedIds = new HashSet<Integer>();

    /*
     * (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#create(com.openexchange.publish.Publication)
     */
    @Override
    public void create(final Publication publication) {
        publication.setId(newId);
    }

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#delete(com.openexchange.groupware.contexts.Context, int)
     */
    @Override
    public void delete(Context ctx, int publicationId) throws OXException {
        deletedIds.add(Integer.valueOf(publicationId));
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#delete(com.openexchange.publish.Publication)
     */
    @Override
    public void delete(final Publication publication) {
        deletedIds.add(Integer.valueOf(publication.getId()));
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#getAllPublications(com.openexchange.groupware.contexts.Context)
     */
    @Override
    public Collection<Publication> getAllPublications(final Context ctx) {
        // Nothing to do
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#getAllPublications(com.openexchange.groupware.contexts.Context, int)
     */
    @Override
    public Collection<Publication> getAllPublications(final Context ctx, final String entityId) {
        // Nothing to do
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#getTarget()
     */
    @Override
    public PublicationTarget getTarget() {
        return target;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#knows(com.openexchange.groupware.contexts.Context, int)
     */
    @Override
    public boolean knows(final Context ctx, final int publicationId) {
        // Nothing to do
        return false;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#load(com.openexchange.groupware.contexts.Context, int)
     */
    @Override
    public Publication load(final Context ctx, final int publicationId) {
        // Nothing to do
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#update(com.openexchange.publish.Publication)
     */
    @Override
    public void update(final Publication publication) {
        this.updatedId = publication.getId();
    }

    public void setTarget(final PublicationTarget publicationTarget) {
        this.target = publicationTarget;
    }

    public void setNewId(final int i) {
        this.newId = i;
    }

    public int getUpdatedId() {
        return updatedId;
    }

    public Set<Integer> getDeletedIDs() {
        return deletedIds;
    }

	@Override
    public Collection<Publication> getAllPublications(final Context ctx, final int userId, final String module) throws OXException {
		// Nothing to do
		return null;
	}

    @Override
    public Publication resolveUrl(Context ctx, String URL) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getInformation(Publication publication) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isCreateModifyEnabled() {
        return true;
    }

}

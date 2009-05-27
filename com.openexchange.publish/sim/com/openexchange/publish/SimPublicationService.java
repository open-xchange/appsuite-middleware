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

package com.openexchange.publish;

import java.util.Collection;
import com.openexchange.groupware.contexts.Context;


/**
 * {@link SimPublicationService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SimPublicationService implements PublicationService {

    private PublicationTarget target;

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#create(com.openexchange.publish.Publication)
     */
    public void create(Publication publication) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#delete(com.openexchange.publish.Publication)
     */
    public void delete(Publication publication) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#getAllPublications(com.openexchange.groupware.contexts.Context)
     */
    public Collection<Publication> getAllPublications(Context ctx) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#getAllPublications(com.openexchange.groupware.contexts.Context, int)
     */
    public Collection<Publication> getAllPublications(Context ctx, String entityId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#getTarget()
     */
    public PublicationTarget getTarget() {
        return target;
    }

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#knows(com.openexchange.groupware.contexts.Context, int)
     */
    public boolean knows(Context ctx, int publicationId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#load(com.openexchange.groupware.contexts.Context, int)
     */
    public Publication load(Context ctx, int publicationId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.publish.PublicationService#update(com.openexchange.publish.Publication)
     */
    public void update(Publication publication) {
        // TODO Auto-generated method stub

    }

    public void setTarget(PublicationTarget publicationTarget) {
        this.target = publicationTarget;
    }

}

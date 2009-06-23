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

package com.openexchange.publish.helpers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationErrorMessage;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.PublicationStorage;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.impl.DummyStorage;


/**
 * {@link AbstractPublicationService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public abstract class AbstractPublicationService implements PublicationService {

    public static enum Permission {
        CREATE, DELETE, UPDATE;
    }
    
    public static SecurityStrategy ALLOW_ALL = new AllowEverything();
    public static SecurityStrategy FOLDER_ADMIN_ONLY = new AllowEverything(); // Must be overwritten by activator
    
    
    public static PublicationStorage STORAGE = new DummyStorage();
    
    public void create(Publication publication)  throws PublicationException{
        checkPermission(Permission.CREATE, publication);
        modifyIncoming(publication);
        beforeCreate(publication);
        STORAGE.rememberPublication(publication);
        afterCreate(publication);
        modifyOutgoing(publication);
    }

    public void delete(Publication publication)  throws PublicationException{
        checkPermission(Permission.DELETE, publication);
        beforeDelete(publication);
        STORAGE.forgetPublication(publication);
        afterDelete(publication);
    }

    public Collection<Publication> getAllPublications(Context ctx) throws PublicationException {
        List<Publication> publications = STORAGE.getPublications(ctx, getTarget().getId());
        for(Publication publication : publications) {
            modifyOutgoing(publication);
        }
        afterLoad(publications);
        return publications;
    }

    public Collection<Publication> getAllPublications(Context ctx, String entityId) throws PublicationException {
        List<Publication> publications = STORAGE.getPublications(ctx, getTarget().getModule(), entityId);
        for(Publication publication : publications) {
            modifyOutgoing(publication);
        }
        afterLoad(publications);
        return publications;
    }
    
    

    public boolean knows(Context ctx, int publicationId) throws PublicationException {
        return load(ctx, publicationId) != null;
    }

    public Publication load(Context ctx, int publicationId) throws PublicationException {
        Publication publication = STORAGE.getPublication(ctx, publicationId);
        if(publication.getTarget().getId().equals(getTarget().getId())) {
            modifyOutgoing(publication);
            return publication;
        }
        return null;
    }

    public void update(Publication publication) throws PublicationException {
        checkPermission(Permission.UPDATE, publication);
        modifyIncoming(publication);
        beforeUpdate(publication);
        STORAGE.updatePublication(publication);
        afterUpdate(publication);
    }
    
    public PublicationStorage getStorage() {
        return STORAGE;
    }
    
    // Callbacks for subclasses
    
    public abstract PublicationTarget getTarget() throws PublicationException;

    
    public void modifyIncoming(Publication publication) throws PublicationException{
        
    }
    
    public void modifyOutgoing(Publication publication) throws PublicationException{
        
    }
    
    public void beforeCreate(Publication publication) throws PublicationException {
        
    }
    
    public void afterCreate(Publication publication) throws PublicationException{
        
    }
    
    public void beforeUpdate(Publication publication) throws PublicationException{
        
    }
    
    public void afterUpdate(Publication publication) throws PublicationException{
        
    }
    
    public void beforeDelete(Publication publication) throws PublicationException{
        
    }
    
    public void afterDelete(Publication publication) throws PublicationException{
        
    }
    
    public void afterLoad(Collection<Publication> publications) throws PublicationException{
        
    }
    
    public PublicationException uniquenessConstraintViolation(String key, String value) {
        return PublicationErrorMessage.UniquenessConstraintViolation.create(value, key);
    }
    
    public void checkPermission(Permission permission, Publication publication) throws PublicationException {
        boolean allow = false;
        try {
            switch(permission) {
            case CREATE : allow = mayCreate(publication); break;
            case UPDATE : allow = mayUpdate(publication); break;
            case DELETE : allow = mayDelete(publication); break;
            }
        } catch (PublicationException x) {
            throw x;
        } catch (AbstractOXException x) {
            throw new PublicationException(x);
        }
        if(! allow) {
            throw PublicationErrorMessage.AccessDenied.create(permission);
        }
    }

    protected boolean mayDelete(Publication publication) throws AbstractOXException {
        return getSecurityStrategy().mayDelete(publication);
    }

    protected boolean mayUpdate(Publication publication) throws AbstractOXException{
        return getSecurityStrategy().mayUpdate(publication);
    }

    protected boolean mayCreate(Publication publication) throws AbstractOXException{
        return getSecurityStrategy().mayCreate(publication);
    }
    
    protected abstract SecurityStrategy getSecurityStrategy();

}

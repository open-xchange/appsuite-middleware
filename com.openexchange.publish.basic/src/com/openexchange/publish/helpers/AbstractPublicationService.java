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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.log.LogFactory;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationErrorMessage;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.PublicationStorage;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.impl.DummyStorage;

/**
 * {@link AbstractPublicationService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractPublicationService implements PublicationService {

    public static enum Permission {
        CREATE, DELETE, UPDATE;
    }

    private static Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AbstractPublicationService.class));

    public static SecurityStrategy ALLOW_ALL = new AllowEverything();

    public static SecurityStrategy FOLDER_ADMIN_ONLY = new AllowEverything(); // Must be overwritten by activator

    private static PublicationStorage STORAGE = new DummyStorage(); // Must be overwritten by activator

    public static void setDefaultStorage(final PublicationStorage storage) {
        STORAGE = storage;
    }

    public static PublicationStorage getDefaultStorage() {
        return STORAGE;
    }

    @Override
    public void create(final Publication publication) throws OXException {
        checkPermission(Permission.CREATE, publication);
        modifyIncoming(publication);
        beforeCreate(publication);
        STORAGE.rememberPublication(publication);
        afterCreate(publication);
        modifyOutgoing(publication);
    }

    @Override
    public void delete(final Publication publication) throws OXException {
        checkPermission(Permission.DELETE, publicationForPermissionCheck(publication));
        beforeDelete(publication);
        STORAGE.forgetPublication(publication);
        afterDelete(publication);
    }

    @Override
    public Collection<Publication> getAllPublications(final Context ctx) throws OXException {
        final List<Publication> publications = STORAGE.getPublications(ctx, getTarget().getId());
        List<Publication> returnPublications = new ArrayList<Publication>();
        for (final Publication publication : publications) {
            /* as some publications are not working anymore, we should at least filter out the not working ones and write them to LOG */
            try {
                modifyOutgoing(publication);
                returnPublications.add(publication);
            } catch (OXException e) {
                if (InfostoreExceptionCodes.NOT_EXIST.equals(e)){
                    LOG.info(e.getLogMessage());
                } else {
                    throw e;
                }
            }
        }
        afterLoad(returnPublications);
        return returnPublications;
    }

    @Override
    public Collection<Publication> getAllPublications(final Context ctx, final String entityId) throws OXException {
        final List<Publication> publications = STORAGE.getPublications(ctx, getTarget().getModule(), entityId);
        List<Publication> returnPublications = new ArrayList<Publication>();
        for (final Publication publication : publications) {
            /* as some publications are not working anymore, we should at least filter out the not working ones and write them to LOG */
            try {
                modifyOutgoing(publication);
                returnPublications.add(publication);
            } catch (OXException e) {
                if (InfostoreExceptionCodes.NOT_EXIST.equals(e)){
                    LOG.info(e.getLogMessage());
                } else {
                    throw e;
                }
            }
        }
        afterLoad(returnPublications);
        return returnPublications;
    }

    @Override
    public Collection<Publication> getAllPublications(final Context ctx, final int userId, final String module) throws OXException {
    	List<Publication> publications;
    	List<Publication> returnPublications = new ArrayList<Publication>();
    	if (module == null) {
    		publications = STORAGE.getPublicationsOfUser(ctx, userId);
    	} else {
    		publications = STORAGE.getPublicationsOfUser(ctx, userId, module);
    	}

    	for (final Publication publication : publications) {
    	    /* as some publications are not working anymore, we should at least filter out the not working ones and write them to LOG */
            try {
                modifyOutgoing(publication);
                returnPublications.add(publication);
            } catch (OXException e) {
                if (InfostoreExceptionCodes.NOT_EXIST.equals(e)){
                    LOG.info(e.getLogMessage());
                } else {
                    throw e;
                }
            }
        }
        afterLoad(returnPublications);
        return returnPublications;
    }

    @Override
    public boolean knows(final Context ctx, final int publicationId) throws OXException {
        return load(ctx, publicationId) != null;
    }

    @Override
    public Publication load(final Context ctx, final int publicationId) throws OXException {
        final Publication publication = loadInternally(ctx, publicationId);
        if(publication != null) {
            modifyOutgoing(publication);
        }

        return publication;
    }

    protected Publication loadInternally(final Context ctx, final int publicationId) throws OXException {
        final Publication publication = STORAGE.getPublication(ctx, publicationId);
        if (null != publication && publication.getTarget() != null && publication.getTarget().getId().equals(getTarget().getId())) {
            return publication;
        }
        return null;
    }

    @Override
    public void update(final Publication publication) throws OXException {
        checkPermission(Permission.UPDATE, publicationForPermissionCheck(publication));
        modifyIncoming(publication);
        beforeUpdate(publication);
        STORAGE.updatePublication(publication);
        afterUpdate(publication);
    }

    private Publication publicationForPermissionCheck(final Publication publication) throws OXException {
        final Publication loaded = load(publication.getContext(), publication.getId());
        loaded.setUserId(publication.getUserId());
        if (null != publication.getEntityId()) {
            loaded.setEntityId(publication.getEntityId());
        }
        return loaded;
    }

    public PublicationStorage getStorage() {
        return STORAGE;
    }

    // Callbacks for subclasses

    @Override
    public abstract PublicationTarget getTarget() throws OXException;

    public void modifyIncoming(final Publication publication) throws OXException {
        // Empty method
    }

    public void modifyOutgoing(final Publication publication) throws OXException {
        // Empty method
    }

    public void beforeCreate(final Publication publication) throws OXException {
        // Empty method
    }

    public void afterCreate(final Publication publication) throws OXException {
        // Empty method
    }

    public void beforeUpdate(final Publication publication) throws OXException {
        // Empty method
    }

    public void afterUpdate(final Publication publication) throws OXException {
        // Empty method
    }

    public void beforeDelete(final Publication publication) throws OXException {
        // Empty method
    }

    public void afterDelete(final Publication publication) throws OXException {
        // Empty method
    }

    public void afterLoad(final Collection<Publication> publications) throws OXException {
        // Empty method
    }

    public OXException uniquenessConstraintViolation(final String key, final String value) {
        return PublicationErrorMessage.UniquenessConstraintViolation.create(value, key);
    }

    public void checkPermission(final Permission permission, final Publication publication) throws OXException {
        boolean allow = false;
        try {
            switch (permission) {
            case CREATE:
                allow = mayCreate(publication);
                break;
            case UPDATE:
                allow = mayUpdate(publication);
                break;
            case DELETE:
                allow = mayDelete(publication);
                break;
            }
        } catch (final OXException x) {
            throw x;
        }
        if (!allow) {
            throw PublicationErrorMessage.AccessDenied.create(permission);
        }
    }

    protected boolean mayDelete(final Publication publication) throws OXException {
        return getSecurityStrategy().mayDelete(publication);
    }

    protected boolean mayUpdate(final Publication publication) throws OXException {
        return getSecurityStrategy().mayUpdate(publication);
    }

    protected boolean mayCreate(final Publication publication) throws OXException {
        return getSecurityStrategy().mayCreate(publication);
    }

    protected abstract SecurityStrategy getSecurityStrategy();

}

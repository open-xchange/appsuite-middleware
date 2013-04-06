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

package com.openexchange.admin.rmi.impl;

import java.rmi.RemoteException;
import org.apache.commons.logging.Log;
import com.openexchange.admin.rmi.OXPublicationInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Publication;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.MissingServiceException;
import com.openexchange.admin.rmi.exceptions.NoSuchPublicationException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;

/**
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 */
public class OXPublication extends OXCommonImpl implements OXPublicationInterface {

    private final static Log log = LogFactory.getLog(OXPublication.class);

    private final BasicAuthenticator basicauth;

    public OXPublication() throws StorageException {
        super();
        basicauth = new BasicAuthenticator();
        if (log.isInfoEnabled()) {
            log.info("Class loaded: " + this.getClass().getName());
        }
    }
    
    @Override
    public Publication getPublication(Context ctx, String url, Credentials credentials) throws RemoteException, NoSuchPublicationException, MissingServiceException {
        PublicationTargetDiscoveryService discovery = AdminServiceRegistry.getInstance().getService(PublicationTargetDiscoveryService.class);
        if (null == discovery){
            throw new MissingServiceException(PublicationTargetDiscoveryService.class.getSimpleName()+" is missing or not started yet");
        }
        ContextService contexts = AdminServiceRegistry.getInstance().getService(ContextService.class);
        if (null == contexts){
            throw new MissingServiceException(ContextService.class.getSimpleName()+" is missing or not started yet");
        }
        Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            basicauth.doAuthentication(auth, ctx);
            for (PublicationTarget pubTar : discovery.listTargets()) {
                final PublicationService publicationService = pubTar.getPublicationService();
                if (null != publicationService) {
                    com.openexchange.publish.Publication currentPublication = publicationService.resolveUrl(contexts, url);
                    if (null != currentPublication) {
                        String description = publicationService.getInformation(currentPublication);
                        return parsePublication(currentPublication, description);
                    }
                }
            }
        } catch (OXException e) {
            log.error(e.getMessage(), e);
        } catch (InvalidCredentialsException e) {
            throw new RemoteException(e.getMessage());
        } catch (StorageException e) {
            throw new RemoteException(e.getMessage());
        } catch (InvalidDataException e) {
            throw new RemoteException(e.getMessage());
        }
        throw new NoSuchPublicationException("No such publication with URL \"" + url + "\" found");
    }

    @Override
    public boolean deletePublication(Context ctx, String url, Credentials credentials) throws RemoteException, NoSuchPublicationException, MissingServiceException {
        PublicationTargetDiscoveryService discovery = AdminServiceRegistry.getInstance().getService(PublicationTargetDiscoveryService.class);
        if (null == discovery){
            throw new MissingServiceException(PublicationTargetDiscoveryService.class.getSimpleName()+" is missing or not started yet");
        }
        ContextService contexts = AdminServiceRegistry.getInstance().getService(ContextService.class);
        if (null == contexts){
            throw new MissingServiceException(ContextService.class.getSimpleName()+" is missing or not started yet");
        }
        Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            basicauth.doAuthentication(auth, ctx);
            for (PublicationTarget pubTar : discovery.listTargets()) {
                final PublicationService publicationService = pubTar.getPublicationService();
                if (null != publicationService) {
                    com.openexchange.publish.Publication currentPublication = publicationService.resolveUrl(contexts, url);
                    if (null != currentPublication) {
                        publicationService.delete(currentPublication);
                        return true;
                    }
                }
            }
        } catch (OXException e) {
            log.error(e.getMessage(), e);
        } catch (InvalidCredentialsException e) {
            throw new RemoteException(e.getMessage());
        } catch (StorageException e) {
            throw new RemoteException(e.getMessage());
        } catch (InvalidDataException e) {
            throw new RemoteException(e.getMessage());
        }
        throw new NoSuchPublicationException("no Publication with URL " + url + " found");
    }

    private Publication parsePublication(com.openexchange.publish.Publication input, String description) {
        Publication pub = new Publication();
        pub.setContext(new Context(Integer.valueOf(input.getContext().getContextId())));
        pub.setEntityId(input.getEntityId());
        pub.setId(Integer.valueOf(input.getId()));
        pub.setUserId(Integer.valueOf(input.getUserId()));
        pub.setModule(input.getModule());
        pub.setName(input.getDisplayName());
        pub.setDescription(description);
        return pub;
    }

}

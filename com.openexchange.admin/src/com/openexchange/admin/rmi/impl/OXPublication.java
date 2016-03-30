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

package com.openexchange.admin.rmi.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;

/**
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OXPublication extends OXCommonImpl implements OXPublicationInterface {

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXPublication.class);

    private final BasicAuthenticator basicauth;

    public OXPublication() throws StorageException {
        super();
        basicauth = new BasicAuthenticator();
    }

    @Override
    public Publication getPublication(final Context ctx, final String url, Credentials credentials) throws RemoteException, NoSuchPublicationException, MissingServiceException {
        authenticate(ctx, credentials);

        try {
            final com.openexchange.groupware.contexts.Context oxCtx = getOXContext(ctx);
            for (PublicationTarget pubTar : listTargets()) {
                final PublicationService publicationService = pubTar.getPublicationService();

                // Performer
                if (null != publicationService) {
                    com.openexchange.publish.Publication currentPublication = publicationService.resolveUrl(oxCtx, url);
                    if (null != currentPublication) {
                        String description = publicationService.getInformation(currentPublication);
                        return parsePublication(currentPublication, description);
                    }
                }
                // Performer
            }
        } catch (OXException e) {
            log.error("", e);
        }
        throw new NoSuchPublicationException("No such publication with URL \"" + url + "\"");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.OXPublicationInterface#listPublications(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public List<Publication> listPublications(Context ctx, Credentials credentials) throws RemoteException, NoSuchPublicationException, MissingServiceException {
        authenticate(ctx, credentials);

        List<Publication> publications = new ArrayList<Publication>();
        try {
            final com.openexchange.groupware.contexts.Context oxCtx = getOXContext(ctx);
            for (PublicationTarget pubTar : listTargets()) {
                final PublicationService publicationService = pubTar.getPublicationService();

                // Performer
                if (null != publicationService) {
                    Collection<com.openexchange.publish.Publication> allPublications = publicationService.getAllPublications(oxCtx);
                    for (com.openexchange.publish.Publication pub : allPublications) {
                        String description = publicationService.getInformation(pub);
                        publications.add(parsePublication(pub, description));
                    }
                }
                // Performer
            }
        } catch (OXException e) {
            log.error("", e);
        }
        return publications;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.OXPublicationInterface#listPublications(com.openexchange.admin.rmi.dataobjects.Context, java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public List<Publication> listPublications(Context ctx, Credentials auth, String entityId) throws RemoteException, NoSuchPublicationException, MissingServiceException {
        authenticate(ctx, auth);

        List<Publication> publications = new ArrayList<Publication>();
        try {
            final com.openexchange.groupware.contexts.Context oxCtx = getOXContext(ctx);
            for (PublicationTarget pubTar : listTargets()) {
                final PublicationService publicationService = pubTar.getPublicationService();

                // Performer
                if (null != publicationService) {
                    Collection<com.openexchange.publish.Publication> allPublications = publicationService.getAllPublications(oxCtx, entityId);
                    for (com.openexchange.publish.Publication pub : allPublications) {
                        String description = publicationService.getInformation(pub);
                        publications.add(parsePublication(pub, description));
                    }
                }
                // Performer
            }
        } catch (OXException e) {
            log.error("", e);
        }
        return publications;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.OXPublicationInterface#listPublications(com.openexchange.admin.rmi.dataobjects.Context, int, java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public List<Publication> listPublications(Context ctx, Credentials auth, int user, String module) throws RemoteException, NoSuchPublicationException, MissingServiceException {
        authenticate(ctx, auth);

        List<Publication> publications = new ArrayList<Publication>();
        try {
            final com.openexchange.groupware.contexts.Context oxCtx = getOXContext(ctx);
            for (PublicationTarget pubTar : listTargets()) {
                if (pubTar.getModule().equals(module)) {
                    final PublicationService publicationService = pubTar.getPublicationService();

                    // Performer
                    if (null != publicationService) {
                        Collection<com.openexchange.publish.Publication> allPublications = publicationService.getAllPublications(oxCtx, user, module);
                        for (com.openexchange.publish.Publication pub : allPublications) {
                            String description = publicationService.getInformation(pub);
                            publications.add(parsePublication(pub, description));
                        }
                    }
                    // Performer
                }
            }
        } catch (OXException e) {
            log.error("", e);
        }
        return publications;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.OXPublicationInterface#listPublications(com.openexchange.admin.rmi.dataobjects.Context, int, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public List<Publication> listPublications(Context ctx, Credentials credentials, int user) throws RemoteException, NoSuchPublicationException, MissingServiceException {
        authenticate(ctx, credentials);

        List<Publication> publications = new ArrayList<Publication>();
        try {
            final com.openexchange.groupware.contexts.Context oxCtx = getOXContext(ctx);
            for (PublicationTarget pubTar : listTargets()) {
                final PublicationService publicationService = pubTar.getPublicationService();

                // Performer
                if (null != publicationService) {
                    Collection<com.openexchange.publish.Publication> allPublications = publicationService.getAllPublications(oxCtx);
                    for (com.openexchange.publish.Publication pub : allPublications) {
                        if (pub.getUserId() == user) {
                            String description = publicationService.getInformation(pub);
                            publications.add(parsePublication(pub, description));
                        }
                    }
                }
                // Performer
            }
        } catch (OXException e) {
            log.error("", e);
        }
        return publications;
    }

    @Override
    public boolean deletePublication(Context ctx, String url, Credentials credentials) throws RemoteException, NoSuchPublicationException, MissingServiceException {
        authenticate(ctx, credentials);

        try {
            final com.openexchange.groupware.contexts.Context oxCtx = getOXContext(ctx);
            for (PublicationTarget pubTar : listTargets()) {
                final PublicationService publicationService = pubTar.getPublicationService();

                // Performer
                if (null != publicationService) {
                    com.openexchange.publish.Publication currentPublication = publicationService.resolveUrl(oxCtx, url);
                    if (null != currentPublication) {
                        publicationService.delete(currentPublication);
                        return true;
                    }
                }
                // Performer
            }
        } catch (OXException e) {
            log.error("", e);
        }
        throw new NoSuchPublicationException("No such publication with URL \"" + url + "\"");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.OXPublicationInterface#deletePublications(com.openexchange.admin.rmi.dataobjects.Context, int, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public List<Publication> deletePublications(Context ctx, Credentials credentials, int user) throws RemoteException, NoSuchPublicationException, MissingServiceException {
        authenticate(ctx, credentials);

        List<Publication> publications = new ArrayList<Publication>();
        try {
            final com.openexchange.groupware.contexts.Context oxCtx = getOXContext(ctx);
            for (PublicationTarget pubTar : listTargets()) {
                final PublicationService publicationService = pubTar.getPublicationService();

                // Performer
                if (null != publicationService) {
                    Collection<com.openexchange.publish.Publication> allPublications = publicationService.getAllPublications(oxCtx);
                    for (com.openexchange.publish.Publication pub : allPublications) {
                        if (pub.getUserId() == user) {
                            publicationService.delete(pub);
                            String description = publicationService.getInformation(pub);
                            publications.add(parsePublication(pub, description));
                        }
                    }
                }
                // Performer
            }
        } catch (OXException e) {
            log.error("", e);
        }
        return publications;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.OXPublicationInterface#deletePublication(com.openexchange.admin.rmi.dataobjects.Context, int, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public Publication deletePublication(Context ctx, Credentials credentials, int publicationId) throws RemoteException, NoSuchPublicationException, MissingServiceException {
        authenticate(ctx, credentials);

        try {
            final com.openexchange.groupware.contexts.Context oxCtx = getOXContext(ctx);
            for (PublicationTarget pubTar : listTargets()) {
                final PublicationService publicationService = pubTar.getPublicationService();

                // Performer
                if (publicationService != null) {
                    com.openexchange.publish.Publication publication = publicationService.load(oxCtx, publicationId);
                    if (publication != null) {
                        String description = publicationService.getInformation(publication);
                        publicationService.delete(oxCtx, publicationId);
                        return parsePublication(publication, description);
                    }
                }
                // Performer
            }
        } catch (OXException e) {
            log.error("", e);
        }

        throw new NoSuchPublicationException("No such publication with ID \"" + publicationId + "\"");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.OXPublicationInterface#deletePublications(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials, java.lang.String)
     */
    @Override
    public List<Publication> deletePublications(Context ctx, Credentials credentials, String entityId) throws RemoteException, NoSuchPublicationException, MissingServiceException {
        authenticate(ctx, credentials);

        List<Publication> publications = new ArrayList<Publication>();
        try {
            final com.openexchange.groupware.contexts.Context oxCtx = getOXContext(ctx);
            for (PublicationTarget pubTar : listTargets()) {
                final PublicationService publicationService = pubTar.getPublicationService();

                // Performer
                Collection<com.openexchange.publish.Publication> allPublications = publicationService.getAllPublications(oxCtx, entityId);
                for (com.openexchange.publish.Publication pub : allPublications) {
                    if (pub.getEntityId().equals(entityId)) {
                        String description = publicationService.getInformation(pub);
                        publicationService.delete(pub);
                        publications.add(parsePublication(pub, description));
                    }
                }
                // Performer
            }
        } catch (OXException e) {
            log.error("", e);
        }

        return publications;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.OXPublicationInterface#deletePublications(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials, int, java.lang.String)
     */
    @Override
    public List<Publication> deletePublications(Context ctx, Credentials credentials, int user, String module) throws RemoteException, NoSuchPublicationException, MissingServiceException {
        authenticate(ctx, credentials);

        List<Publication> publications = new ArrayList<Publication>();
        try {
            final com.openexchange.groupware.contexts.Context oxCtx = getOXContext(ctx);
            for (PublicationTarget pubTar : listTargets()) {
                if (pubTar.getModule().equals(module)) {
                    final PublicationService publicationService = pubTar.getPublicationService();

                    // Performer
                    if (null != publicationService) {
                        Collection<com.openexchange.publish.Publication> allPublications = publicationService.getAllPublications(oxCtx, user, module);
                        for (com.openexchange.publish.Publication pub : allPublications) {
                            String description = publicationService.getInformation(pub);
                            publicationService.delete(pub);
                            publications.add(parsePublication(pub, description));
                        }
                    }
                    // Performer
                }
            }
        } catch (OXException e) {
            log.error("", e);
        }
        return publications;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.OXPublicationInterface#deletePublications(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    @Override
    public List<Publication> deletePublications(Context ctx, Credentials credentials) throws RemoteException, NoSuchPublicationException, MissingServiceException {
        authenticate(ctx, credentials);

        List<Publication> publications = new ArrayList<Publication>();
        try {
            final com.openexchange.groupware.contexts.Context oxCtx = getOXContext(ctx);
            for (PublicationTarget pubTar : listTargets()) {
                final PublicationService publicationService = pubTar.getPublicationService();

                // Performer
                if (null != publicationService) {
                    Collection<com.openexchange.publish.Publication> allPublications = publicationService.getAllPublications(oxCtx);
                    for (com.openexchange.publish.Publication pub : allPublications) {
                        String description = publicationService.getInformation(pub);
                        publicationService.delete(pub);
                        publications.add(parsePublication(pub, description));
                    }
                }
                // Performer
            }
        } catch (OXException e) {
            log.error("", e);
        }
        return publications;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.OXPublicationInterface#deletePublication(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials, java.lang.String)
     */
    @Override
    public Publication deletePublication(Context ctx, Credentials credentials, String url) throws RemoteException, NoSuchPublicationException, MissingServiceException {
        authenticate(ctx, credentials);

        try {
            final com.openexchange.groupware.contexts.Context oxCtx = getOXContext(ctx);
            for (PublicationTarget pubTar : listTargets()) {
                final PublicationService publicationService = pubTar.getPublicationService();

                // Performer
                if (null != publicationService) {
                    com.openexchange.publish.Publication currentPublication = publicationService.resolveUrl(oxCtx, url);
                    if (null != currentPublication) {
                        String description = publicationService.getInformation(currentPublication);
                        publicationService.delete(currentPublication);
                        return parsePublication(currentPublication, description);
                    }
                }
                // Performer
            }
        } catch (OXException e) {
            log.error("", e);
        }
        throw new NoSuchPublicationException("No such publication with URL \"" + url + "\"");
    }

    /**
     * Get a service from the AdminServiceRegistry
     * 
     * @param clazz The service to get
     * @return The service instance
     * @throws MissingServiceException
     */
    private <S> S getService(Class<? extends S> clazz) throws MissingServiceException {
        S service = AdminServiceRegistry.getInstance().getService(clazz);
        if (null == service) {
            throw new MissingServiceException(clazz.getSimpleName() + " is missing or not started yet");
        }
        return service;
    }

    /**
     * Authenticate
     * 
     * @param context The context
     * @param credentials The credentials
     * @throws RemoteException If authentication fails
     */
    private void authenticate(Context context, Credentials credentials) throws RemoteException {
        Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            basicauth.doAuthentication(auth, context);
        } catch (InvalidCredentialsException e) {
            throw new RemoteException(e.getMessage());
        } catch (StorageException e) {
            throw new RemoteException(e.getMessage());
        } catch (InvalidDataException e) {
            throw new RemoteException(e.getMessage());
        }
    }

    /**
     * Parses the specified OX publication and returns it as an RMI object
     * 
     * @param input The OX publication
     * @param description The description
     * @return The RMI publication
     */
    private Publication parsePublication(com.openexchange.publish.Publication input, String description) {
        Publication pub = new Publication();
        pub.setContext(new Context(Integer.valueOf(input.getContext().getContextId())));
        pub.setEntityId(input.getEntityId());
        pub.setId(Integer.valueOf(input.getId()));
        pub.setUserId(Integer.valueOf(input.getUserId()));
        pub.setModule(input.getModule());
        pub.setName(input.getDisplayName());
        pub.setDescription(description);
        pub.setUrl((String) input.getConfiguration().get("url"));
        return pub;
    }

    /**
     * List all publication targets
     * 
     * @return A collection with all available publication targets
     * @throws OXException
     * @throws MissingServiceException
     */
    private Collection<PublicationTarget> listTargets() throws OXException, MissingServiceException {
        PublicationTargetDiscoveryService discovery = getService(PublicationTargetDiscoveryService.class);
        return discovery.listTargets();
    }

    /**
     * Get the OX context from the RMI context
     * 
     * @param ctx The RMI Context
     * @return The OX Context
     * @throws MissingServiceException
     * @throws OXException
     */
    private com.openexchange.groupware.contexts.Context getOXContext(Context ctx) throws MissingServiceException, OXException {
        ContextService contexts = getService(ContextService.class);
        return contexts.getContext(ctx.getId());

    }
}

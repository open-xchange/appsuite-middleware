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

package com.openexchange.admin.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Publication;
import com.openexchange.admin.rmi.exceptions.MissingServiceException;
import com.openexchange.admin.rmi.exceptions.NoSuchPublicationException;

/**
 *
 * This interface defines methods for checking and deleting Publications by Users.<br><br>
 *
 * <b>Example:</b>
 * <pre>
 * final OXPublicationInterface iface = (OXPublicationInterface)Naming.lookup("rmi:///oxhost/"+OXPublicationInterface.RMI_NAME);
 * </pre>
 *
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface OXPublicationInterface extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "OXPublication";

    /**
     * Gets a Publication for a given URL
     *
     * @return Publication if Publication is found
     * @throws OXException
     * @throws NoSuchPublicationException
     * @throws MissingServiceException
     */
    public Publication getPublication(Context ctx, String url, Credentials credentials) throws RemoteException, NoSuchPublicationException, MissingServiceException;

    /**
     * Lists all publications in the specified context
     * 
     * @param ctx The context
     * @param credentials The context administrative credentials
     * @return A Collection with all publications in the specified context
     * @throws RemoteException
     * @throws NoSuchPublicationException
     * @throws MissingServiceException
     */
    public List<Publication> listPublications(Context ctx, Credentials credentials) throws RemoteException, NoSuchPublicationException, MissingServiceException;

    /**
     * List all publications in the specified context with the specified entity identifier
     * 
     * @param ctx The context
     * @param credentials The credentials
     * @param entityId The entity identifier
     * @return A Collection with all publications in the specified context with the specified entity identifier
     * @throws RemoteException
     * @throws NoSuchPublicationException
     * @throws MissingServiceException
     */
    public List<Publication> listPublications(Context ctx, Credentials credentials, String entityId) throws RemoteException, NoSuchPublicationException, MissingServiceException;

    /**
     * Lists all publications for the specified user in the specified context for the specified module
     * 
     * @param ctx The context
     * @param credentials The context's administrative credentials
     * @param user The user identifier
     * @param module The module name
     * @return A Collection with all user publications in the specified context for the specified module
     * @throws RemoteException
     * @throws NoSuchPublicationException
     * @throws MissingServiceException
     */
    public List<Publication> listPublications(Context ctx, Credentials credentials, int user, String module) throws RemoteException, NoSuchPublicationException, MissingServiceException;

    /**
     * Lists all publications for the specified user in the specified context
     * 
     * @param ctx The context
     * @param credentials The context's administrative credentials
     * @param user The user identifier
     * @return A Collection with all user publications in the specified context for the specified module
     * @throws RemoteException
     * @throws NoSuchPublicationException
     * @throws MissingServiceException
     */
    public List<Publication> listPublications(Context ctx, Credentials credentials, int user) throws RemoteException, NoSuchPublicationException, MissingServiceException;

    /**
     * Deletes a Publication
     *
     * @return true if the publication is deleted; false otherwise
     * @throws OXException
     * @throws NoSuchPublicationException
     * @throws MissingServiceException
     * @deprecated Use {@link OXPublicationInterface.deletePublication(Context ctx, Credentials credentials, String url)} instead
     */
    public boolean deletePublication(Context ctx, String url, Credentials credentials) throws RemoteException, NoSuchPublicationException, MissingServiceException;

    /**
     * Deletes all Publications for the specified user
     * 
     * @param ctx The context
     * @param credentials The context's administrative credentials
     * @param user The user identifier
     * @throws RemoteException
     * @throws NoSuchPublicationException
     * @throws MissingServiceException
     */
    public List<Publication> deletePublications(Context ctx, Credentials credentials, int user) throws RemoteException, NoSuchPublicationException, MissingServiceException;

    /**
     * Deletes the publication with the specified identifier
     * 
     * @param ctx The context
     * @param credentials The context's administrative credentials
     * @param publicationId The publication identifier
     * @return the publication that was deleted
     * @throws RemoteException
     * @throws NoSuchPublicationException
     * @throws MissingServiceException
     */
    public Publication deletePublication(Context ctx, Credentials credentials, int publicationId) throws RemoteException, NoSuchPublicationException, MissingServiceException;

    /**
     * Delete all publications in the specified context with the specified entity identifier
     * 
     * @param ctx The context
     * @param credentials The context's administrative credentials
     * @param entityId The entity identifier
     * @throws RemoteException
     * @throws NoSuchPublicationException
     * @throws MissingServiceException
     */
    public List<Publication> deletePublications(Context ctx, Credentials credentials, String entityId) throws RemoteException, NoSuchPublicationException, MissingServiceException;

    /**
     * Delete all publications for the specified user in the specified context for the specified module
     * 
     * @param ctx The context
     * @param credentials The context's administrative credentials
     * @param user The user identifier
     * @param module The module name
     * @throws RemoteException
     * @throws NoSuchPublicationException
     * @throws MissingServiceException
     */
    public List<Publication> deletePublications(Context ctx, Credentials credentials, int user, String module) throws RemoteException, NoSuchPublicationException, MissingServiceException;

    /**
     * Deletes all publications in the specified context
     * 
     * @param ctx The context
     * @param credentials The context's administrative credentials
     * @return A list with all publication ids that were deleted
     * @throws RemoteException
     * @throws NoSuchPublicationException
     * @throws MissingServiceException
     */
    public List<Publication> deletePublications(Context ctx, Credentials credentials) throws RemoteException, NoSuchPublicationException, MissingServiceException;

    /**
     * Deletes a Publication
     *
     * @return the publication that was deleted
     * @throws OXException
     * @throws NoSuchPublicationException
     * @throws MissingServiceException
     */
    public Publication deletePublication(Context ctx, Credentials credentials, String url) throws RemoteException, NoSuchPublicationException, MissingServiceException;

}

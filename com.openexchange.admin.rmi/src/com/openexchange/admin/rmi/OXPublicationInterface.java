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
package com.openexchange.admin.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
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
 *
 * </pre
 *
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 *
 */
public interface OXPublicationInterface extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "OXPublication";
    
    /**
     * This method returns a Publication for a given Url
     *
     * @return Publication if Publication is found
     * @throws OXException 
     * @throws NoSuchPublicationException 
     * @throws MissingServiceException 
     */
    public Publication getPublication(Context ctx, String url, Credentials auth) throws RemoteException, NoSuchPublicationException, MissingServiceException;
    
    /**
     * This method will delete a Publication
     *
     * @return true if the publication is deleted, false if not
     * @throws OXException 
     * @throws NoSuchPublicationException 
     * @throws MissingServiceException 
     */
    public boolean deletePublication(Context ctx, String url, Credentials auth) throws RemoteException, NoSuchPublicationException, MissingServiceException;
    
}

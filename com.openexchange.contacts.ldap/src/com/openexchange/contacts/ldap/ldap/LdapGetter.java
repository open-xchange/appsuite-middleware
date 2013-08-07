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

package com.openexchange.contacts.ldap.ldap;

import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;


/**
 * This interface describes how values can be fetched from an ldap object
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface LdapGetter {

    /**
     * Gets the value of the object with the given name
     *
     * @param attributename The name of the attribute
     * @return null if the attribute is not available
     * @throws OXException If something went wrong
     */
    public String getAttribute(final String attributename) throws OXException;

    /**
     * Gets the value of the object with the given name as Date
     *
     * @param birthday
     * @return null if the attribute is not available
     * @throws OXException If something went wrong
     */
    public Date getDateAttribute(final String attributename) throws OXException;

    /**
     * Gets the value of the object with the given name as int
     *
     * @param attributename The name of the attribute
     * @return -1 if the attribute is not available
     * @throws OXException If something went wrong
     */
    public int getIntAttribute(final String attributename) throws OXException;

    /**
     * Gets an LdapGetter object for getting the attributes below the object with
     * the specified distinguished name.
     *
     * @param dn The dn for which the getter should be returned
     * @param attributes The attributes which should be fetched (not all are needed)
     * @return null if no such object was found
     * @throws OXException
     */
    public LdapGetter getLdapGetterForDN(final String dn, final String[] attributes) throws OXException;

    /**
     * Gets the values of the object with the given name as array used
     * especially for multi-value attributes
     *
     * @param attributename
     * @return
     * @throws OXException
     */
    public List<String> getMultiValueAttribute(final String attributename) throws OXException;

    /**
     * Gets the fullname of the object to which the attributes belong
     *
     * @return
     * @throws OXException
     */
    public String getObjectFullName() throws OXException;
}

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

package com.openexchange.carddav.mixins;

import com.openexchange.carddav.CarddavProtocol;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;


///**
// * The {@link CalendarHomeSet} property mixin extends resources to include a pointer to the collection containing all of a users calendars.
// *
// * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
// */


/**
 * {@link AddressbookHomeSet} - Identifies the URL of any WebDAV collections 
 * that contain address book collections owned by the associated principal
 * resource.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class AddressbookHomeSet extends SingleXMLPropertyMixin {
	
/*	
   Name:  addressbook-home-set

   Namespace:  urn:ietf:params:xml:ns:carddav

   Purpose:  Identifies the URL of any WebDAV collections that contain
      address book collections owned by the associated principal
      resource.

   Protected:  MAY be protected if the server has fixed locations in
      which address books are created.

   COPY/MOVE behavior:  This property value MUST be preserved in COPY
      and MOVE operations.

   allprop behavior:  SHOULD NOT be returned by a PROPFIND DAV:allprop
      request.

   Description:  The CARDDAV:addressbook-home-set property is meant to
      allow users to easily find the address book collections owned by
      the principal.  Typically, users will group all the address book
      collections that they own under a common collection.  This
      property specifies the URL of collections that are either address
      book collections or ordinary collections that have child or
      descendant address book collections owned by the principal.

   Definition:

       <!ELEMENT addressbook-home-set (DAV:href*)>

   Example:

       <C:addressbook-home-set xmlns:D="DAV:"
          xmlns:C="urn:ietf:params:xml:ns:carddav">
         <D:href>/bernard/addresses/</D:href>
       </C:addressbook-home-set>
*/
    private static final String PROPERTY_NAME = "addressbook-home-set";
    public static final String ADDRESSBOOK_HOME = "/carddav/";
    
    public AddressbookHomeSet() {
        super(CarddavProtocol.CARD_NS.getURI(), PROPERTY_NAME);
    }

    @Override
    protected String getValue() {
        return "<D:href>/carddav/</D:href>";
    }

}

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

package com.openexchange.caldav.mixins;

import org.apache.commons.logging.Log;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.resources.CommonFolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.log.LogFactory;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * The {@link Owner}
 * 
 * This property identifies a particular principal as being the "owner" of the 
 * resource. Since the owner of a resource often has special access control 
 * capabilities (e.g., the owner frequently has permanent DAV:write-acl 
 * privilege), clients might display the resource owner in their user 
 * interface.
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Owner extends SingleXMLPropertyMixin {

    private static final Log LOG = LogFactory.getLog(Owner.class);
    private static final String PROPERTY_NAME = "owner";
    private final CommonFolderCollection<?> collection;    

    public Owner(CommonFolderCollection<?> collection) {
        super(Protocol.DAV_NS.getURI(), PROPERTY_NAME);
        this.collection = collection;
    }
    
    @Override
    protected String getValue() {
        User owner = null;
        try {
            owner = collection.getOwner();
        } catch (OXException e) {
            LOG.error("error determining owner from collection", e);
        }
        return null != owner ? "<D:href>/principals/users/" + owner.getLoginInfo() + "</D:href>" : null;
    }

}

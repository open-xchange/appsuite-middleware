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

package com.openexchange.caldav.mixins;

import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.dav.mixins.PrincipalURL;
import com.openexchange.dav.resources.CommonFolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link CalendarOwner}<p/>
 *
 * <b>Name:</b>   calendar-owner<p/>
 * <b>Location:</b>   MUST appear on a calendar if there is a principal resources (user or group) with which it is associated.<p/>
 * <b>Purpose:</b>   This property is used for browsing clients to find out the user, group or resource for which the calendar events are
 * scheduled.  Sometimes the calendar is a user's calendar, in which case the value SHOULD be the user's principal URL from WebDAV ACL.
 * (In this case the DAV:owner property probably has the same principal URL value.) If the calendar is a group calendar the value SHOULD be
 * the group's principal URL.  (In this case the DAV:owner property probably specifies one user who manages this group calendar.) If the
 * calendar is a resource calendar (e.g.  for a room, or a projector) there won't be a principal URL, so some other URL SHOULD be used.
 * A LDAP URL could be useful in this case.  This property contains one 'href' element in the "DAV:" namespace.</p>
 * <b>Declaration:</b>   <code><!ELEMENT calendar-owner (href) ></code>
 * <b>Extensibility:</b>   MAY contain additional elements, which MUST be ignored if not understood.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CalendarOwner extends SingleXMLPropertyMixin {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarOwner.class);

    private final CommonFolderCollection<?> collection;

    /**
     * Initializes a new {@link CalendarOwner}.
     *
     * @param collection The underlying folder collection to represent the calendar owner for
     */
    public CalendarOwner(CommonFolderCollection<?> collection) {
        super(CaldavProtocol.CAL_NS.getURI(), "calendar-owner");
        this.collection = collection;
    }

    @Override
    protected String getValue() {
        User owner = null;
        try {
            owner = collection.getOwner();
        } catch (OXException e) {
            LOG.warn("error determining owner from folder collection '{}'", collection.getFolder(), e);
        }
        return null != owner ? "<D:href>" + PrincipalURL.forUser(owner.getId()) + "</D:href>" : null;
    }

}

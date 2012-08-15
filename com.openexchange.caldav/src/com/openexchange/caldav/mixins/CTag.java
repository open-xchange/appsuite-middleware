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

import java.util.Date;
import org.apache.commons.logging.Log;
import com.openexchange.caldav.resources.CommonFolderCollection;
import com.openexchange.log.LogFactory;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;


/**
 * {@link CTag}
 * 
 * getctag xmlns="http://calendarserver.org/ns/"
 * 
 * The "calendar-ctag" is like a resource "etag"; it changes when anything in the calendar has changed. This allows the client to quickly 
 * determine that it does not need to synchronize any changed events.
 * @see <a href="https://trac.calendarserver.org/browser/CalendarServer/trunk/doc/Extensions/caldav-ctag.txt">caldav-ctag-02</a>
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CTag extends SingleXMLPropertyMixin {
	
    private static final Log LOG = com.openexchange.log.Log.loggerFor(CTag.class);

	private final CommonFolderCollection<?> collection;
    private String value = null;
    
    public CTag(CommonFolderCollection<?> collection) {
        super("http://calendarserver.org/ns/", "getctag");
        this.collection = collection;        
    }   
    
    @Override
    protected String getValue() {
        if (null == value && null != collection) {
            try {
                Date lastModified = this.collection.getLastModified();
                String folderID = null != collection.getFolder() ? collection.getFolder().getID() : "0";
                this.value = "http://www.open-xchange.com/ctags/" + folderID + "-" + (null != lastModified ? lastModified.getTime() : 0);
            } catch (WebdavProtocolException e) {
                LOG.error("error determining ctag from collection", e);
            }
        }
        return value;
    }
}

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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.calendar;

import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.attach.AttachmentAuthorization;
import com.openexchange.groupware.attach.AttachmentEvent;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.sessiond.SessionObjectWrapper;
import com.openexchange.tools.StringCollection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CalendarAttachments
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public class CalendarAttachments implements  AttachmentListener, AttachmentAuthorization {
    
    private static final Log LOG = LogFactory.getLog(CalendarAttachments.class);
    
    public long attached(AttachmentEvent e) throws Exception {
        CalendarSql csql = new CalendarSql(null);
        return csql.attachmentAction(e.getAttachedId(), e.getUser().getId(), e.getContext(), true);
    }
    
    public long detached(AttachmentEvent e) throws Exception {
        CalendarSql csql = new CalendarSql(null);
        return csql.attachmentAction(e.getAttachedId(), e.getUser().getId(), e.getContext(), false);
    }
    
    public void checkMayAttach(int folderId, int objectId, User user, UserConfiguration userConfig, Context ctx) throws OXException {
        try {
            SessionObject so = SessionObjectWrapper.createSessionObject(user.getId(), ctx, CalendarCommonCollection.getUniqueCalendarSessionName());
            if (!CalendarCommonCollection.getWritePermission(objectId, folderId, so)) {
                throw new OXCalendarException(OXCalendarException.Code.NO_PERMISSIONS_TO_ATTACH_DETACH);
            }
        } catch (OXObjectNotFoundException oxonfe) {
            if (LOG.isErrorEnabled()) {
                LOG.error(StringCollection.convertArraytoString(new Object[] { "checkMayAttach failed. The object does not exists (cid:oid) : ",ctx.getContextId(),":",objectId } ));
            }
            throw oxonfe;
        } catch(Exception e) {
            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 14);
        }
    }
    
    public void checkMayDetach(int folderId, int objectId, User user, UserConfiguration userConfig, Context ctx) throws OXException {
        checkMayAttach(folderId, objectId, user, userConfig, ctx);
    }
    
    public void checkMayReadAttachments(int folderId, int objectId, User user, UserConfiguration userConfig, Context ctx) throws OXException {
        try {
            SessionObject so = SessionObjectWrapper.createSessionObject(user.getId(), ctx, CalendarCommonCollection.getUniqueCalendarSessionName());
            if (!CalendarCommonCollection.getReadPermission(objectId, folderId, so)) {
                throw new OXCalendarException(OXCalendarException.Code.NO_PERMISSIONS_TO_READ);
            }
        } catch (OXObjectNotFoundException oxonfe) {
            if (LOG.isErrorEnabled()) {
                LOG.error(StringCollection.convertArraytoString(new Object[] { "checkMayReadAttachments failed. The object does not exists (cid:oid) : ",ctx.getContextId(),":",objectId } ));
            }
            throw oxonfe;
        } catch(Exception e) {
            throw new OXCalendarException(OXCalendarException.Code.UNEXPECTED_EXCEPTION, e, 15);
        }
    }
    
}

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



package com.openexchange.api2;

import java.sql.Connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.container.LinkObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.links.Links;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.session.Session;

/**
 * LinkSQLInterface
 *
 * @author <a href="mailto:ben.pahne@open-xchange.com">Benjamin Frederic Pahne</a>
 */
public class RdbLinkSQLInterface implements LinkSQLInterface {
	
	private static final Log LOG = LogFactory.getLog(RdbLinkSQLInterface.class);
	
	public LinkObject[] getLinksOfObject(final int objectId, final int type, final int folder, final int user, final int[] group, final Session sessionobject) throws OXException {
		LinkObject[] lo = null;
		Connection readcon = null;
		Context ctx = null;
		try{
			ctx = ContextStorage.getStorageContext(sessionobject.getContextId());
			readcon = DBPool.pickup(ctx);
			lo = Links.getAllLinksFromObject(objectId,type,folder,user,group,sessionobject,readcon);
		} catch (ContextException ct){
			new ContactException(ct);
		}catch (final DBPoolingException e){
			LOG.error("AN ERROR OCCURRED DURING saveLink", e);
		}catch (final OXException e){
			throw e;
			//throw new OXException("AN ERROR OCCURRED DURING getLinksOfObject", e);
		} finally {
			if (readcon != null) {
				DBPool.closeReaderSilent(ctx, readcon);
			}
		}
		return lo;
	}

	public void saveLink(final LinkObject l, final int user, final int[] group, final Session so) throws OXException {
		Connection writecon = null;
		Context ctx = null;
		try{
			ctx = ContextStorage.getStorageContext(so.getContextId());
			writecon = DBPool.pickupWriteable(ctx);
			Links.performLinkStorage(l, user, group, so, writecon);
		}catch (ContextException ct){
			new ContactException(ct);
		}catch (final DBPoolingException e){
			LOG.error("AN ERROR OCCURRED DURING saveLink", e);
		}catch (final OXException e){
			throw e;
			//throw new OXException("AN ERROR OCCURRED DURING saveLink", e);
		} finally {
			if (writecon != null) {
				DBPool.closeWriterSilent(ctx, writecon);
			}
		}
	}

	public int[][] deleteLinks(final int id, final int type, final int folder, final int[][] data, final int user, final int[] group, final Session sessionobject) throws OXException {

		int[][] resp = null;
		Connection writecon = null;
		Connection readcon = null;
		Context ctx = null;
		try{
			ctx = ContextStorage.getStorageContext(sessionobject.getContextId());
			readcon = DBPool.pickup(ctx);
			writecon = DBPool.pickupWriteable(ctx);
			resp = Links.deleteLinkFromObject(id,type,folder,data,user,group,sessionobject,readcon,writecon);
		}catch (ContextException ct){
			new ContactException(ct);
		}catch (final DBPoolingException e){
			LOG.error("AN ERROR OCCURRED DURING saveLink", e);
		}catch (final OXException e){
			throw e;
			//throw new OXException("AN ERROR OCCURRED DURING deleteLinks", e);
		} finally {
			if (readcon != null) {
				DBPool.closeReaderSilent(ctx, readcon);
			}
			if (writecon != null) {
				DBPool.closeWriterSilent(ctx, writecon);
			}
		}
		return resp;
	}
}

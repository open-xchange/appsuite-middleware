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

import com.openexchange.groupware.container.LinkObject;
import com.openexchange.groupware.links.Links;
import com.openexchange.server.DBPool;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.SessionObject;

/**
 * LinkSQLInterface
 *
 * @author <a href="mailto:ben.pahne@netline-is.de">Benjamin Frederic Pahne</a>
 */
public class RdbLinkSQLInterface implements LinkSQLInterface {
	
	private static final Log LOG = LogFactory.getLog(RdbLinkSQLInterface.class);
	
	public LinkObject[] getLinksOfObject(int objectId, int type, int folder, int user, int[] group, SessionObject sessionobject) throws OXException {
		LinkObject[] lo = null;
		Connection readcon = null;
		try{
			readcon = DBPool.pickup(sessionobject.getContext());
			lo = Links.getAllLinksFromObject(objectId,type,folder,user,group,sessionobject,readcon);
		}catch (DBPoolingException e){
			LOG.error("AN ERROR OCCURED DURING saveLink", e);
		}catch (OXException e){
			throw e;
			//throw new OXException("AN ERROR OCCURED DURING getLinksOfObject", e);
		} finally {
			if (readcon != null) {
				DBPool.closeReaderSilent(sessionobject.getContext(), readcon);
			}
		}
		return lo;
	}

	public void saveLink(LinkObject l, int user, int[] group, SessionObject so) throws OXException {
		Connection writecon = null;
		try{
			writecon = DBPool.pickupWriteable(so.getContext());
			Links.performLinkStorage(l, user, group, so, writecon);
		}catch (DBPoolingException e){
			LOG.error("AN ERROR OCCURED DURING saveLink", e);
		}catch (OXException e){
			throw e;
			//throw new OXException("AN ERROR OCCURED DURING saveLink", e);
		} finally {
			if (writecon != null) {
				DBPool.closeWriterSilent(so.getContext(), writecon);
			}
		}
	}

	public int[][] deleteLinks(int id, int type, int folder, int[][] data, int user, int[] group, SessionObject sessionobject) throws OXException {

		int[][] resp = null;
		Connection writecon = null;
		Connection readcon = null;
		try{
			readcon = DBPool.pickup(sessionobject.getContext());
			writecon = DBPool.pickupWriteable(sessionobject.getContext());
			resp = Links.deleteLinkFromObject(id,type,folder,data,user,group,sessionobject,readcon,writecon);
		}catch (DBPoolingException e){
			LOG.error("AN ERROR OCCURED DURING saveLink", e);
		}catch (OXException e){
			throw e;
			//throw new OXException("AN ERROR OCCURED DURING deleteLinks", e);
		} finally {
			if (readcon != null) {
				DBPool.closeReaderSilent(sessionobject.getContext(), readcon);
			}
			if (writecon != null) {
				DBPool.closeWriterSilent(sessionobject.getContext(), writecon);
			}
		}
		return resp;
	}
}

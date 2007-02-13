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



package com.openexchange.groupware.links;

import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.container.LinkObject;

/**
 LinksSql
 @author <a href="mailto:ben.pahne@comfire.de">Benjamin Frederic Pahne</a>

 */

public class LinksMySql implements LinksSql {

	private static final Log LOG = LogFactory.getLog(LinksMySql.class);
	
	public String iFperformLinkStorage(LinkObject l, int cid){
		return new StringBuilder("SELECT firstid, firstmodule, firstfolder, secondid, secondmodule, secondfolder, cid FROM prg_links WHERE ((firstid = "+l.getFirstId()+" AND firstmodule = "+l.getFirstType()+") AND (secondid = "+l.getSecondId()+" AND secondmodule = "+l.getSecondType()+") OR (firstid = "+l.getSecondId()+" AND firstmodule = "+l.getSecondType()+") AND (secondid = "+l.getFirstId()+" AND secondmodule = "+l.getFirstType()+")) AND cid = "+cid).toString();
	}
	
	public String iFperformLinkStorageInsertString(){
		return "INSERT INTO prg_links (firstid, firstmodule, firstfolder,secondid,secondmodule,secondfolder,cid) VALUES (?,?,?,?,?,?,?)";
	}

	public String iFgetLinkFromObject(int first_id, int first_type, int second_id, int second_type, int cid) {
		return new StringBuilder("SELECT firstid, firstmodule, firstfolder, secondid, secondmodule, secondfolder, cid FROM prg_links WHERE (firstid = "+first_id+" AND firstmodule = "+first_type+") AND (secondid = "+second_id+" AND secondmodule = "+second_type+") AND cid = "+cid).toString();
	}
	
	public String iFgetAllLinksFromObject(int id, int type, int folder, int cid){
		return new StringBuilder("SELECT firstid, firstmodule, firstfolder, secondid, secondmodule,secondfolder, cid FROM prg_links WHERE (firstid = "+id+" AND firstmodule = "+type+" AND firstfolder = "+folder+") OR (secondid = "+id+" AND secondmodule = "+type+" AND secondfolder = "+folder+") AND (cid = "+cid+')').toString();
	}

	public void iFDeleteLinkFromObject(Statement del, boolean second, int id, int type, int folder, int loadid, int loadfolder, int loadtype, int cid) throws SQLException {	
		if (second){
			LOG.debug(new StringBuilder("DELETE from prg_links WHERE (firstid = "+id+" AND firstmodule = "+type+" AND firstfolder = "+folder+") AND (secondid = "+loadid+" AND secondmodule = "+loadtype+" AND secondfolder = "+loadfolder+") AND cid = "+cid));
			del.execute("DELETE from prg_links WHERE (firstid = "+id+" AND firstmodule = "+type+" AND firstfolder = "+folder+") AND (secondid = "+loadid+" AND secondmodule = "+loadtype+" AND secondfolder = "+loadfolder+") AND cid = "+cid);							
		} else {
			LOG.debug(new StringBuilder("DELETE from prg_links WHERE (firstid = "+loadid+" AND firstmodule = "+loadtype+" AND firstfolder = "+loadfolder+") AND (secondid = "+id+" AND secondmodule = "+type+" AND secondfolder = "+folder+") AND cid = "+cid));
			del.execute("DELETE from prg_links WHERE (firstid = "+loadid+" AND firstmodule = "+loadtype+" AND firstfolder = "+loadfolder+") AND (secondid = "+id+" AND secondmodule = "+type+" AND secondfolder = "+folder+") AND cid = "+cid);
		}
	}
	
	public String iFdeleteAllObjectLinks() {
		return "DELETE FROM prg_links WHERE ((firstid = ? AND firstmodule = ?) OR (secondid = ? AND secondmodule = ?)) AND cid = ?";
	}
	
}

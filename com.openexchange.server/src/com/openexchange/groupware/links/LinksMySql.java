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


package com.openexchange.groupware.links;

import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.logging.Log;
import com.openexchange.groupware.container.LinkObject;
import com.openexchange.log.LogFactory;


/**
 * {@link LinksMySql} - The MySQL implementation of {@link LinksSql}
 *
 * @author <a href="mailto:ben.pahne@open-xchange.com">Benjamin Frederic Pahne</a>
 *
 */
public class LinksMySql implements LinksSql {

	private static final String SQL_START_SELECT = "SELECT firstid,firstmodule,firstfolder,secondid,secondmodule,secondfolder,cid FROM prg_links WHERE ((firstid=";

	private static final String SQL_DELETE_START = "DELETE from prg_links WHERE (firstid=";

	private static final String SQL_AND_SECONDFOLDER = " AND secondfolder=";

	private static final String SQL_AND_FIRSTFOLDER = " AND firstfolder=";

	private static final String SQL_AND_CID = ") AND cid=";

	private static final String SQL_AND_SECONDMODULE = " AND secondmodule=";

	private static final String SQL_AND_FIRSTMODULE = " AND firstmodule=";

	private static final String SQL_AND_SECONDID = ") AND (secondid=";

	private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(LinksMySql.class));

	@Override
    public String iFperformLinkStorage(final LinkObject l, final int cid){
		return new StringBuilder("SELECT firstid, firstmodule, firstfolder, secondid, secondmodule, secondfolder, cid FROM prg_links WHERE ((firstid = ").append(l.getFirstId()).append(SQL_AND_FIRSTMODULE).append(l.getFirstType()).append(SQL_AND_SECONDID).append(l.getSecondId()).append(SQL_AND_SECONDMODULE).append(l.getSecondType()).append(") OR (firstid = ").append(l.getSecondId()).append(SQL_AND_FIRSTMODULE).append(l.getSecondType()).append(SQL_AND_SECONDID).append(l.getFirstId()).append(SQL_AND_SECONDMODULE).append(l.getFirstType()).append(")) AND cid = ").append(cid).toString();
	}

	private static final String SQL_INSERT = "INSERT INTO prg_links (firstid, firstmodule, firstfolder,secondid,secondmodule,secondfolder,cid,uuid) VALUES (?,?,?,?,?,?,?,?)";

	@Override
    public String iFperformLinkStorageInsertString(){
		return SQL_INSERT;
	}

	@Override
    public String iFgetLinkFromObject(final int first_id, final int first_type, final int second_id, final int second_type, final int cid) {
		return new StringBuilder(SQL_START_SELECT).append(first_id).append(SQL_AND_FIRSTMODULE).append(first_type).append(SQL_AND_SECONDID).append(second_id).append(SQL_AND_SECONDMODULE).append(second_type).append(')').append(SQL_AND_CID).append(cid).toString();
	}

	@Override
    public String iFgetAllLinksFromObject(final int id, final int type, final int folder, final int cid) {
		return new StringBuilder(SQL_START_SELECT).append(id).append(SQL_AND_FIRSTMODULE).append(type).append(SQL_AND_FIRSTFOLDER).append(folder).append(") OR (secondid=").append(id).append(SQL_AND_SECONDMODULE).append(type).append(SQL_AND_SECONDFOLDER).append(folder).append(")) AND cid=").append(cid).toString();
	}

	@Override
    public String iFgetAllLinksByObjectID(final int id, final int type, final int cid) {
		return new StringBuilder(SQL_START_SELECT).append(id).append(SQL_AND_FIRSTMODULE).append(type).append(") OR (secondid=").append(id).append(SQL_AND_SECONDMODULE).append(type).append(")) AND cid=").append(cid).toString();
	}

	@Override
    public void iFDeleteLinkFromObject(final Statement del, final boolean second, final int id, final int type, final int folder, final int loadid, final int loadfolder, final int loadtype, final int cid) throws SQLException {
		if (second){
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuilder(SQL_DELETE_START+id+SQL_AND_FIRSTMODULE+type+SQL_AND_FIRSTFOLDER+folder+SQL_AND_SECONDID+loadid+SQL_AND_SECONDMODULE+loadtype+SQL_AND_SECONDFOLDER+loadfolder+SQL_AND_CID+cid));
			}
			del.execute(new StringBuilder(200).append(SQL_DELETE_START).append(id).append(SQL_AND_FIRSTMODULE).append(type).append(SQL_AND_FIRSTFOLDER).append(folder).append(SQL_AND_SECONDID).append(loadid).append(SQL_AND_SECONDMODULE).append(loadtype).append(SQL_AND_SECONDFOLDER).append(loadfolder).append(SQL_AND_CID).append(cid).toString());
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuilder(SQL_DELETE_START+loadid+SQL_AND_FIRSTMODULE+loadtype+SQL_AND_FIRSTFOLDER+loadfolder+SQL_AND_SECONDID+id+SQL_AND_SECONDMODULE+type+SQL_AND_SECONDFOLDER+folder+SQL_AND_CID+cid));
			}
			del.execute(new StringBuilder(200).append(SQL_DELETE_START).append(loadid).append(SQL_AND_FIRSTMODULE).append(loadtype).append(SQL_AND_FIRSTFOLDER).append(loadfolder).append(SQL_AND_SECONDID).append(id).append(SQL_AND_SECONDMODULE).append(type).append(SQL_AND_SECONDFOLDER).append(folder).append(SQL_AND_CID).append(cid).toString());
		}
	}

	@Override
    public String iFdeleteAllObjectLinks() {
		return "DELETE FROM prg_links WHERE ((firstid = ? AND firstmodule = ?) OR (secondid = ? AND secondmodule = ?)) AND cid = ?";
	}

	private static final String SQL_LINKS_DEL2 = "DELETE FROM prg_links WHERE ((firstfolder = ?) OR (secondfolder = ?)) AND cid = ?";

	@Override
    public String iFdeleteAllFolderLinks() {
		return SQL_LINKS_DEL2;
	}
}

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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.index.internal;


/**
 * {@link IndexDatabaseStuff}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class IndexDatabaseStuff {
    
    public static final String TBL_IDX_SERVER = "index_servers";
    
    public static final String TBL_IDX_MAPPING = "user_module2index";
    
    public static final String SQL_SELECT_INDEX_URL = "SELECT " +
                                                          "s.id, s.serverUrl, s.maxIndices, s.socketTimeout, " +
                                                          "s.connectionTimeout, s.maxConnections, u.index " +
                                                      "FROM " +
                                                          TBL_IDX_SERVER + " AS s " +
                                                      "JOIN " +
                                                          TBL_IDX_MAPPING + " AS u " +
                                                      "ON " +
                                                          "s.id = u.server " +
                                                      "WHERE " +
                                                          "u.cid = ? AND u.uid = ? AND u.module = ?";
    
    public static final String SQL_CREATE_SERVER_TBL = "CREATE TABLE " + TBL_IDX_SERVER + " (" +
                                                           "id int(10) unsigned NOT NULL," +
                                                           "serverUrl varchar(32) NOT NULL," +
                                                           "maxIndices int(10) unsigned default '1000'," +
                                                           "socketTimeout int(10) unsigned default '1000'," +
                                                           "connectionTimeout int(10) unsigned default '100'," +
                                                           "maxConnections int(10) unsigned default '100'," +
                                                           "PRIMARY KEY (id)," +
                                                           "KEY url (serverUrl)" +
                                                       ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
    
    public static final String SQL_CREATE_MAPPING_TBL = "CREATE TABLE " + TBL_IDX_MAPPING + " (" +
                                                            "cid int(10) unsigned NOT NULL," +
                                                            "uid int(10) unsigned NOT NULL," +
                                                            "module int(10) unsigned NOT NULL," +
                                                            "server int(10) unsigned NOT NULL," +
                                                            "index varchar(32) NOT NULL," +
                                                            "PRIMARY KEY  (cid,uid,module)," +
                                                            "KEY user_module (cid,uid,module)," +
                                                            "KEY server (server)" +
                                                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8";
    
    public static final String SQL_DEL_USR_FROM_MAPPING = "DELETE FROM " + 
                                                              TBL_IDX_MAPPING + " " +
                                                          "WHERE " +
                                                              "cid = ? AND uid = ?";
    
    public static final String SQL_DEL_CTX_FROM_MAPPING = "DELETE FROM " + 
                                                              TBL_IDX_MAPPING + " " +
                                                          "WHERE " +
                                                              "cid = ?";
    
    public static final String SQL_INSERT_INDEX_SERVER = "INSERT INTO " + TBL_IDX_SERVER + " " +
                                                             "(id, serverUrl, maxIndices, socketTimeout, connectionTimeout, maxConnections) " +
                                                         "VALUES " +
                                                             "(?, ?, ?, ?, ?, ?)";
    
    public static final String SQL_DELETE_INDEX_SERVER = "DELETE FROM " +
    		                                                 TBL_IDX_SERVER + " " +
    		                                             "WHERE id = ?";
    
    public static final String SQL_DELETE_INDEX_MAPPING = "DELETE FROM " +
                                                              TBL_IDX_MAPPING + " " +
                                                          "WHERE cid = ? AND uid = ? AND module = ?";
    
    public static final String SQL_UPDATE_INDEX_MAPPING = "UPDATE " + TBL_IDX_MAPPING + " " +
                                                          "SET " +
                                                              "server = ?, index = ? " +
                                                          "WHERE cid = ? AND uid = ? AND module = ?";
    
    public static final String SQL_SELECT_INDEX_SERVERS = "SELECT " +
    		                                                  "id, serverUrl, maxIndices, socketTimeout, connectionTimeout, maxConnections " +
    		                                              "FROM " +
    		                                                  TBL_IDX_SERVER;
    
    public static final String SQL_INSERT_INDEX_MAPPING = "INSERT INTO " + TBL_IDX_MAPPING + " " +
                                                    	      "(cid, uid, module, server, index) " +
                                                    	  "VALUES" +
                                                    	      "(?, ?, ?, ?, ?)";
    
    public static final String SQL_UPDATE_INDEX_SERVER = "UPDATE " + TBL_IDX_SERVER + " " +
                                                		 "SET " +
                                                		     "serverUrl = ?, maxIndices = ?, socketTimeout = ?, " +
                                                		     "connectionTimeout = ?, maxConnections = ? " +
                                                		 "WHERE id = ?";
    
    public static final String SQL_DELETE_INDEX_MAPPING_BY_SERVER = "DELETE FROM " +
                                                                        TBL_IDX_MAPPING + " " +
                                                                    "WHERE server = ?";

}

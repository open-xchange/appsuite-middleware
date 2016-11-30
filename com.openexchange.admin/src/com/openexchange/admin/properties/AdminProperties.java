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
package com.openexchange.admin.properties;

/**
 * This class will hold the properties setting from now on
 * @author d7
 *
 */
public class AdminProperties {
	/**
	 * The properties for group
	 * @author d7
	 *
	 */
	public class Group {
	    public static final String CHECK_NOT_ALLOWED_CHARS  = "CHECK_GROUP_UID_FOR_NOT_ALLOWED_CHARS";
	    public static final String AUTO_LOWERCASE           = "AUTO_TO_LOWERCASE_UID";
	    public static final String CHECK_NOT_ALLOWED_NAMES  = "CHECK_GROUP_UID_FOR_NOT_ALLOWED_NAMES";
	    public static final String NOT_ALLOWED_NAMES        = "NOT_ALLOWED_GROUP_UID_NAMES";
            public static final String GID_NUMBER_START        = "GID_NUMBER_START";

	}

	/**
	 * The general properties
	 * @author d7
	 *
	 */
	public class Prop {
	    public static final String SERVER_NAME                      = "SERVER_NAME";

	    public static final String ADMINDAEMON_LOGLEVEL             = "LOG_LEVEL";
	    public static final String ADMINDAEMON_LOGFILE              = "LOG";

	    // public static final String PROPERTIES_SQL_FILE              = "SQL_PROP";
	    // public static final String PROPERTIES_USER_FILE             = "USER_PROP";
	    // public static final String PROPERTIES_GROUP_FILE            = "GROUP_PROP";
	    // public static final String PROPERTIES_RESOURCE_FILE         = "RESOURE_PROP";
	    // public static final String PROPERTIES_RMI_FILE              = "RMI_PROP";
	}

	/**
	 * The properties for resources
	 * @author d7
	 *
	 */
	public class Resource {
	    public static final String CHECK_NOT_ALLOWED_CHARS  = "CHECK_RES_UID_FOR_NOT_ALLOWED_CHARS";
	    public static final String AUTO_LOWERCASE           = "AUTO_TO_LOWERCASE_UID";
	    public static final String CHECK_NOT_ALLOWED_NAMES  = "CHECK_RES_UID_FOR_NOT_ALLOWED_NAMES";
	    public static final String NOT_ALLOWED_NAMES        = "NOT_ALLOWED_RES_UID_NAMES";
	}

	/**
	 * The properties for RMI
	 * @author d7
	 *
	 */
	public class RMI {
	    public static final String RMI_PORT   = "RMI_PORT";
	}

	/**
	 * The properties for the user
	 * @author d7
	 *
	 */
	public class User {
            public static final String UID_NUMBER_START           = "UID_NUMBER_START";
	    public static final String CHECK_NOT_ALLOWED_CHARS    = "CHECK_USER_UID_FOR_NOT_ALLOWED_CHARS";
	    public static final String AUTO_LOWERCASE             = "AUTO_TO_LOWERCASE_UID";
	    public static final String CHECK_NOT_ALLOWED_NAMES    = "CHECK_USER_UID_FOR_NOT_ALLOWED_NAMES";
	    public static final String NOT_ALLOWED_NAMES          = "NOT_ALLOWED_USER_UID_NAMES";
            //public static final String CREATE_HOMEDIRECTORY       = "CREATE_HOMEDIRECTORY";
            //public static final String HOME_DIR_ROOT              = "HOME_DIR_ROOT";
            public static final String PRIMARY_MAIL_UNCHANGEABLE  = "PRIMARY_MAIL_UNCHANGEABLE";
            public static final String USERNAME_CHANGEABLE        = "USERNAME_CHANGEABLE";
            public static final String DEFAULT_PASSWORD_MECHANISM = "DEFAULT_PASSWORD_MECHANISM";
            public static final String DEFAULT_TIMEZONE = "DEFAULT_TIMEZONE";
	}
}

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
package com.openexchange.admin.exceptions;


/**
 * OXUser exception class
 * 
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a> , 
 * <a href="mailto:sebastian.kotyrba@open-xchange.com">Sebastian Kotyrba</a> , 
 * <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 */
public class OXUserException extends Exception {
    
    
    private static final long serialVersionUID = -3925531910546517770L;
    
    
    
    /**
     * If user already exists
     */
    public static final String USER_EXISTS  = "User already exists";
    
    
    /*
     * If client wants to delete the admin user of a context
     */
    public static final String ADMIN_DELETE_NOT_SUPPORTED = "Admin delete not supported";
    
    /**
     * If user does not exist
     */
    public static final String NO_SUCH_USER = "User does not exist";
    
    
    
    /**
     * If user identifier has white spaces
     */
    public static final String SPACE_IN_USER_NAME_EXCEPTION         = "Usernames with spaces are not allowed";
    
    
    
    /**
     * If user identifier has upper chars
     */
    public static final String ONLY_LOW_CHAR_USER_NAME_EXCEPTION    = "Usernames with uppercase chars are not allowed";
    
    
    
    /**
     * If user identifier is not allowed
     */
    public static final String USERNAME_NOT_ALLOWED                 = "This username is not allowed";
    

    /**
     * If identifier contains illegal characters
     */
    public static final String ILLEGAL_CHARS                        = "Illegal characters in userid";

    
    /**
     * OX exceptions for user handling with various messages
     * 
     * @see #USER_EXISTS
     * @see #NO_SUCH_USER
     * @see #SPACE_IN_USER_NAME_EXCEPTION
     * @see #ONLY_LOW_CHAR_USER_NAME_EXCEPTION
     * @see #USERNAME_NOT_ALLOWED
     */
    public OXUserException( String s ) {
        super( s );
    }
}

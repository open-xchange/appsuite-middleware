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
 * OXGroup exception class
 * 
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a> , 
 * <a href="mailto:sebastian.kotyrba@open-xchange.com">Sebastian Kotyrba</a> , 
 * <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 */
public class OXGroupException extends Exception {
    
    
    private static final long serialVersionUID = 5040236157527189890L;
    
    
    
    /**
     * If group already exists
     */
    public static final String GROUP_EXISTS   = "Group already exists";
    
    
    
    /**
     * If requested group does not exist
     */
    public static final String NO_SUCH_GROUP  = "Group does not exist";
    
    
    
    /**
     * If group identifier has white spaces
     */
    public static final String SPACE_IN_GROUP_NAME_EXCEPTION        = "Don't allow group names with space";
    
    
    
    /**
     * If the group has already this member
     */
    public static final String HAVE_THIS_MEMBER                     = "Member already exists in this group";
    
    
    
    /**
     * If group identifier has upper chars
     */
    public static final String ONLY_LOW_CHAR_GROUP_NAME_EXCEPTION   = "Don't allow group names with uppercase char";
    

    /**
     * If identifier contains illegal characters
     */
    public static final String ILLEGAL_CHARS                        = "Illegal characters in groupid";
    
    
    /**
     * OX exceptions for group handling with various messages
     * 
     * @see #GROUP_EXISTS
     * @see #NO_SUCH_GROUP
     * @see #SPACE_IN_GROUP_NAME_EXCEPTION
     * @see #ONLY_LOW_CHAR_GROUP_NAME_EXCEPTION
     */
    public OXGroupException( String s ) {
        super( s );
    }

}

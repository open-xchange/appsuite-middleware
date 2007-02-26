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
 * OXResource exception class
 * 
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a> , 
 * <a href="mailto:sebastian.kotyrba@open-xchange.com">Sebastian Kotyrba</a> , 
 * <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 */
public class OXResourceException extends Exception {
    
    
    private static final long serialVersionUID = -7242584511173089574L;
    
    
    
    /**
     * If resource already exists
     */
    public static final String RESOURCE_EXISTS    = "Resource already exists";
    
    
    
    /**
     * If requested resource not exists
     */
    public static final String NO_SUCH_RESOURCE   = "Resource does not exist";
    
    
    
    /**
     * If resource identifier have white spaces
     */
    public static final String SPACE_IN_RESOURCE_NAME_EXCEPTION      = "Don't allow resource names with space";
    
    
    
    /**
     * If resource identifier have upper chars
     */
    public static final String ONLY_LOW_CHAR_RESOURCE_NAME_EXCEPTION = "Don't allow resource names with uppercase char";
    

    /**
     * If identifier contains illegal characters
     */
    public static final String ILLEGAL_CHARS                        = "Illegal characters in resourceid";
    
    
    /**
     * OX exceptions for resource handling with various messages
     * 
     * @see #RESOURCE_EXISTS
     * @see #NO_SUCH_RESOURCE
     * @see #SPACE_IN_RESOURCE_NAME_EXCEPTION
     * @see #ONLY_LOW_CHAR_RESOURCE_NAME_EXCEPTION
     */
    public OXResourceException( String s ) {
        super( s );
    }
}

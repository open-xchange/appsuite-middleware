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
 * OXResourceGroup exception class
 * 
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a> , 
 * <a href="mailto:sebastian.kotyrba@open-xchange.com">Sebastian Kotyrba</a> , 
 * <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 */
public class OXResourceGroupException extends Exception {
    
    
    private static final long serialVersionUID = 6674820226236204471L;
    
    
    
    /**
     * If resource group already exists
     */
    public static final String RESOURCE_GROUP_EXISTS    = "ResourceGroup already exists";
    
    
    
    /**
     * If requested resource group not exists
     */
    public static final String NO_SUCH_RESOURCE_GROUP   = "ResourceGroup does not exist";
    
    /**
     * If data is missing to create the resource
     */
    public static final String MANDATORY_FIELDS_MISSING   = "Mandatory field(s) missing";
    
    
    
    /**
     * If resource group identifier have white spaces
     */
    public static final String SPACE_IN_RESOURCE_GROUP_NAME_EXCEPTION      = "Don't allow resource group names with space";
    
    
    
    /**
     * If resource group identifier have upper chars
     */
    public static final String ONLY_LOW_CHAR_RESOURCE_GROUP_NAME_EXCEPTION = "Don't allow resource group names with uppercase char";
    
    
    
    /**
     * OX exceptions for resource group handling with various messages
     * 
     * @see #RESOURCE_GROUP_EXISTS
     * @see #NO_SUCH_RESOURCE_GROUP
     * @see #SPACE_IN_RESOURCE_GROUP_NAME_EXCEPTION
     * @see #ONLY_LOW_CHAR_RESOURCE_GROUP_NAME_EXCEPTION
     */
    public OXResourceGroupException( String s ) {
        super( s );
    }
}

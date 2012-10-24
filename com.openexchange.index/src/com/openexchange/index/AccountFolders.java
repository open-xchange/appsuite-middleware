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

package com.openexchange.index;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * {@link AccountFolders} should be used to filter index search results by accounts and folders.
 * If the module you want to search in does not support accounts use {@link IndexConstants#DEFAULT_ATTACHMENT}.<br>
 * <br>
 * If you want to search the whole account use constructor {@link AccountFolders#AccountFolders(String)} and just don't add any folders.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class AccountFolders implements Serializable {
    
    private static final long serialVersionUID = 5383632935001614920L;
    
    private final String account;
    
    private final Set<String> folders;
    
    
    public AccountFolders(String account) {
        this(account, Collections.EMPTY_SET);
    }
    
    public AccountFolders(String account, Set<String> folders) {
        super();
        this.account = account;
        this.folders = new HashSet<String>(folders);
    }
    
    public void addFolder(String folder) {
        folders.add(folder);
    }
    
    public void removeFolder(String folder) {
        folders.remove(folder);
    }
    
    public String getAccount() {
        return account;
    }
    
    public Set<String> getFolders() {
        return folders;
    }

}

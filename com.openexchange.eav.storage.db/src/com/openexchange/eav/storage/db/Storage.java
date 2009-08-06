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

package com.openexchange.eav.storage.db;

import java.util.Set;
import com.openexchange.eav.EAVException;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVPath;
import com.openexchange.eav.EAVSetTransformation;
import com.openexchange.eav.EAVStorage;
import com.openexchange.eav.EAVTypeMetadataNode;
import com.openexchange.groupware.contexts.Context;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Storage implements EAVStorage {

    public void delete(Context ctx, EAVPath path) throws EAVException {
        // TODO Auto-generated method stub

    }

    public EAVNode get(Context ctx, EAVPath path) throws EAVException {
        // TODO Auto-generated method stub
        return null;
    }

    public EAVNode get(Context ctx, EAVPath path, boolean allBinaries) throws EAVException {
        // TODO Auto-generated method stub
        return null;
    }

    public EAVNode get(Context ctx, EAVPath path, Set<EAVPath> loadBinaries) throws EAVException {
        // TODO Auto-generated method stub
        return null;
    }

    public EAVTypeMetadataNode getTypes(Context ctx, EAVPath parent, EAVNode node) throws EAVException {
        // TODO Auto-generated method stub
        return null;
    }

    public void insert(Context ctx, EAVPath parentPath, EAVNode tree) throws EAVException {
        // TODO Auto-generated method stub

    }

    public void replace(Context ctx, EAVPath path, EAVNode tree) throws EAVException {
        // TODO Auto-generated method stub

    }

    public void update(Context ctx, EAVPath parentPath, EAVNode tree) throws EAVException {
        // TODO Auto-generated method stub

    }

    public void updateSets(Context ctx, EAVPath path, EAVSetTransformation update) throws EAVException {
        // TODO Auto-generated method stub

    }

}

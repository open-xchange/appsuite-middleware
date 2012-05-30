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
package com.openexchange.admin.reseller.soap;

import java.util.HashSet;
import com.openexchange.admin.reseller.rmi.extensions.OXContextExtensionImpl;
import com.openexchange.admin.reseller.soap.dataobjects.ResellerContext;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.soap.SOAPUtils;


public final class ResellerContextUtil {

    /**
     * @param ctx
     * @return
     * @throws DuplicateExtensionException
     */
    public static com.openexchange.admin.rmi.dataobjects.Context resellerContext2Context(ResellerContext ctx) throws DuplicateExtensionException {
        com.openexchange.admin.rmi.dataobjects.Context ret = new com.openexchange.admin.rmi.dataobjects.Context();
        ret.setId(ctx.getId());
        ret.setAverage_size(ctx.getAverage_size());
        ret.setEnabled(ctx.getEnabled());
        ret.setFilestore_name(ctx.getFilestore_name());
        ret.setFilestoreId(ctx.getFilestoreId());
        String[] lmappings = ctx.getLoginMappings();
        if( null != lmappings && lmappings.length > 0 ) {
            HashSet<String> lmh = new HashSet<String>();
            for(final String l : lmappings) {
                lmh.add(l);
            }
            ret.setLoginMappings(lmh);
        }
        ret.setMaxQuota(ctx.getMaxQuota());
        ret.setName(ctx.getName());
        ret.setReadDatabase(SOAPUtils.soapDatabase2Database(ctx.getReadDatabase()));
        ret.setUsedQuota(ctx.getUsedQuota());
        ret.setWriteDatabase(SOAPUtils.soapDatabase2Database(ctx.getWriteDatabase()));
        OXContextExtensionImpl ctxext = new OXContextExtensionImpl();
        ctxext.setCustomid(ctx.getCustomid());
        ctxext.setOwner(ctx.getOwner());
        ctxext.setRestriction(ctx.getRestriction());
        ctxext.setSid(ctx.getSid());
        ret.addExtension(ctxext);
        return ret;
    }


}

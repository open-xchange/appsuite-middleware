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

package com.openexchange.appstore.json.converter;

import java.util.Iterator;
import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.appstore.Application;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ManifestResultConverter}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ManifestResultConverter implements ResultConverter {

    @Override
    public String getInputFormat() {
        return "application";
    }

    @Override
    public String getOutputFormat() {
        return "text/plain";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        List<Application> apps = (List<Application>) result.getResultObject();
        StringBuilder sb = new StringBuilder();
        StringBuilder installed = new StringBuilder();
        Iterator<Application> iter = apps.iterator();
        while (iter.hasNext()) {
            Application application = iter.next();
            // append manifest code
            sb.append("// manifest for " + application.getName());
            sb.append(System.getProperty("line.separator"));
            sb.append(application.getManifest());
            sb.append(System.getProperty("line.separator"));
            // append register code
            sb.append("ox.registry.register(\"" + application.getName() + "\", \"" + application.getRelativePath() + "\");");
            sb.append(System.getProperty("line.separator"));
            // add to installed apps
            installed.append("\"" + application.getName() + "\"");
            if (iter.hasNext()) {
                installed.append(", ");
            }
        }
        // finally, add installed apps
        sb.append(System.getProperty("line.separator"));
        sb.append("ox.apps.setInstalled([" + installed.toString() + "]);");
        sb.append(System.getProperty("line.separator"));
        // build result
        result.setResultObject(sb.toString(), getOutputFormat());
    }

}

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

package com.openexchange.test.resourcecache.actions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.framework.Header;
import com.openexchange.ajax.framework.Header.SimpleHeader;


/**
 * {@link UploadRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UploadRequest extends AbstractResourceCacheRequest<UploadResponse> {

    private final List<FileParameter> files = new ArrayList<FileParameter>();

    private final boolean waitForAlignment;

    private String resourceId = null;

    public UploadRequest() {
        super("upload");
        this.waitForAlignment = false;
    }

    public UploadRequest(boolean waitForAlignment) {
        super("upload");
        this.waitForAlignment = waitForAlignment;
    }

    public void addFile(String fileName, String mimeType, InputStream is) {
        files.add(new FileParameter("resource_" + files.size(), fileName, is, mimeType));
    }

    public void setResourceId(String id) {
        resourceId = id;
    }

    @Override
    public Method getMethod() {
        return Method.POST;
    }

    @Override
    public Parameter[] getAdditionalParameters() {
        List<Parameter> allParams = new LinkedList<Parameter>();
        allParams.addAll(files);
        allParams.add(new URLParameter("waitForAlignment", Boolean.toString(waitForAlignment)));
        if (resourceId != null) {
            allParams.add(new URLParameter("id", resourceId));
        }

        return allParams.toArray(new Parameter[0]);
    }

    @Override
    public UploadResponseParser getParser() {
        return new UploadResponseParser(true);
    }

    @Override
    public Header[] getHeaders() {
        return new Header[] { new SimpleHeader("Content-Type", "multipart/form-data") };
    }

}

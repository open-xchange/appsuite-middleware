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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.xing.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Header;
import com.openexchange.configuration.XingConfig;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractXingRequest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractXingRequest<T extends AbstractAJAXResponse> implements AJAXRequest<T> {

    public static final String XING_URL = "/ajax/xing";

    protected final boolean failOnError;

    protected final String token;

    protected final String secret;

    /**
     * Initializes a new {@link AbstractXingRequest}.
     */
    public AbstractXingRequest(final boolean foe) {
        this(foe, XingTestAccount.DIMITRI_BRONKOWITSCH);
    }

    /**
     * Initializes a new {@link AbstractXingRequest}.
     * 
     * @param foe
     * @param testAccount The identifier of the xingTestAccount
     */
    public AbstractXingRequest(final boolean foe, final XingTestAccount testAccount) {
        failOnError = foe;
        try {
            XingConfig.init();
        } catch (final OXException ex) {
            ex.printStackTrace();
        }

        token = XingConfig.getProperty(testAccount.getToken());
        secret = XingConfig.getProperty(testAccount.getSecret());
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajax.framework.AJAXRequest#getServletPath()
     */
    @Override
    public String getServletPath() {
        return XING_URL;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.ajax.framework.AJAXRequest#getHeaders()
     */
    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    public final Parameter[] getParameters() throws IOException, JSONException {
        final List<Parameter> params = new ArrayList<Parameter>();

        if (token != null) {
            params.add(new URLParameter("testToken", token));
        }

        if (secret != null) {
            params.add(new URLParameter("testSecret", secret));
        }
        setMoreParameters(params);
        return params.toArray(new Parameter[params.size()]);
    }

    /**
     * @param params
     */
    protected abstract void setMoreParameters(List<com.openexchange.ajax.framework.AJAXRequest.Parameter> params);

}

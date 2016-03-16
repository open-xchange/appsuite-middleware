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

package com.openexchange.find.json.converters;

import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.find.facet.ComplexDisplayItem;
import com.openexchange.find.facet.DisplayItemVisitor;
import com.openexchange.find.facet.FormattableDisplayItem;
import com.openexchange.find.facet.NoDisplayItem;
import com.openexchange.find.facet.SimpleDisplayItem;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.util.Pair;
import com.openexchange.tools.session.ServerSession;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class JSONDisplayItemVisitor implements DisplayItemVisitor {

    private static final Logger LOG = LoggerFactory.getLogger(JSONDisplayItemVisitor.class);

    private final StringTranslator translator;

    private final ServerSession session;

    private final Locale locale;

    private Pair<String, Object> result;

    private JSONException exception = null;

    public JSONDisplayItemVisitor(final StringTranslator translator, final ServerSession session) {
        super();
        this.translator = translator;
        this.session = session;
        User user = session.getUser();
        locale = user.getLocale();
    }

    @Override
    public void visit(SimpleDisplayItem item) {
        String displayName;
        if (item.isLocalizable()) {
            displayName = translator.translate(locale, item.getDisplayName());
        } else {
            displayName = item.getDisplayName();
        }

        result = new Pair<String, Object>("name", displayName);
    }

    @Override
    public void visit(ComplexDisplayItem item) {
        JSONObject jItem = new JSONObject();
        result = new Pair<String, Object>("item", jItem);
        try {
            jItem.put("name", item.getDisplayName());
            jItem.put("detail", item.getDetail());
            if (item.hasImageData()) {
                try {
                    String imageUrl = item.getImageDataSource().generateUrl(item.getImageLocation(), session);
                    jItem.put("image_url", imageUrl);
                } catch (OXException e) {
                    LOG.warn("Could not generate image url for ComplexDisplayItem.", e);
                }
            }
        } catch (JSONException e) {
            exception = e;
        }
    }

    @Override
    public void visit(FormattableDisplayItem item) {
        JSONObject jItem = new JSONObject();
        result = new Pair<String, Object>("item", jItem);
        try {
            jItem.put("name", item.getArgument());
            jItem.put("detail", translator.translate(locale, item.getSuffix()));
        } catch (JSONException e) {
            exception = e;
        }
    }

    @Override
    public void visit(NoDisplayItem noDisplayItem) {
        result = null;
    }

    /**
     * Appends the display name to the given JSON object.
     * This value is only valid if DisplayItem.accept(visitor) has been called.
     */
    public void appendResult(JSONObject json) throws JSONException {
        if (exception != null) {
            throw exception;
        }

        if (result != null) {
            json.put(result.getFirst(), result.getSecond());
        }
    }
}

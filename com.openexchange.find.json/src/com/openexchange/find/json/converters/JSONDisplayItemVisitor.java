/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.find.json.converters;

import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.find.facet.ComplexDisplayItem;
import com.openexchange.find.facet.DisplayItemVisitor;
import com.openexchange.find.facet.FormattableDisplayItem;
import com.openexchange.find.facet.NoDisplayItem;
import com.openexchange.find.facet.SimpleDisplayItem;
import com.openexchange.java.util.Pair;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class JSONDisplayItemVisitor implements DisplayItemVisitor {

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
                String imageUrl = item.getImageUrl(session);
                jItem.put("image_url", imageUrl);
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

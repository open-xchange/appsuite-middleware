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

package com.openexchange.ajax.printing.converter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * {@link AssetHelper}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AssetHelper {
    /**
     * Initializes a new {@link AssetHelper}.
     * @param string
     */
    public AssetHelper(String prefix) {
        super();
        this.prefix = prefix;
    }

    private final String prefix;
    
    public String getLinkTo(String asset) {
        try {
            return prefix + URLEncoder.encode(asset, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return prefix + asset;
        }
    }
    
    public String includeJS(String asset) {
        return "<script type=\"text/javascript\" src=\"" + getLinkTo(asset) + "\"></script>";
    }
    
    public String includeCSS(String asset) {
        return "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + getLinkTo(asset) + "\">";
    }
}

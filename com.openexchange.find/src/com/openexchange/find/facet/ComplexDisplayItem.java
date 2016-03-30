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

package com.openexchange.find.facet;

import com.openexchange.image.ImageDataSource;
import com.openexchange.image.ImageLocation;

/**
 * {@link ComplexDisplayItem}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class ComplexDisplayItem implements DisplayItem {

    private final String displayName;

    private final String detail;

    private ImageLocation imageLocation;

    private ImageDataSource imageDataSource;

    /**
     * Initializes a new {@link ComplexDisplayItem}.
     *
     * @param displayName
     * @param name
     * @param detail
     * @param imageUrl
     */
    public ComplexDisplayItem(String displayName, String detail) {
        super();
        this.displayName = displayName;
        this.detail = detail;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public String getDetail() {
        return detail;
    }

    public ImageLocation getImageLocation() {
        return imageLocation;
    }

    public ImageDataSource getImageDataSource() {
        return imageDataSource;
    }

    public boolean hasImageData() {
        return imageDataSource != null && imageLocation != null;
    }

    public void setImageData(ImageDataSource imageDataSource, ImageLocation imageLocation) {
        if (imageDataSource != null && imageLocation != null) {
            this.imageDataSource = imageDataSource;
            this.imageLocation = imageLocation;
        }
    }

    @Override
    public void accept(DisplayItemVisitor visitor) {
        visitor.visit(this);
    }

}

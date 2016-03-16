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

package com.openexchange.subscribe.crawler.internal;

/**
 * This a page part to unequivocally identify information (e.g. a contact\u00b4s name) in a webpages sourcecode. To identify a particular bit of
 * information two factors are used: - Its place in a sequence (->PagePartSequence) (e.g. in the page\u00b4s sourcecode the last name is listed
 * after the first name) - The sourcecode immediately surrounding it. There are two kinds of page parts (identified by their TYPE-Integer):
 * - Fillers, only used to make the sequence unequivocal and containing a single-capture-group regex identifiyng them - Infos, containing a
 * three-capture-group regex (immediately before, relevant part, immediately after) and the type of the info (e.g. Contact.LAST_NAME)
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */

public class PagePart {

    public static int FILLER = 0;

    public static int INFO = 1;

    private int type;

    private String regex;

    private String typeOfInfo;

    private int addInfo;

    private boolean keepStringAfterMatching;

    public PagePart() {

    }

    public PagePart(final String regex, final String typeOfInfo) {
        type = INFO;
        this.regex = regex;
        this.typeOfInfo = typeOfInfo;
        this.addInfo = 0;
    }

    public PagePart(final String regex, final String typeOfInfo, boolean keepStringAfterMatching) {
        type = INFO;
        this.regex = regex;
        this.typeOfInfo = typeOfInfo;
        this.addInfo = 0;
        this.keepStringAfterMatching = keepStringAfterMatching;
    }

    public PagePart(final String regex, final String typeOfInfo, int addInfo) {
        this.addInfo = addInfo;
        type = INFO;
        this.regex = regex;
        this.typeOfInfo = typeOfInfo;
    }

    public PagePart(final String regex) {
        type = FILLER;
        this.regex = regex;
    }

    public int getType() {
        return type;
    }

    public void setType(final int type) {
        this.type = type;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(final String regex) {
        this.regex = regex;
    }

    public String getTypeOfInfo() {
        return typeOfInfo;
    }

    public void setTypeOfInfo(final String typeOfInfo) {
        this.typeOfInfo = typeOfInfo;
    }


    public int getAddInfo() {
        return addInfo;
    }


    public void setAddInfo(int addInfo) {
        this.addInfo = addInfo;
    }


    public boolean isKeepStringAfterMatching() {
        return keepStringAfterMatching;
    }


    public void setKeepStringAfterMatching(boolean keepStringAfterMatching) {
        this.keepStringAfterMatching = keepStringAfterMatching;
    }

}

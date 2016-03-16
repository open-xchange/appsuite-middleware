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

package com.openexchange.report.client.container;

public class ClientLoginCount {

    private String usmeas;

    private String usmeasYear;

    private String olox2;

    private String mobileapp;

    private String caldav;

    private String carddav;

    /**
     * @return the mobileapp
     */
    public final String getMobileapp() {
        return mobileapp;
    }

    /**
     * @param mobileapp the mobileapp to set
     */
    public final void setMobileapp(String mobileapp) {
        this.mobileapp = mobileapp;
    }

    /**
     * @return the caldav
     */
    public final String getCaldav() {
        return caldav;
    }

    /**
     * @param caldav the caldav to set
     */
    public final void setCaldav(String caldav) {
        this.caldav = caldav;
    }

    /**
     * @return the carddav
     */
    public final String getCarddav() {
        return carddav;
    }

    /**
     * @param carddav the carddav to set
     */
    public final void setCarddav(String carddav) {
        this.carddav = carddav;
    }

    public String getUsmeas() {
        return usmeas;
    }

    public void setUsmeas(final String usmeas) {
        this.usmeas = usmeas;
    }

    public String getOlox2() {
        return olox2;
    }

    public void setOlox2(final String usmjson) {
        this.olox2 = usmjson;
    }

    public void setUsmeasYear(String usmeasYear) {
        this.usmeasYear = usmeasYear;
    }

    public String getUsmeasYear() {
        return usmeasYear;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ClientLoginCount [usmeas=" + usmeas + ", usmeasYear=" + usmeasYear + ", olox2=" + olox2 + ", mobileapp=" + mobileapp + ", caldav=" + caldav + ", carddav=" + carddav + "]";
    }

}

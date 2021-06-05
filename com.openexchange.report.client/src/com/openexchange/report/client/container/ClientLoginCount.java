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

    @Override
    public String toString() {
        return "ClientLoginCount [usmeas=" + usmeas + ", usmeasYear=" + usmeasYear + ", olox2=" + olox2 + ", mobileapp=" + mobileapp + ", caldav=" + caldav + ", carddav=" + carddav + "]";
    }

}

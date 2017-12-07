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

package com.openexchange.ajax.oauth.provider;

import static org.junit.Assert.assertNotNull;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.image.ImageRequest;
import com.openexchange.ajax.image.ImageResponse;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;

/**
 * {@link ReadContactTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class ReadContactTest extends AbstractOAuthTest {

    public ReadContactTest() throws OXException {
        super(Scope.newInstance(ContactActionFactory.OAUTH_READ_SCOPE, ContactActionFactory.OAUTH_WRITE_SCOPE));
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testContactImageRetrieval() throws Exception {
        byte[] image;
        {
            String base64Image = "iVBORw0KGgoAAAANSUhEUgAAACMAAAAjCAYAAAAe2bNZAAAKvmlDQ1BJQ0MgUHJvZmlsZQAASImV\n" +
                "lwdUU1kax+976SGhBUKREnqT3kF6DaAgHUQlJBBCCSGFZkNlcARHFBERUAZ0qAqOhTaoiAXbIGCv\n" +
                "AzIoqONgAQsq84Al7Oye3T37P+ee98v37vve99137zn/AEAaY/B4KbA0AKlcIT/Yx50WGRVNww0D\n" +
                "CMAABywBnsEU8NyCggIAooXr3zV1B5mN6KbxbK5/v/9fJcOKFzABgIIQjmMJmKkIn0RGH5PHFwKA\n" +
                "ykPiWplC3izXIyzHRwpEuGuW2fPcP8tx8/z73JzQYA+EPwKAJzEYfDYAJDQSp2Uw2UgekjbCZlwW\n" +
                "h4twKMLOzEQGC+EShJempqbNcjfC+nH/lIf9t5xx4pwMBlvM873MCe/JEfBSGNn/53L8b6WmiBbe\n" +
                "oYkMUiLfNxi56iJrVp+c5i9mbtyKwAXmsObmz3GiyDdsgZkCj+gFZjE8/RdYlBzmtsAM/uKzHCE9\n" +
                "dIH5acHi/NyUFQHi/PF0MccLvEIWOIHjTV/gnMTQiAXO4ISvWGBBcoj/4hwPcZwvChbXnMD3FveY\n" +
                "KlisjclYfJcwMdR3sYZIcT2seE8vcZwbJp7PE7qLc/JSghbrT/ERxwUZIeJnhcgGW+Akhl/QYp4g\n" +
                "8fqAUJAIRIALWCAe8EEcSAMpQAhowBNwgADwkF8MgGwPYXyWcLYJjzReNp/DThTS3JBTFE+jc5km\n" +
                "S2kWZua2AMyeyflP/o46d9Yg6tXFWHo3APYFSJC9GGNoAdDxDADK1GJM6y2yXXYBcLqfKeJnzMdm\n" +
                "ty3AACKQAnJACagBLaAPjIEFsAGOwBV4AT8QiHQSBdYAJtJPKtJJJlgPNoN8UAh2gb2gHFSBQ6Ae\n" +
                "HAXHQRvoAufAJXAN9IPb4CEYAqPgJZgAU2AagiAcRIYokBKkDulARpAFZAc5Q15QABQMRUGxEBvi\n" +
                "QiJoPbQVKoSKoXKoGmqAfoY6oHPQFWgAug8NQ+PQW+gzjIJJsBysCuvCprAd7Ab7w6HwapgNp8M5\n" +
                "cB68Ey6Da+AjcCt8Dr4G34aH4JfwJAqgJFBUlAbKGGWH8kAFoqJRCSg+aiOqAFWKqkE1ozpRvaib\n" +
                "qCHUK9QnNBZNQdPQxmhHtC86DM1Ep6M3onegy9H16Fb0BfRN9DB6Av0NQ8aoYIwwDhg6JhLDxmRi\n" +
                "8jGlmFrMKcxFzG3MKGYKi8VSsXpYW6wvNgqbhF2H3YE9gG3BdmMHsCPYSRwOp4QzwjnhAnEMnBCX\n" +
                "j9uPO4I7ixvEjeI+4iXw6ngLvDc+Gs/Fb8GX4hvxZ/CD+Of4aYI0QYfgQAgksAjZhCLCYUIn4QZh\n" +
                "lDBNlCHqEZ2IocQk4mZiGbGZeJH4iPhOQkJCU8JeYqUERyJXokzimMRliWGJTyRZkiHJgxRDEpF2\n" +
                "kupI3aT7pHdkMlmX7EqOJgvJO8kN5PPkJ+SPkhRJE0m6JEtyk2SFZKvkoORrKYKUjpSb1BqpHKlS\n" +
                "qRNSN6ReSROkdaU9pBnSG6UrpDuk70pPylBkzGUCZVJldsg0ylyRGZPFyerKesmyZPNkD8melx2h\n" +
                "oChaFA8Kk7KVcphykTIqh5XTk6PLJckVyh2V65ObkJeVt5IPl8+Sr5A/LT9ERVF1qXRqCrWIepx6\n" +
                "h/pZQVXBTSFeYbtCs8KgwgfFJYquivGKBYotircVPyvRlLyUkpV2K7UpPVZGKxsqr1TOVD6ofFH5\n" +
                "1RK5JY5LmEsKlhxf8kAFVjFUCVZZp3JI5brKpKqaqo8qT3W/6nnVV2pUNVe1JLUStTNq4+oUdWd1\n" +
                "jnqJ+ln1FzR5mhsthVZGu0Cb0FDR8NUQaVRr9GlMa+pphmlu0WzRfKxF1LLTStAq0erRmtBW116u\n" +
                "vV67SfuBDkHHTidRZ59Or84HXT3dCN1tum26Y3qKenS9HL0mvUf6ZH0X/XT9Gv1bBlgDO4NkgwMG\n" +
                "/YawobVhomGF4Q0j2MjGiGN0wGhgKWap/VLu0pqld41Jxm7GGcZNxsMmVJMAky0mbSavTbVNo013\n" +
                "m/aafjOzNksxO2z20FzW3M98i3mn+VsLQwumRYXFLUuypbflJst2yzdWRlbxVget7llTrJdbb7Pu\n" +
                "sf5qY2vDt2m2GbfVto21rbS9aydnF2S3w+6yPcbe3X6TfZf9JwcbB6HDcYc/HY0dkx0bHceW6S2L\n" +
                "X3Z42YiTphPDqdppyJnmHOv8o/OQi4YLw6XG5amrlivLtdb1uZuBW5LbEbfX7mbufPdT7h88HDw2\n" +
                "eHR7ojx9PAs8+7xkvcK8yr2eeGt6s72bvCd8rH3W+XT7Ynz9fXf73qWr0pn0BvqEn63fBr8L/iT/\n" +
                "EP9y/6cBhgH8gM7l8HK/5XuWP1qhs4K7oi0QBNID9wQ+DtILSg/6ZSV2ZdDKipXPgs2D1wf3hlBC\n" +
                "1oY0hkyFuocWhT4M0w8ThfWES4XHhDeEf4jwjCiOGIo0jdwQeS1KOYoT1R6Niw6Pro2eXOW1au+q\n" +
                "0RjrmPyYO6v1VmetvrJGeU3KmtNrpdYy1p6IxcRGxDbGfmEEMmoYk3H0uMq4CaYHcx/zJcuVVcIa\n" +
                "j3eKL45/nuCUUJwwxnZi72GPJ7oklia+4nhwyjlvknyTqpI+JAcm1yXPpESktKTiU2NTO7iy3GTu\n" +
                "hTS1tKy0AZ4RL583lO6Qvjd9gu/PrxVAgtWCdqEcYn6ui/RF34mGM5wzKjI+ZoZnnsiSyeJmXc82\n" +
                "zN6e/TzHO+endeh1zHU96zXWb14/vMFtQ/VGaGPcxp5NWpvyNo3m+uTWbyZuTt786xazLcVb3m+N\n" +
                "2NqZp5qXmzfync93TfmS+fz8u9sct1V9j/6e833fdsvt+7d/K2AVXC00Kywt/LKDuePqD+Y/lP0w\n" +
                "szNhZ1+RTdHBXdhd3F13drvsri+WKc4pHtmzfE9rCa2koOT93rV7r5RalVbtI+4T7RsqCyhr36+9\n" +
                "f9f+L+WJ5bcr3CtaKlUqt1d+OMA6MHjQ9WBzlWpVYdXnHzk/3qv2qW6t0a0pPYQ9lHHo2eHww70/\n" +
                "2f3UUKtcW1j7tY5bN1QfXH+hwbahoVGlsagJbhI1jR+JOdJ/1PNoe7Nxc3ULtaXwGDgmOvbi59if\n" +
                "7xz3P95zwu5E80mdk5WnKKcKWqHW7NaJtsS2ofao9oEOv46eTsfOU7+Y/FLXpdFVcVr+dNEZ4pm8\n" +
                "MzNnc85OdvO6X51jnxvpWdvz8Hzk+VsXVl7ou+h/8fIl70vne916z152utx1xeFKx1W7q23XbK61\n" +
                "Xre+fupX619P9dn0td6wvdHeb9/fObBs4Mygy+C5m543L92i37p2e8XtgTthd+7djbk7dI91b+x+\n" +
                "yv03DzIeTD/MfYR5VPBY+nHpE5UnNb8Z/NYyZDN0ethz+PrTkKcPR5gjL38X/P5lNO8Z+Vnpc/Xn\n" +
                "DWMWY13j3uP9L1a9GH3Jezn9Kv8PmT8qX+u/Pvmn65/XJyInRt/w38y83fFO6V3de6v3PZNBk0+m\n" +
                "UqemPxR8VPpY/8nuU+/niM/PpzO/4L6UfTX42vnN/9ujmdSZGR6Dz5izAihkwAkJALytA4AchXgH\n" +
                "xFcTJec985ygeZ8/R+A/8byvnpMNAHWuAITlAhCAeJSDyNDJnffWs5Yp1BXAlpbi8Q8JEiwt5nOR\n" +
                "EOeJ+Tgz804VAFwnAF/5MzPTB2Zmvh5Gir0PQHf6vFefFRb5B1OsJ2+ucv/qjAL4V/0F7CAMHBwt\n" +
                "hc0AAAGbaVRYdFhNTDpjb20uYWRvYmUueG1wAAAAAAA8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2Jl\n" +
                "Om5zOm1ldGEvIiB4OnhtcHRrPSJYTVAgQ29yZSA1LjQuMCI+CiAgIDxyZGY6UkRGIHhtbG5zOnJk\n" +
                "Zj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+CiAgICAgIDxy\n" +
                "ZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiCiAgICAgICAgICAgIHhtbG5zOmV4aWY9Imh0dHA6\n" +
                "Ly9ucy5hZG9iZS5jb20vZXhpZi8xLjAvIj4KICAgICAgICAgPGV4aWY6UGl4ZWxYRGltZW5zaW9u\n" +
                "PjM1PC9leGlmOlBpeGVsWERpbWVuc2lvbj4KICAgICAgICAgPGV4aWY6UGl4ZWxZRGltZW5zaW9u\n" +
                "PjM1PC9leGlmOlBpeGVsWURpbWVuc2lvbj4KICAgICAgPC9yZGY6RGVzY3JpcHRpb24+CiAgIDwv\n" +
                "cmRmOlJERj4KPC94OnhtcG1ldGE+CqtiM/sAAAHMSURBVFgJY/wPBAyDBDANEneAnTHqGFyxwYJL\n" +
                "gnri6EmSEafRNHPM359fGB4fmcvw6fEFhs/ProIdwCulzcAna8Aga5PMwMzOg+EoRlrkpvf3jjHc\n" +
                "WF3G8OPDUwwLQQIcAtIMGqFdDIJKVijyVHcMyCEX5sQwMMBLDPRogUYbIyODQcoSFAdR1TF/f35l\n" +
                "ODXBHR4i7PySDCpe1QwCimbgEPhw/xTDnW2tDD8/PgfzQSFkVrATGGXcYD5Vs/bjI3OQHCLBYJq3\n" +
                "jUFMz5uBjVcUjEFskBg7vwTYclA0gvTAAFUdA0qsMKDiVcPAyiUA48JpkBhIDgaQ9VDVMbBcA7II\n" +
                "FjUwS5FpZDlkPVR1zK/Pr+F2gqIGF0CWQ9ZDVcfgspxYccKFHjyLEmskVB1MHzALEwvwOgaU0u/v\n" +
                "mcAAyrKkgv1VSuAsq+hSACxxU4jSjjeayHUIzGaQJ0BmYAPCGi4MDMBQA9NQBXhDhpwQQbcYlxl6\n" +
                "8bPRlTLgDRkM1TQWGHUMrgAeDZnRkMEVArjE8aYZWKMHl2ZixEkxA69jQEU5Mwew4QyqX8jAIL0g\n" +
                "M4gFVG12EmspLnV4QwaXJlqJjzoGV8iOhsxoyOAKAVziADt/ifvTHt/1AAAAAElFTkSuQmCC";

            image = Base64.decodeBase64(base64Image);
        }

        Contact contact = new Contact();
        contact.setTitle("Herr");
        contact.setSurName("Meier");
        contact.setGivenName("Herbert");
        contact.setDisplayName("Herbert Meier");
        contact.setStreetBusiness("Franz-Meier Weg 17");
        contact.setCityBusiness("Test Stadt");
        contact.setStateBusiness("NRW");
        contact.setCountryBusiness("Deutschland");
        contact.setTelephoneBusiness1("+49112233445566");
        contact.setCompany("Internal Test AG");
        contact.setEmail1("hebert.meier@open-xchange.com");
        contact.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        contact.setFileAs("I'm the file_as field of Herbert Meier");
        contact.setImage1(image);

        InsertRequest insertContactReq = new InsertRequest(contact);
        InsertResponse insertContactResp = getClient().execute(insertContactReq);
        try {
            insertContactResp.fillObject(contact);

            com.openexchange.ajax.contact.action.GetRequest getContactReq = new com.openexchange.ajax.contact.action.GetRequest(contact.getParentFolderID(), contact.getObjectID(), getClient().getValues().getTimeZone());
            com.openexchange.ajax.contact.action.GetResponse getContactResp = getClient().execute(getContactReq);

            JSONObject jContact = (JSONObject) getContactResp.getResponse().getData();
            String imageUrl = jContact.getString(ContactFields.IMAGE1_URL); // e.g. "/ajax/image/user/picture?id=273&timestamp=1468497596841"

            ImageRequest imageRequest = ImageRequest.parseFrom(imageUrl);
            ImageResponse imageResponse = getClient().execute(imageRequest);

            assertNotNull(imageResponse.getImage());
        } finally {
            com.openexchange.ajax.contact.action.DeleteRequest deleteContactReq = new com.openexchange.ajax.contact.action.DeleteRequest(contact, false);
            getClient().execute(deleteContactReq);
        }
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

}

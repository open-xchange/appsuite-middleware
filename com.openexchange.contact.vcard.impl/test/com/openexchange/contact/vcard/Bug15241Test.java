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

package com.openexchange.contact.vcard;

import java.awt.image.BufferedImage;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Streams;

/**
 * {@link Bug15241Test}
 *
 * Unable to import vcards from Apple Address Book
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug15241Test extends VCardTest {

    /**
     * Initializes a new {@link Bug15241Test}.
     */
    public Bug15241Test() {
        super();
    }

    public void testImportVCard() throws Exception {
        /*
         * import vCard
         */
        String vCard =
            "BEGIN:VCARD\n"
            +"VERSION:3.0\n"
            +"N:Anmann;Rohrdreas;;;\n"
            +"FN:Andreas Rohrmann\n"
            +"EMAIL;type=INTERNET;type=WORK;type=pref:rohrdreas@anmann.com\n"
            +"EMAIL;type=INTERNET;type=HOME:rohrdreas.anmann@piratenpartei-rp.ru\n"
            +"BDAY;value=date:1954-14-11\n"
            +"PHOTO;BASE64:\n"
            +"  /9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAUDBBAQDxAPDRAQDxAPEQ0RDw4PDg0QDw0QDRAQDQ0Q\n"
            +"  EA0ODxAPDQ8ODhANDxUODhESExQTDg8XGBYSGBASFBIBBQUFCAcIDwkJDx0UEhQdFBcfFxcXFxYa\n"
            +"  FBQUGBQXFBcVFxwUHR4VFxQUFBgXFBwUHBQUFBwUFBQUFhQUFBcUFP/AABEIAGAAYAMBIgACEQED\n"
            +"  EQH/xAAcAAACAgMBAQAAAAAAAAAAAAAFBgQHAgMIAQD/xAA4EAABAgQDBQUGBgMBAQAAAAABAhEA\n"
            +"  AwQhBRIxBgdBUWETInGRoTJCgbHB8BVSYnKC0RQj4fEI/8QAGQEAAwEBAQAAAAAAAAAAAAAAAwQF\n"
            +"  AgEA/8QAJhEAAgICAgIBAwUAAAAAAAAAAQIAEQMSBCExQfATUZEiMkJhcf/aAAwDAQACEQMRAD8A\n"
            +"  ZtrcO7aRMl8VDu/uF0+tvjHONQlixsRZuRGsdMJXFH72sM7OoJGkwZx4myvW/wAYVxN3H2T3Ptgc\n"
            +"  QCAtLpSZliVGzAPpZuN31IESZOMAKXmWlAsAUJfMALB7v1LveEvDqVUxWRF1EFhzygqbxIGnOIyp\n"
            +"  h4wT3OKa9S4t2OPoUoy0SQksSqckAP8Au5PoAC3SGTaPaCXKDrJ6AfdoXNzVSk06gPaStWb+QBSe\n"
            +"  toB7W4euYss7AkO1vCBO4UwyoSIdn70ViWUyUgE6KNyH5DSFudvOq9FTCOgy/wBQDn7PL4An0jUv\n"
            +"  ZxbXEY3S7mjhfxUa8I3lzkqBmErSOvzIh0wLfPLJCJiMt2zZrN6xQ1bJWgsp2jTMllgUm0E1U9wL\n"
            +"  Ag1Oy8GxRE1OeWQoPqC4PxjetcU5uL2sShAkrLElhfiflFxTxAW6MxEpMyE3e1g3aye0T7Upz4oP\n"
            +"  teXtecNJWxjyaQQQrQggjmCGMcDe43pfU54wWsMubLWLFK0nyIg5vYoBLqVFNkzAFjleyvUQBx2i\n"
            +"  MuatB91RHwGh+IYwx7Z1XbUtNOPtIzSlnqACH8ReGi3YMCq9EQ9uMmA9sl2J7Mv07w9PrD9jS0lK\n"
            +"  ZaQyU+p5k8SYr3c9LCJc2YdVEJHNk3PqYaDUv/7CPIa2qU+JjGtmTpSAOEZTpKSIgFRjCZUtAAaj\n"
            +"  RUGKm8HD05HGsV7Im5bfd4d94dY6Q3A3ivBNDw7g7WSeUP1Q5hE/KX4iOsNi6gzKWSslypAc+Fo4\n"
            +"  8oKh3jsTdZStQ0/7AfMmNZvEUHcU8Vkss6c/ONUpP20F9ppDEH4eUBJkxhCuNrWUytGVTvooMs5K\n"
            +"  xpMQH8UFj5hoWcKqHkTpR/RNSOqDlX5oUT/GH/e1IMyUlQB/1kvp7KmBPwLecJ+w+z6lq7RfdlJC\n"
            +"  gpRLBbhso5voTyhtXATuB+mS9D3M8CnTESM4QSEqsb3CvneDmIYyoISoAhw/h4wyUcsTEJQWYue7\n"
            +"  oADZuQAiNtFQhXdAACQAB0hRntrqVFwlBVxWoNrphN1BvBoOJxMKFteIhfTsmoqsDfkbX5iDFDsm\n"
            +"  ZbEr04f9jjMpnFDe4G2mpCpJUOEV+KdRUyQ5PCLdxRggjpCajC1JQZgGttb30aC4cmo7i+fj7sJE\n"
            +"  wzZxYUkZkKKlJBSlTqSVEAOP6juHD6MS5SED3EpT5ACOXdzmDdrVyBkbKrMt+KU95/No6tqjHMmQ\n"
            +"  tFeTgXG1LEXaqn7h6fZhIXMBt9+dotbE6IKcKDgi8AvwhCdEi3SJuPkaio+cWxldTqNISUlLpUCD\n"
            +"  16dB1EIu3yJuXulKUJ9xJAYeH0hy3s40pCsibDTweKt2dpDPqZcpanC5iUlzZib+kOY22Gxm1AWN\n"
            +"  WC56enlLmAntEqUhtcuZuPE2PhED8eRM7qArtCbO/wA9IsDf8UJ7CQmxSFG3upICUJbqz36c4p6j\n"
            +"  WUH/AF5SR1IPrHcR3GxEIzEHqPNPibDvajWB+IYy/GBH+WpXtgpPHrAWtqdWMaVLnjn1EnYrXE2i\n"
            +"  XQV6LBTHLol+PUcYVFVuYgcXidjlSJQABBWrT9I5mDDH/Gor9eiXM6m3GbK9nK/yVgiZNByghskv\n"
            +"  gw/VrFgVSo5i2F381MpCJc9KZ6UJZy6ZhAHddYsW0ch+sEp//wBLvpSAde1UfoBHTxXEl5eRu2xl\n"
            +"  /wBQIhzWhyRsf+dfwAb5x5WYNIlB1kD92p8BqYitjZP3CUBzMZNA3/k5U3ngKmTHDly0VfQLWhRK\n"
            +"  BcHXkR1ixdvq0dpMY2KlN4OYQvxUJSo2DkxSxDqgIXb39ocwSVVV1SJYBmzcqlkksyZScxJJsAAG\n"
            +"  D8SBxiHWYpLvYBXPkRrC/UbQTJCFgEpXPAC2LESncILXGcsojkEwsTqp7Q3j4u3fz59orl5mrV8E\n"
            +"  O4rtE1s2aAM/FVK0jfU5Mgu6ruANALBzz42j7BpOaYkFkh3L8hcwdcH9RV+QT5M8ollFyHURZ9Ix\n"
            +"  mLJJUouTqY+xKcFLUQbFRbw4RHzQymML3FnyluoSpZse/hLl0lx+U2I+PEesQJc2J0iraNsgMDc6\n"
            +"  axffPUrU6alCQAzJ7ttHOYanWF2btyta0hU6ygfZXnKgkOe85Z+OkUdhlZe9/rHmzdQTPfRgbcni\n"
            +"  KeGtbX8/Ep4+TR1oRu27rS5Ie8CdgsPE1a1rv2QCgDo5dj/EA/GM9ucTSEgE35c4DbO7SdlInBI7\n"
            +"  8wkOeCcrebkwRcbNjpfJmzmCv2fEBbT15mTVH8yi3hoPSIkyb5DXqeEaFqa/E+g/7GSEXSnmQ8VF\n"
            +"  UKKkzI1kkzI1LDrzjXQ1SnzCPqqW5YaDU84zAjUxcnUvZn2sw6gv6RN/D3cy1BbcNDARQj2XOILg\n"
            +"  kEcQY1OCSluLGxHAj+4+RNiZLxATRlmWV7q/ofvygdUSikkGxEeuvM5c/9k=\n"
            +"CATEGORIES:Piraten\n"
            +"X-ABUID:0C13691C-A7E6-4DC2-BD53-D6B1A9797509\\:ABPerson\n"
            +"END:VCARD\n"
        ;
        Contact contact = getMapper().importVCard(parse(vCard), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("image/jpeg", contact.getImageContentType());
        assertNotNull(contact.getImage1());
        BufferedImage bufferedImage = javax.imageio.ImageIO.read(Streams.newByteArrayInputStream(contact.getImage1()));
        assertNotNull(bufferedImage);
        assertTrue(0 < bufferedImage.getWidth() && 0 < bufferedImage.getHeight());
    }

}

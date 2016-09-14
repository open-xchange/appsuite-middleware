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

package com.openexchange.dav.carddav.tests;

import static org.junit.Assert.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.Photos;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;
import net.sourceforge.cardme.vcard.arch.EncodingType;
import net.sourceforge.cardme.vcard.types.PhotoType;
import net.sourceforge.cardme.vcard.types.media.ImageMediaType;

/**
 * {@link ImageTest} - Tests contact images via the CardDAV interface
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ImageTest extends CardDAVTest {

	public ImageTest() {
		super();
	}

	@Test
	public void testCroppedImage() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		String syncToken = super.fetchSyncToken();
		/*
		 * create contact
		 */
    	String uid = randomUID();
    	String firstName = "bild";
    	String lastName = "otto";
        String vCard =
        		"BEGIN:VCARD" + "\r\n" +
        		"VERSION:3.0" + "\r\n" +
        		"PRODID:-//Apple Inc.//Address Book 6.1//EN" + "\r\n" +
				"N:" + lastName + ";" + firstName + ";;;" + "\r\n" +
				"FN:" + firstName + " " + lastName + "\r\n" +
        		"PHOTO;ENCODING=b;TYPE=JPEG;X-ABCROP-RECTANGLE=ABClipRect_1&11&11&25&25&ZNtYcAgH/lm2pubKd1ul0g==:" + "\r\n" +
        		" /9j/4AAQSkZJRgABAQAAAQABAAD/4QBARXhpZgAATU0AKgAAAAgAAYdpAAQAAAABAAAAGgAAAA" + "\r\n" +
        		" AAAqACAAQAAAABAAAAMKADAAQAAAABAAAAMAAAAAD/2wBDAAIBAQIBAQICAQICAgICAwUDAwMD" + "\r\n" +
        		" AwYEBAMFBwYHBwcGBgYHCAsJBwgKCAYGCQ0JCgsLDAwMBwkNDg0MDgsMDAv/2wBDAQICAgMCAw" + "\r\n" +
        		" UDAwULCAYICwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsL" + "\r\n" +
        		" Cwv/wAARCAAwADADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8" + "\r\n" +
        		" QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2Jy" + "\r\n" +
        		" ggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhI" + "\r\n" +
        		" WGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl" + "\r\n" +
        		" 5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAg" + "\r\n" +
        		" ECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl" + "\r\n" +
        		" 8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiI" + "\r\n" +
        		" mKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq" + "\r\n" +
        		" 8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD997+/g0qxnutUnitra2jaWaaVwkcSKMszMeAAASSeAB" + "\r\n" +
        		" Xzb8Q/2p/Evxu8KX3/AAyVEttoRXyP+ErvISY7t2YKP7Njz++GMnzm+UZ4BIr5r/4Ltf8ABRjw" + "\r\n" +
        		" /wDDPUdF+BDX+owL4miXUfGL2NvvuF0vdmOzhYsoD3DoVZs/LGrDOWxXT/sJf8FCvD/xN+Fc0m" + "\r\n" +
        		" n6BF4Y8I+FiILNdRu1DxQxpjzGVQEijHzKACw+Q4I6V8ZnmbVJYlYOjUdOP2pJat9ovp5ta9E0" + "\r\n" +
        		" 0z9jyHwzzFcPw4onhvaU3LSLso8t7JvVOTlK6jFXSiuaV00e2eF/2Zn+IehwH9ojW/G2v3MSEl" + "\r\n" +
        		" pNclt7QqQN37qHy9ucc8n+lfEf7b3j28/ZRa90L9hHxD8VtK8T2yiWc3WvTnS4YwxUGO2vA5nH" + "\r\n" +
        		" BCkFU64Lcivtrxv+2XY674EN14Rk0+0hlUNELpXJmXsxRSpRT1APPTPcD4a/a5/bG8UeF/DOp6" + "\r\n" +
        		" jdeFPA3jG3eEQ3dv5V0ktvEpZhPEvmMXZCxJClWxyM4wfBxWDoShfByfOvt68z8k27v8fI+q8N" + "\r\n" +
        		" 1z5qpZrhlOi2l7FtRpXvvJW5bK70vFX3dj1/9jH/AIK7+Lvh18L/AAvJ/wAFHNMWPw9rcKJY+P" + "\r\n" +
        		" 8ATLc/Zom8wxldYgBzasGG0zoDGSOQpNfo1YahBqtjBdaXPDc21zGssM0Th45UYZVlYcEEEEEc" + "\r\n" +
        		" EGv5o/C//BTSWfwHd/DXxxpA1/whrVzc3Fq8d2fPtTNGN0Y+UieLO47XxzIWLHAr9FP+Ddj/AI" + "\r\n" +
        		" KRQfEzS9S/Z78e3l3car4NtXv/AAncXEJ8y60kPh7eRwSC9u0iKpJ5jdRn5a9/hrO62Kf1bE3b" + "\r\n" +
        		" tdO33p/p1Ovxi8H45Fha3EGWU4woxquMoRleKjLWEoq7asmlNbapx0ufGP8AwUs+Inhb4x/8FR" + "\r\n" +
        		" Pjj/wtrUFiXSpU0DT5dyq1ubKBSu3dgZDtKMZAYE5POa8Q0j9ttdf8R6H4b8MRfZPCejW3lmAs" + "\r\n" +
        		" Gk1KSPayPOFxuVXLOEORuO45PTa/4ODvhZ/wp/8A4Kz/ABBhilKW3jq0stf3AFB+9hEbjGefng" + "\r\n" +
        		" bJzzz0zXxD4s1SX4fXVneabe/aLqQtG+6MISMd8d68qrlNWOPqV6jbvJtdr/0tO35fs2TcQUcT" + "\r\n" +
        		" wNhKNCklRhTgnrs9Fe2ju73dk93trzfpsn7XztYfvrwhY48uzPhT+J7V4p8ef+CiOm6BFcReHz" + "\r\n" +
        		" c67qK5VY7YkRIenzykYHTGF3H1r44tvFmq+JVt5PFF5NMoYGK33Yij567ehPuea+xf25f2RofE" + "\r\n" +
        		" uiXnjj4cJFa6nbRedqdoMLHfKBzKnYSDuON2M/e+97H1OnhqlKGJb9++2yatv6337nkYHh55pg" + "\r\n" +
        		" 51aEbSjbRLVp/8Ntu156HyN4c+O91b+KtX1PU9JsbdNUnMjLDGY1sweSqZ6KTyR68194f8EbfH" + "\r\n" +
        		" 1r8IP+Cqvwd1HRJY7k+KHn0G5kV/MjKXdu5AUKecSLFz0yuegr84ZNNh8W2zpdXk9ogdhsVB++" + "\r\n" +
        		" K4yDnByMjj8fp+gv8Awbl+CB8Z/wDgp34Bsrgt5Hw8sLvW1fYWUiKLyYlPPXM+Bk9gecVdXI6l" + "\r\n" +
        		" HFU8RRdkpa+ell+O/wB5WO4rjS4PzLKMztPD+xl7JvlupXu0rarTVN+i0sfqJ/wcNf8ABLxf2z" + "\r\n" +
        		" PgfH45+G9msnj7wWjTWOwMJLxNvz27NnG2QIgUHAEip/fav535/Cv/AAmGox2Oqo2narYu6Sw3" + "\r\n" +
        		" KNFKkijGx1PKsCMYIGDnNf2g3NtHeW8kN5GksUqlHR1DK6kYIIPBBHavzw/4Kb/8EDvBn7Y2/w" + "\r\n" +
        		" ASfCUJ4a8cQjdFfRbVeUhhtSUHCzIBx85DgDh+cV7GYYSVWLlT38t15n89+GXiHhuHv+E3Noc+" + "\r\n" +
        		" GlJN+Wq+a8n09D+fTWPgWV02zg8FTyz6tEjSMpO5bpgy/Ino2C2PXFfVnwN8XaX8b/hL4nT4v6" + "\r\n" +
        		" nqs8WnJF/Z8Ekj26w+WSPMm24ZyW2jaSQAQSC33dT4sf8ABFD9pD4DyLcwWtlrsIkLRNEkto7h" + "\r\n" +
        		" cdFZSD1Gfm71sfCD/gi9+0F8Z7NHuraz0WKJFR8Rz3Ui5B4YRhQBwSPnxxXlwwOZSoRVROSW0r" + "\r\n" +
        		" ary1t5dnv0P7Hw/G/BNKhUx+ExCo0pKF4RlHVw0Vpczs2nLm/m0b63+StX8LaZZXN7b6VpkGt6" + "\r\n" +
        		" xq19utEt4nlldyAgRI+S7MQexYnFfvR/wQH/AOCZEP7G3wVfxj47sYofHXi9RPqGVO61G35LdG" + "\r\n" +
        		" HGyNXdTjIMjyH+Fa0f+CYv/BDzwh+yBotrrvxHX+3vGM8QaTULlALiPJyY0XBFuhHVUO87vmbK" + "\r\n" +
        		" 4r7+t7eOzt44bSNIoolCIiKFVFAwAAOgA7V6mBwlWm3OvK7vp/W33H8o+MXivhOMOXLMloKnho" + "\r\n" +
        		" O7fWb6u+7V+rbb9D//2Q==" + "\r\n" +
        		"REV:2012-05-24T09:51:40Z" + "\r\n" +
				"UID:" + uid + "\r\n" +
        		"END:VCARD"
        	;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCard(uid, vCard));
        /*
         * verify contact on server
         */
        Contact contact = super.getContact(uid);
        super.rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("firstname wrong", firstName, contact.getGivenName());
        assertEquals("lastname wrong", lastName, contact.getSurName());
        assertEquals("wrong numer of images", 1, contact.getNumberOfImages());
        assertNotNull("no image found in contact", contact.getImage1());
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(contact.getImage1()));
        assertEquals("image width wrong", 25, image.getWidth());
        assertEquals("image height wrong", 25, image.getHeight());
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken);
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        assertTrue("PHOTO wrong", 0 < card.getVCard().getPhotos().size());
        byte[] vCardPhoto = card.getVCard().getPhotos().get(0).getPhoto();
        assertNotNull("POHTO wrong", vCardPhoto);
        BufferedImage vCardImage = ImageIO.read(new ByteArrayInputStream(vCardPhoto));
        assertEquals("POHTO width wrong", 25, vCardImage.getWidth());
        assertEquals("POHTO height wrong", 25, vCardImage.getHeight());
	}

	@Test
	public void testNegativeCropOffset() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create contact
		 */
    	String uid = randomUID();
    	String firstName = "bild";
    	String lastName = "wurst";
        String vCard =
        		"BEGIN:VCARD" + "\r\n" +
        		"VERSION:3.0" + "\r\n" +
        		"PRODID:-//Apple Inc.//Address Book 6.1//EN" + "\r\n" +
				"N:" + lastName + ";" + firstName + ";;;" + "\r\n" +
				"FN:" + firstName + " " + lastName + "\r\n" +
				"PHOTO;ENCODING=b;TYPE=JPEG;X-ABCROP-RECTANGLE=ABClipRect_1&-76&-76&200&200&XKZcdOASW3junIR92qq6RA==:" + "\r\n" +
				" /9j/4AAQSkZJRgABAQAAAQABAAD/4QBARXhpZgAATU0AKgAAAAgAAYdpAAQAAAABAAAAGgAAAA" + "\r\n" +
				" AAAqACAAQAAAABAAAAMKADAAQAAAABAAAAMAAAAAD/2wBDAAIBAQIBAQICAQICAgICAwUDAwMD" + "\r\n" +
				" AwYEBAMFBwYHBwcGBgYHCAsJBwgKCAYGCQ0JCgsLDAwMBwkNDg0MDgsMDAv/2wBDAQICAgMCAw" + "\r\n" +
				" UDAwULCAYICwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsL" + "\r\n" +
				" Cwv/wAARCAAwADADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8" + "\r\n" +
				" QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2Jy" + "\r\n" +
				" ggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhI" + "\r\n" +
				" WGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl" + "\r\n" +
				" 5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAg" + "\r\n" +
				" ECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl" + "\r\n" +
				" 8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiI" + "\r\n" +
				" mKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq" + "\r\n" +
				" 8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD96/Fvi3TPAfhm+1nxnf2umaVpkLXF1d3MgjigjUZLMx" + "\r\n" +
				" 6Cvzp1X/gpT8aP+Cj3jTWPDH/BMHTdM8HeBdKmks9S+JHiGEyB2UHelhBg73CEPjaxUYJZMivD" + "\r\n" +
				" v+DkP9ufV/GnjGy+AXwgvjFpdl5V34nntyN01w/+rti4JwqI+9lIHzMAQdor70/4JofBHwN4D+" + "\r\n" +
				" COj2nwWvr+fRPDtgNG+wXVssRgmKo8sjMnEryFi5cdTIScE4H6vhOGqPC/D9LiHMaKqVa7fsoS" + "\r\n" +
				" TcIxVvfmtpOV1yRl7ltZc11E+VebRzXMKmXUKvKqfxWdpN9k+iXVrW+mi1fgWh/8EgtD8X3X9o" + "\r\n" +
				" ftM/tD/Gzx3rlwwkuGttTi0uzaTJJKQ7ZCASf7+a+uG/ZM8O6Xtm8A6nquhXqLhZ7WXymzjGS0" + "\r\n" +
				" Wxjkcda8K/aO8WaL+z943/sPSvEGqajrMbpJNDJbrHBao4DIC+cu20g8cep7V9NReIyYkIbqoP" + "\r\n" +
				" 6V83xZWxeKp4aviJ80JKTguRQSXu/DGMYpJ6bK3Y9LLIYanUqwoL3lbm1bd9d229dzk4fip4t+" + "\r\n" +
				" BOow2vxaD+JNAkBWPUbeMG7iwBgHG0SDg5BAfkkF+leyaBr9l4q0W21Hw5dQ3tjeRiWGaJtySK" + "\r\n" +
				" e4P+cV514mlg8S+H7ux1ILJFPGRg87TjIYe4NcH+zZ8RLnwH8Q18Ka5OzaRrRdrFXORa3QBcqp" + "\r\n" +
				" J+VZFDnHTzFyBmQk/GHsH4j/ABO1T/hc/wC078QfFHjWcSXur67eTMWxyPOYBfQAKAPwFfWnww" + "\r\n" +
				" /4KJ678PPgTovgn4fal/wj8lhM13fajG4a51GXcNvzdEQIsalcEsVOTg7a+Rv2nPhvN8IPj740" + "\r\n" +
				" 0XU45Yryw1q6hmGepErc8ccgg8cVwH9oH1l/76P+NfteN+kRwFiMNQy7H4evJUOVJezg0nFcu3" + "\r\n" +
				" tNUul12drpHNgfoFeMOZ1Z55lGY4SNPEx5k/bVLuE7TWqo6N6ap91ezZ9n/tdftot8btf0jxBq" + "\r\n" +
				" 8Vpb66lpFZX81tkR3zJIdkoTnY21grDJBxkYHyj9VYHb7NFkn/Vr39hX87E1ytwymcSMUIYZJO" + "\r\n" +
				" D19a+oE/4LH/HSNAq69o+FGBnRLY8D/gNflHiB4v8ACmdUsHRyalVjGlzpqUIqyly2S996Kz9F" + "\r\n" +
				" ZI/ZvD/6E/ijk31medYrCznUcWmqtR3tzXv+5Xderuz9g79pHt2SHcSwwT6CvKvitNJ4O1HStY" + "\r\n" +
				" sVQz6VeQ3iBgcMY5FfBwc4IUqeRwTX5pf8Pkfjr/0HtH/8Edr/APEV1/7Pv7cvxf8A2s/jl4Q8" + "\r\n" +
				" HeJdZ02SDXtUgtJQulwxARs43ksi5A2g8ivzuhxvl+JqRpQjPmk0lot27fzH2ub/AEWeL8jwNf" + "\r\n" +
				" McXXwyp0oSnK1Sp8MU5P8A5dLoj3P/AILg/sSy2PjiD4seDrNG0rVxHZ62sYUGC6AISYqAPldV" + "\r\n" +
				" ALc/MuSRuGfgzT/gy+qIGsoy+f7ozX9Fvinwtp3jbw7e6R4usrfUdM1GJoLm2nQPHMh6gg1+e/" + "\r\n" +
				" x3/wCCTXiT4Ta5ca5+y68XiPRizStol1II7y2GWO2J2IWVQAAASG5wAeteHxZwpWr1pY3BRvf4" + "\r\n" +
				" ore/dd79Vvf8Ps/Bfx8hlOVU+Hc4q8jpaUqkn7rj0hN/Zcdoyfu8tk2rLm/OP/hQd3/z7Sf980" + "\r\n" +
				" f8KDu/+faT/vmvubQfFth4WkNh8XPAuvaVfwko6yabJww4IyFwcHril1zxFB4ymFh8GPh/4k1m" + "\r\n" +
				" /lBEYh0uU5IGSc7ccAHv2NfnX1DFc/J7KV+1nf7rH7b/AMRexXN/DXJ/P7SHJbvzc1rfM+DtQ+" + "\r\n" +
				" Db6Wm68jKfUYr9BP8AgiD+w/cW3iOX4teOLAw6fbxPa6AJkwbiQ/LJcKCOVUblDf3icH5TXU/s" + "\r\n" +
				" 9f8ABJnXPiX4kh8QftXFNG0dSJYtCtJla6uDwVE7gFUTnlQS3BB21+gfhzw5YeENCtNM8MWkFh" + "\r\n" +
				" p9jEIbe3hQKkSDoABX6PwlwpVw1VY3GRs18Met+77eS3vqfhvjV49QzrK58O5PV5/aaVakfh5d" + "\r\n" +
				" +SD+1d/FJe7y6K93b//Z" + "\r\n" +
				"REV:2012-05-24T12:32:30Z" + "\r\n" +
				"UID:" + uid + "\r\n" +
        		"END:VCARD"
        	;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCard(uid, vCard));
        /*
         * verify contact on server
         */
        Contact contact = super.getContact(uid);
        super.rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("firstname wrong", firstName, contact.getGivenName());
        assertEquals("lastname wrong", lastName, contact.getSurName());
        assertEquals("wrong numer of images", 1, contact.getNumberOfImages());
        assertNotNull("no image found in contact", contact.getImage1());
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(contact.getImage1()));
        assertEquals("image width wrong", 200, image.getWidth());
        assertEquals("image height wrong", 200, image.getHeight());
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        assertTrue("PHOTO wrong", 0 < card.getVCard().getPhotos().size());
        byte[] vCardPhoto = card.getVCard().getPhotos().get(0).getPhoto();
        assertNotNull("POHTO wrong", vCardPhoto);
        BufferedImage vCardImage = ImageIO.read(new ByteArrayInputStream(vCardPhoto));
        assertEquals("POHTO width wrong", 200, vCardImage.getWidth());
        assertEquals("POHTO height wrong", 200, vCardImage.getHeight());
	}

	@Test
	public void testAddPhotoOnServer() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create contact on server
		 */
    	String uid = randomUID();
    	String firstName = "test";
    	String lastName = "kimberly";
		Contact contact = new Contact();
		contact.setSurName(lastName);
		contact.setGivenName(firstName);
		contact.setDisplayName(firstName + " " + lastName);
		contact.setUid(uid);
		super.rememberForCleanUp(super.create(contact));
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        /*
         * update contact on server
         */
    	String updatedFirstName = "test2";
    	String udpatedLastName = "waldemar2";
		contact.setSurName(udpatedLastName);
		contact.setGivenName(updatedFirstName);
		contact.setDisplayName(updatedFirstName + " " + udpatedLastName);
		contact.setImage1(Photos.PNG_100x100);
        contact.setImageContentType("image/png");
		contact = super.update(contact);
        /*
         * verify contact on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        card = assertContains(uid, addressData);
        assertEquals("N wrong", updatedFirstName, card.getGivenName());
        assertEquals("N wrong", udpatedLastName, card.getFamilyName());
        assertEquals("FN wrong", updatedFirstName + " " + udpatedLastName, card.getFN());
        assertTrue("PHOTO wrong", 0 < card.getVCard().getPhotos().size());
        byte[] vCardPhoto = card.getVCard().getPhotos().get(0).getPhoto();
        assertNotNull("POHTO wrong", vCardPhoto);
        Assert.assertArrayEquals("image data wrong", contact.getImage1(), vCardPhoto);
    }

	@Test
	public void testAddPhotoOnClient() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create contact on server
		 */
    	String uid = randomUID();
    	String firstName = "test";
    	String lastName = "jaqueline";
		Contact contact = new Contact();
		contact.setSurName(lastName);
		contact.setGivenName(firstName);
		contact.setDisplayName(firstName + " " + lastName);
		contact.setUid(uid);
		super.rememberForCleanUp(super.create(contact));
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        /*
         * update contact on client
         */
        PhotoType photo = new PhotoType();
        photo.setImageMediaType(ImageMediaType.PNG);
        photo.setEncodingType(EncodingType.BINARY);
        photo.setPhoto(Photos.PNG_100x100);
        card.getVCard().addPhoto(photo);
		assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCardUpdate(card.getUID(), card.toString(), "\"" + card.getETag() + "\""));
        /*
         * verify updated contact on server
         */
        Contact updatedContact = super.getContact(uid);
        super.rememberForCleanUp(updatedContact);
        assertEquals("wrong numer of images", 1, updatedContact.getNumberOfImages());
        assertNotNull("no image found in contact", updatedContact.getImage1());
        /*
         * verify updated contact on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        card = assertContains(uid, addressData);
        assertTrue("PHOTO wrong", 0 < card.getVCard().getPhotos().size());
        byte[] vCardPhoto = card.getVCard().getPhotos().get(0).getPhoto();
        assertNotNull("POHTO wrong", vCardPhoto);
        Assert.assertArrayEquals("image data wrong", updatedContact.getImage1(), vCardPhoto);
    }

	@Test
	public void testRemovePhotoOnServer() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create contact on server
		 */
    	String uid = randomUID();
    	String firstName = "test";
    	String lastName = "kimberly";
		Contact contact = new Contact();
		contact.setSurName(lastName);
		contact.setGivenName(firstName);
		contact.setDisplayName(firstName + " " + lastName);
		contact.setImage1(Photos.PNG_100x100);
        contact.setImageContentType("image/png");
		contact.setUid(uid);
		super.rememberForCleanUp(super.create(contact));
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        assertTrue("PHOTO wrong", 0 < card.getVCard().getPhotos().size());
        byte[] vCardPhoto = card.getVCard().getPhotos().get(0).getPhoto();
        assertNotNull("POHTO wrong", vCardPhoto);
        Assert.assertArrayEquals("image data wrong", contact.getImage1(), vCardPhoto);
        /*
         * update contact on server
         */
		contact.setImage1(null);
		contact.setNumberOfImages(0);
		contact = super.update(contact);
        /*
         * verify contact on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        card = assertContains(uid, addressData);
        assertTrue("PHOTO wrong", null == card.getVCard().getPhotos() || 0 == card.getVCard().getPhotos().size());
    }

	@Test
    public void testRemovePhotoOnClient() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken());
        /*
         * create contact on server
         */
        String uid = randomUID();
        String firstName = "test";
        String lastName = "kimberly";
        Contact contact = new Contact();
        contact.setSurName(lastName);
        contact.setGivenName(firstName);
        contact.setDisplayName(firstName + " " + lastName);
        contact.setImage1(Photos.PNG_100x100);
        contact.setImageContentType("image/png");
        contact.setUid(uid);
        super.rememberForCleanUp(super.create(contact));
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        assertTrue("PHOTO wrong", 0 < card.getVCard().getPhotos().size());
        byte[] vCardPhoto = card.getVCard().getPhotos().get(0).getPhoto();
        assertNotNull("POHTO wrong", vCardPhoto);
        Assert.assertArrayEquals("image data wrong", contact.getImage1(), vCardPhoto);
        /*
         * update contact on client
         */
        card.getVCard().removePhoto(card.getVCard().getPhotos().get(0));
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCardUpdate(card.getUID(), card.toString(), "\"" + card.getETag() + "\""));
        /*
         * verify updated contact on server
         */
        Contact updatedContact = super.getContact(uid);
        super.rememberForCleanUp(updatedContact);
        assertEquals("wrong numer of images", 0, updatedContact.getNumberOfImages());
        assertNull("image found in contact", updatedContact.getImage1());
        /*
         * verify updated contact on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        card = assertContains(uid, addressData);
        assertTrue("PHOTO wrong", null == card.getVCard().getPhotos() || 0 == card.getVCard().getPhotos().size());
    }

	@Test
    public void testScalingImagesOnServer() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken());
        /*
         * create contact on server
         */
        String uid = randomUID();
        String firstName = "chantalle";
        String lastName = "dick";
        Contact contact = new Contact();
        contact.setSurName(lastName);
        contact.setGivenName(firstName);
        contact.setDisplayName(firstName + " " + lastName);
        contact.setImage1(Photos.JPG_400x250);
        contact.setImageContentType("image/jpeg");
        contact.setUid(uid);
        super.rememberForCleanUp(super.create(contact));
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        assertTrue("PHOTO wrong", 0 < card.getVCard().getPhotos().size());
        byte[] vCardPhoto = card.getVCard().getPhotos().get(0).getPhoto();
        assertNotNull("POHTO wrong", vCardPhoto);
        Assert.assertTrue("image not scaled", contact.getImage1().length > vCardPhoto.length);
    }

}

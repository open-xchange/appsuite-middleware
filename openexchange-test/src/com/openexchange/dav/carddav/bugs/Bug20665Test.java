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

package com.openexchange.dav.carddav.bugs;

import static org.junit.Assert.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug20665Test}
 *
 * Adding picture to contact aborts with exception
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug20665Test extends CardDAVTest {

	public Bug20665Test() {
		super();
	}

	@Test
	public void testAddPhoto() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		final String syncToken = super.fetchSyncToken();
		/*
		 * create contact
		 */
    	final String uid = randomUID();
    	final String firstName = "test";
    	final String lastName = uid;
    	final String vCard =
    			"BEGIN:VCARD" + "\r\n" +
				"VERSION:3.0" + "\r\n" +
				"N:" + lastName + ";" + firstName + ";;;" + "\r\n" +
				"FN:" + firstName + " " + lastName + "\r\n" +
				"PHOTO;ENCODING=b;TYPE=JPEG;X-ABCROP-RECTANGLE=ABClipRect_1&-21&0&182&182&22H1a7Qbat1pkOqveD+T/Q==:" + "\r\n" +
				" /9j/4AAQSkZJRgABAQAAAQABAAD/4gWkSUNDX1BST0ZJTEUAAQEAAAWUYXBwbAIgAABtbnRyUk" + "\r\n" +
				" dCIFhZWiAH2QACABkACwAaAAthY3NwQVBQTAAAAABhcHBsAAAAAAAAAAAAAAAAAAAAAAAA9tYA" + "\r\n" +
				" AQAAAADTLWFwcGwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + "\r\n" +
				" AAAAtkZXNjAAABCAAAAG9kc2NtAAABeAAAA1ZjcHJ0AAAE0AAAADh3dHB0AAAFCAAAABRyWFla" + "\r\n" +
				" AAAFHAAAABRnWFlaAAAFMAAAABRiWFlaAAAFRAAAABRyVFJDAAAFWAAAAA5jaGFkAAAFaAAAAC" + "\r\n" +
				" xiVFJDAAAFWAAAAA5nVFJDAAAFWAAAAA5kZXNjAAAAAAAAABRHZW5lcmljIFJHQiBQcm9maWxl" + "\r\n" +
				" AAAAAAAAAAAAAAAUR2VuZXJpYyBSR0IgUHJvZmlsZQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + "\r\n" +
				" AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAbWx1YwAAAAAAAAATAAAADHB0QlIAAAAmAAAA9GZy" + "\r\n" +
				" RlUAAAAoAAABGnpoVFcAAAAWAAABQml0SVQAAAAoAAABWG5iTk8AAAAmAAABgGtvS1IAAAAWAA" + "\r\n" +
				" ABpmRlREUAAAAsAAABvHN2U0UAAAAmAAABgHpoQ04AAAAWAAAB6GphSlAAAAAaAAAB/nB0UE8A" + "\r\n" +
				" AAAmAAACGG5sTkwAAAAoAAACPmVzRVMAAAAmAAACGGZpRkkAAAAoAAACZnBsUEwAAAAsAAACjn" + "\r\n" +
				" J1UlUAAAAiAAACumFyRUcAAAAmAAAC3GVuVVMAAAAmAAADAmRhREsAAAAuAAADKABQAGUAcgBm" + "\r\n" +
				" AGkAbAAgAFIARwBCACAARwBlAG4A6QByAGkAYwBvAFAAcgBvAGYAaQBsACAAZwDpAG4A6QByAG" + "\r\n" +
				" kAcQB1AGUAIABSAFYAQpAadSgAIABSAEcAQgAggnJfaWPPj/AAUAByAG8AZgBpAGwAbwAgAFIA" + "\r\n" +
				" RwBCACAAZwBlAG4AZQByAGkAYwBvAEcAZQBuAGUAcgBpAHMAawAgAFIARwBCAC0AcAByAG8AZg" + "\r\n" +
				" BpAGzHfLwYACAAUgBHAEIAINUEuFzTDMd8AEEAbABsAGcAZQBtAGUAaQBuAGUAcwAgAFIARwBC" + "\r\n" +
				" AC0AUAByAG8AZgBpAGxmbpAaACAAUgBHAEIAIGPPj/Blh072TgCCLAAgAFIARwBCACAw1zDtMN" + "\r\n" +
				" UwoTCkMOsAUABlAHIAZgBpAGwAIABSAEcAQgAgAGcAZQBuAOkAcgBpAGMAbwBBAGwAZwBlAG0A" + "\r\n" +
				" ZQBlAG4AIABSAEcAQgAtAHAAcgBvAGYAaQBlAGwAWQBsAGUAaQBuAGUAbgAgAFIARwBCAC0AcA" + "\r\n" +
				" ByAG8AZgBpAGkAbABpAFUAbgBpAHcAZQByAHMAYQBsAG4AeQAgAHAAcgBvAGYAaQBsACAAUgBH" + "\r\n" +
				" AEIEHgQxBEkEOAQ5ACAEPwRABD4ERAQ4BDsETAAgAFIARwBCBkUGRAZBACAGKgY5BjEGSgZBAC" + "\r\n" +
				" AAUgBHAEIAIAYnBkQGOQYnBkUARwBlAG4AZQByAGkAYwAgAFIARwBCACAAUAByAG8AZgBpAGwA" + "\r\n" +
				" ZQBHAGUAbgBlAHIAZQBsACAAUgBHAEIALQBiAGUAcwBrAHIAaQB2AGUAbABzAGUAAHRleHQAAA" + "\r\n" +
				" AAQ29weXJpZ2h0IDIwMDcgQXBwbGUgSW5jLiwgYWxsIHJpZ2h0cyByZXNlcnZlZC4AWFlaIAAA" + "\r\n" +
				" AAAAAPNSAAEAAAABFs9YWVogAAAAAAAAdE0AAD3uAAAD0FhZWiAAAAAAAABadQAArHMAABc0WF" + "\r\n" +
				" laIAAAAAAAACgaAAAVnwAAuDZjdXJ2AAAAAAAAAAEBzQAAc2YzMgAAAAAAAQxCAAAF3v//8yYA" + "\r\n" +
				" AAeSAAD9kf//+6L///2jAAAD3AAAwGz/4QBARXhpZgAATU0AKgAAAAgAAYdpAAQAAAABAAAAGg" + "\r\n" +
				" AAAAAAAqACAAQAAAABAAAAiqADAAQAAAABAAAAtgAAAAD/2wBDAAIBAQIBAQICAQICAgICAwUD" + "\r\n" +
				" AwMDAwYEBAMFBwYHBwcGBgYHCAsJBwgKCAYGCQ0JCgsLDAwMBwkNDg0MDgsMDAv/2wBDAQICAg" + "\r\n" +
				" MCAwUDAwULCAYICwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsL" + "\r\n" +
				" CwsLCwv/wAARCAC2AIoDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQ" + "\r\n" +
				" oL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAk" + "\r\n" +
				" M2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eX" + "\r\n" +
				" qDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi" + "\r\n" +
				" 4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtR" + "\r\n" +
				" EAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYk" + "\r\n" +
				" NOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhY" + "\r\n" +
				" aHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn" + "\r\n" +
				" 6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD8fNOt1u9+8tkjKMvc+n1J4/Gow3yHzGwB37Z/yK" + "\r\n" +
				" Xw5Pi4XkBYT5gz1IB5A9aZJCVVluASWO7I9/8AJ/X3rnNBqwlm2x8iTngck+mfWkiiK2w81wwe" + "\r\n" +
				" Rt/PIHHH509YPsls7PyJsBDjjg8/zpiRpCcygvkZ57d+PegDpfCNrjWC3VDlh7j1r3r9mv4JXP" + "\r\n" +
				" xA8S21jFZ3MkF44jLIhIYEDrj6CvW/2Yv+Cfnwj8I/DOw8QftpfETXdJ1fV7Fb610jQliVrFG5" + "\r\n" +
				" X7RLJFLuYg8qEXaeMnt+hf8AwS7+DPwP0bVH1LwV4s8UeLrax3NHpV5bIELYO3dIkS9MZxkZx2" + "\r\n" +
				" GaHF9BXR+Tf7Qn7MGrfA/WLi31uzmj+cmMgfcXnGT9K8E1q2CtCzqGySGjx15+9+FfvH8YP2vP" + "\r\n" +
				" gL+1f8afF/wb+I/gvQNMuLVJohqcEcNrKJEG0rHcqAwlUtwpYgkYIbBFfi1+2T8KNE+CHx/1/w" + "\r\n" +
				" ALeANem8QabpsoVLua3EEnIBKuoJBK5xuGA3BwpyAkrDPKtTkVpbYK2YwwDA/X9P8A61Up1LXf" + "\r\n" +
				" 7rOR074+lT6hL9oukacEF1XPvwKpEGZmR+q8/Q/5/nTA6DwhLG9/Ik7blZAJlJxu7D9SKwNipI" + "\r\n" +
				" UlI25x9O+BWn4WiZ7uZnb5kGUB/jfpj9QayiDv/eAh1XJ9jn/61ACxuBG7jLJIO9WEYvbxSxOO" + "\r\n" +
				" ZiVA4yeOKrrOLmXzIwYllIYL1GRxjj6VNIm2ytIR8pWZiB3Xp3oAgeYG8iJyDtcY+tKgVCNwOy" + "\r\n" +
				" U4z1P+eaSSD9/CwcMrqR7rjiodjEFgMoSc+vHTFAFtpBZw7STMg3Dc2TjqMfzNZnnCD5Mt8ny/" + "\r\n" +
				" eHar8jiXR4UiOCHZseoPH+f/AK9ZjBNx+df1/wAaANTTG3XcOVOA2GUfxKTzSybog/ktuVXyD3" + "\r\n" +
				" 5Pp+VLon/H64mJUxqXjPqR2/PAqKE+ZG0ch27SZN35EfzoAtahOz2UaJnAYk/n/wDq/OqElwbZ" + "\r\n" +
				" SJPl2Egfy/x/Krc13i2j4289x06Uul6BeeL/ABNZ6R4bga7vtTu0tbSFfvSyO4VFGe5YgcmgD7" + "\r\n" +
				" 4/Yy1DRP2uLbVU+IBuJbdtLFlNHFK8bRqpGFJUgqOAMA4Ix61n/wDDtnx5/wAJs0H7P/2PW9N1" + "\r\n" +
				" a6kg0+OW9S3uo2W3nuSjmTCMBHbSEODztA4NbPwp+Ffw3/4JxeHtUi8Yata6/wCPp7Fn1XVIwR" + "\r\n" +
				" baSCB/o1ojEFpDyu7AZv8AYAYVyvw3/wCCy3iDwN4+sbz4c+EdJ+zaJczXVuupzySPOz2l1a5c" + "\r\n" +
				" RlAvyXbNgE8oPmNbWutSYtn0F/wTN/Zj8d/sv3/xD+Jnxqs7fTbVvCc1jYWsd3FdTXRmkilEn7" + "\r\n" +
				" lmVQBCuOc/OD2NfmB8UPGlx4s+KGtX2tSLLcTXUiykHKkg7eMfTtX6A/sw/wDBbiz1LWLDwZ+0" + "\r\n" +
				" r4VsLLw/fxR6ZLqlhMxjtwAEV5YJd2V4Ulg3y4ztYdPOP+Con/BKJP2ZID8SvgBPbX3w31KSIz" + "\r\n" +
				" WZkY3GjSyEgBd3MluxxtOdyltpGF3mZRBt3Pji8t/MvLcKcEpuTPtziqVpIolbdjf/ABAchqht" + "\r\n" +
				" 7uSa9JdizQkAdxjOOPyxTZCUuWKDaD6d81C3KOm8F2sdzfNGy4Zfm4HBGCMnP1rEmK3duBC211" + "\r\n" +
				" B3rjp/hVvSbuXT/Dly0fzTXNx5CHjIAG7A9Dx19/pWTKuRI8JwF6c8sfT8qQCWzH7AyyoBsOMd" + "\r\n" +
				" cHrx+VakbGVbNVX5hMRk9CDs/XrWbbzCNWDHJY7iDxjtVqxfzGtl3lysxLDHU/Kf8/SgBloAzS" + "\r\n" +
				" FBwFbA7dKgcETiIHLFfl/2cipY8hm2A+ZtbcPpVfzlupRMWAJAX/dPc0AXQ1t/Y1jbRqTdtPKL" + "\r\n" +
				" iXja6hVKgfQ5z9ay5od8znyxyxPQVeuGEGn2SBwAjvIpU8qSADn2OB+VNW4nRQPlOOPvUALpoY" + "\r\n" +
				" 3z84ZFJGPp/Pn+VVonUJufBPOcHg5/n1qzpykC5zgny2yfbHNV7V828ilTuYAqeu73/WgBJZCF" + "\r\n" +
				" VZCdpOR24GK9Y/Yg8AXnxC/aY0CawlSG08NyjXb+cjIhgt2Vzj/aZiiD3fPQGvJDJuZEyRtyF4" + "\r\n" +
				" +9689q9f8A2GPFz6R8WdR0uCVYZde0+W1jY8kkFXZQfdVY/wDAaqOrE9je/a++IVl4nuZUeRJ9" + "\r\n" +
				" Vvr+S/ZiCWWABo159GOTj/ZB714p4JH+n3DekZ5r3X4wfsr674y8Lat448BrFc2GgaSJtYheZY" + "\r\n" +
				" 3tokkSPzV3EBxmRAVHzc5APO3wnwUNpvmzysJJ/WthRKV42+5fHPNfqz/wTM8Z+Av23/2C5/2d" + "\r\n" +
				" tW1uXw340tYbqFXvGNx56Tysy3NurMoYRl0BiyuNnUBwa/KCSTMzFj1rX8DeLdU8D+MNM1jwVq" + "\r\n" +
				" F1perabcpPaXdtIY5beQHhlYdKW49z0v8AbD/Yk8f/ALBfxduPCvxy0toHkZ20zUoMvY6xEpwJ" + "\r\n" +
				" beToeq5Q4ZMgMASK8pRfPQiQHeMrgZzkdvwr9aP2WP8Agqz8Nf8AgoB4Ltvg1/wU/wBF0qaW/m" + "\r\n" +
				" jhtdXkH2eCaYfKkySrhrO4GT8ysobLgEB/KPmH7WX/AAb5+Lvh34fvvGP7GviCD4qeE4wZvsCK" + "\r\n" +
				" I9btY8FuEX5LnC4zs2Oc8RemUo2C/Q/P7QI3OimaQAJDck7sZwSnH8u9ZN0jRRCU5w/K46E4/W" + "\r\n" +
				" utsNJNhpM89wjiKBys6Y6N93BH1I/KsO8gFtDMpIOAQvcJUjMfaGlfzOAh28dxVzTLSWXU7aJe" + "\r\n" +
				" GMwTPQfNtwarzQrJ5qD5STkcdsY/Cn/bTDHBKpaOSOVmdhyRjbgigBVkks5wGGPOVkHGRjoari" + "\r\n" +
				" EBQqjGfunPUDr/AE/M1Ks7Xo8tzzDGZAfYjJx+lJCwlhiEnygnIPoO/wBKAHX0Cf2bAwfILkFe" + "\r\n" +
				" DgcY/X+VI128DFHOSh2nn0pt9iNI1AyclgTzleo/z7VEw89jJuPznd09aAJtJlKO/mZTzUKsM4" + "\r\n" +
				" 4Pv9KrmX7NEokGCHAXA98U6F1M8uMtGcKB1IJH+PNFu6Sz2IuCQ0TsWz3BYHGR+NPYCG8b7LNh" + "\r\n" +
				" jkOcA/3D3r64/Zw+HGj/ALMv7O//AAsDxrbwT+OPG8LxaFFcIMaVp+CJLvn7rSgNg44QcH5zXz" + "\r\n" +
				" R8MPAw+J3xS0jRFMgTWNVS2JQcxxmQeY+PZNzf8Br1/wDbx+LS6/4gvbXQiItOg2aPp8anCxW0" + "\r\n" +
				" KqDtHbICKfrVwSJkeZ/Eb46ap8REudPtrqeHQLVMQ2yEqs5Dj97IP4mJyQDwowBzknnPCEmLPU" + "\r\n" +
				" TnpAf5GsDTW/cXee8Q/wDQ1rY8JknTdTP/AEw/o1aDWxnlwzcVd0D5tXtVPeVf51m7vmxWl4cf" + "\r\n" +
				" Gu2mP+eq/wA6BpalvSFWfXwJSAAxOT04r6z/AOCcf/BSfxt+xH8StOFpqd3qngiWZY9R0aeUvE" + "\r\n" +
				" IiSC1vuP7pxknAwrdCOhX47abbdsVOPmP862dK1Mpg+nvik9gunofvp/wUu/YI+E/7QP7Gt5+0" + "\r\n" +
				" V+z28LQ61YpqWqyaZa5iv4v4rtolAZZkz+9HX5CzDcrbvwj8RWFpp91dww3AuII5CkLgYEqjox" + "\r\n" +
				" H44r9C/wDgj/8A8Fav+GYfDdx8Mfj9cNefC/X5j5byqJBodxIcM2G/5d5MnzE6A/OAMyFqH/BT" + "\r\n" +
				" T/gix4g8P6vd/EH9g7TpvGvgLWc3UmkaV/pV9ozMdxWGJfmuLcg5XYCyg4KkAO2TXYLW0Pzdu5" + "\r\n" +
				" Ps95sB3hh8jY/1nT+dPt4or+CNSSp3vuw38OF9/rT/ABBp8mm6zNBqUE9tc27GIwTKUkgdeCrK" + "\r\n" +
				" eVIIIwRkfyZDhhBLGCylyrDB+XgE9f8A9XNJ7gRIzTNECCH3bSc/wnr+mPzoOQxMGHSORlOR1H" + "\r\n" +
				" QcfhUbzlJ4yoOD6enfP+exo8wQxSCLIMjAgdsZ5wPWkBaRRdo7MBwuEJ79cj8M/wCegxYpisSj" + "\r\n" +
				" IOABWql0bMQBBlSX+U85GMVQyx/1CsE/h+UdO1Am7E1o4FxiJiQRgn3xgVGXAjO/kxHk/XpU3h" + "\r\n" +
				" 5kGohbxfMhMbyBfVgDjFQyyia5m4MasclfTk4/nVb6BbU9M/YznaD49aXdRYMmmW17cLkZ/wCW" + "\r\n" +
				" Drn/AMerm/2hb97vW7Hfn94ktx1zy8pH/sore/Y/vPsvxamUsFMum3KKM9cgf4GuY+PcJTXtLb" + "\r\n" +
				" Jw1gBj3Esn+NaRVkJ7nI6ef9Hutx/5ZD/0NK1/C8w/szU8H/lj/Q1i2R/0a6z18of+hpWl4afG" + "\r\n" +
				" m6lk9YR/WqGviKayAtzzWj4dl/4ntp/11X+dZMb56ir2hzeXrNqR/wA9k/mKAXcbI37wn1JNSR" + "\r\n" +
				" XjRj5c1X8zcxz3NIjbG5oC+p12gX73PhrUVP8ACox+Rr2v9j//AIKs/F79jI29l8PtcbUfD0HA" + "\r\n" +
				" 0nUCZIY1zkiJvvIOThfmQEk7Dk14V4Rbdouo47gD9DXPq2F5pWRT1sz9j/hv/wAFGP2av+CsN/" + "\r\n" +
				" Z+Dv26fh3pOh+MtUUWdjrbqsNwJCMIkOoR7XDeiSEIzbV8tuFr4w/4Ko/8Ex/EH/BOH4naaLK9" + "\r\n" +
				" k8QfD/xOry6HrJQLIrDk2t0B8qzovRhhZV+dcfOifIEbkSKVOGBGOcY+nev2i/Zq+KU//BUn/g" + "\r\n" +
				" jvrvg74xsNU8V+DFfQob64cNNI6QibT7yRx8yspHlsxPz+WSxO5hSktCXsfjSZ/MaMOPlycD3z" + "\r\n" +
				" 601Jfs80xYZdMbR/P+VQmZnKBgSyqxxxwf8AOafAhmtsq43Fsq+emCDisnuC2JROXnhaQAjB2r" + "\r\n" +
				" 6nAyPywahVotowGqa5n22YKLslV2L/AKVUI5PySfrSGT2Ay6KBn5WHHUHkf5+tV5JPMhMpHQlD" + "\r\n" +
				" xx1wP0qbRW8u/DrksqsWTpx371EsuRtO3MjnI6A5PB/nV2QHf/sp8fHGxjZQTLaXK5PH/LBjn9" + "\r\n" +
				" KzP2h4gupaS+0j9zLHuPfEhOPw3frV79louP2g9GiB6x3KHHcfZZf8Kh/abBiv9OT+7LdD/wAf" + "\r\n" +
				" WrjsQ9zzqyf9xdY/55D/ANDWvsbxb/wTp0PQ/wBh3xx8WvC/iTUYZfCtrozyaZPbpNHem+l8lt" + "\r\n" +
				" sylfK2lgwyHz93jII+NbM/uLn3jH/oa1+qZCS/8ERfjul1Gplg8O+EJE3DmNv7WtVJHocOw+hN" + "\r\n" +
				" UF2flhHJXov7PH7Nnjn9o3WrqP4JeHrzxHc6S8DT2toVa4fzHKosURO6VyVYBUDMcdOx80Vsda" + "\r\n" +
				" /S7/g2tuvI/aK1Lnr4h8Nj/wAmbigR+cd/Z3GlX81tqkE1tc27lJYZUKPEwOCrKRkEehHao95z" + "\r\n" +
				" mv19/wCCgX7PHg34qfs7eMfEfiXw/Yz+JNA0a4vrLUo4yl1G0MZcBnTBkQBSNr5UZJwOtfj8uQ" + "\r\n" +
				" B2HSgvc6fwfIRo15t7nn8qwN49a+qf+Cbn/BMD4g/8FFfBHxJ1D4Fap4SsE+HcVpPqEWs3VxBJ" + "\r\n" +
				" dC5S5aNbcQ28gY/6HIDvKDLJgnJx8nb8EdaCnuWc4Nfrb/wQ/wBKuNI/4J1fFfXXZBBqviRdLh" + "\r\n" +
				" O7L+bDaxM2R24ukwfrX5HB/lGD/wDXr9if+CEF3pnxF/4J2eI/AuiX8M3isfEeS+l07diRLa4t" + "\r\n" +
				" dNhgkPH3XkhuFyM48vnHGVZEt6H49iUeaSuc7ju9+etSh/LdYm+6SSMDuf8AP8qfqeiXfhzXbz" + "\r\n" +
				" TfEMLW95YzSW1xE33opUYqyn3BBqNELygpw8YyVJ6juaxe4X0LLAiB3YBtgAbOfmNUTdrn7w/O" + "\r\n" +
				" tN5hJCGh4RhgE/8A1/rWU1qHYkydeaQ73NTT7kyXDsF+d0ZD228EHH86pSFHhddvzyuoVgegBw" + "\r\n" +
				" c1Lbny1BX769e7BcHP6Cq+fstwivhg5yGPTOcgf59atpk8x3n7Kh2ftC6CWOQPtQ/8lZv8ar/t" + "\r\n" +
				" RsRq9juzzPd/+jFrqf2Kvh1eeJfizfeIv+PfRfBlnNqOp3Tr+7TdG0ccOezyM4AHXAY9q4n9oz" + "\r\n" +
				" xVBrmo2EUAInjae6cdlErKVH1wmf8AgQ9auOwupwNm+I5wMZZABk/7a1+pA8ceH/GP/BKb9pHT" + "\r\n" +
				" PA2uabqKW3h/QGxbTLI6rDrdn95Ady9Mc+tfmRP4H1zTPD0mp6hpGpQafIgAuZLZ1iOWGPmIxz" + "\r\n" +
				" kYyec1V0fUZ7Kw1BbOaWJLmHypQjlRKu5Ttb1XKqcHuB6VQiDfuxu71+kH/Buf4ntrX9oU6dPc" + "\r\n" +
				" Qx3k/iTQZYYmkAkmAuJAdik5bacZx03D1r86PDvhvUvF2qJY+FbC91S+mBKW9nA000mB2RASfy" + "\r\n" +
				" q/4R1/WPht8QNN1Hw5d6loWvaHfxz29zbyPa3dhcRyAh0dSGjkRl4IIIIoA/Yj9r74seGfBv7O" + "\r\n" +
				" vi/TvH2v6bo83iDQb6wskuJP3tzJJbvGojjGWfl1zgHGRnFfjKJOBjJxWx8SPifr3xc8VTa18S" + "\r\n" +
				" dUudW1OdVRppiOFUYCqoAVQB2AA6nvWGpwc0Afq3/wamfETUdI/aL+MPhq2nxpGr+A5dUuIT0e" + "\r\n" +
				" 4tby2jhbr2S9uR0z8/5/mj8Y/DC+CPjD4s0VE8saPrN5ZBCu3YI53TGO33elZ/hi6eDSr4QyMn" + "\r\n" +
				" mDa2GI3jHQ+o6cVib/AJuuKC9lcmdzuAFfeX/BvD4m1HSP+ClvgKx0ct9j1tL211ADoIVjWZTj" + "\r\n" +
				" /rrDF+fvXwQZQT71+i//AARN/aP+Av7Fd9pnxB+P/jeS18b6nrbaaNPh0W9uF0XS08l2neWOJk" + "\r\n" +
				" dppPl2x7mRYAf+WhUAb3Pkn9uCC1sv2tPHMemwfZ4f7Uf5B/Cy8En6kFj7mvMhmeRTEcttO7nr" + "\r\n" +
				" x/Kv0u/aB/YH+Gn/AAUFsfEnxL/Ys8aJdeLZpGmudJnlU2l8wUBY1yFe2kKqMb8gkjcFyXr83f" + "\r\n" +
				" EOg3nhXXbrTtctLix1LTZWt7m3lQpLE6kqyMD0Ixj8KzcRJ9BlucaTHkcklV57gc/oaq/ZnblM" + "\r\n" +
				" 4PTmppDttIPLIyXYlR7gUzyyvGSMUkh3R6H+y7+zH4q/a0+IX/CM/Cy2ga5SyuL69vLpzFZ6Za" + "\r\n" +
				" RRs8k88mDsUKpxwSxIUAkgV6zrmmfs3fszj7DrkWt/F3X4BtuLn7WdP0iOYEEiFIiHcDpy7A4z" + "\r\n" +
				" jtS+LviZN+yV+zdY+BvAtxJp2reJtLfU/FlzC2yaczRssdmzDkKqPsK9DuY45bPxrqOoy6ndNN" + "\r\n" +
				" dsWY9OeFHoPStErEH0R45/bC0vVvAt54d+FWg6X4L8NSXBvptLsHnlfULjbtBlmmJZgABxnAy2" + "\r\n" +
				" Byc+ffA3wfcfEr4kLqOuqJbOylE9wzD5ZG6pGAeOw47AV57oemT65qcNppqFpp3CgdQPc+w617" + "\r\n" +
				" z8I7m30rxzB4c0Fg1to1o8t04/5b3LFVOfoGYfjjsKYFv9pz47avpUGq+DPD7wwaZqVjCmpMED" + "\r\n" +
				" SXCieOZUyfuAPDE2RzxjOMg+C2RPk3Gc5Kf1r0D9pS0lsvijqU2pKEjvbeCS2JbmRAgVj6/fRh" + "\r\n" +
				" z6d65v4P8AhkeOPH2laRICUv7yGGTvhDIu4/guT+FA1ufb37Hfw+0j9mv4By+J/GZjtr3UbMan" + "\r\n" +
				" qVw4y0MO3dHCo65CkfL3diOwr4m+IXjyT4m/FbVfEN3Elu+r6g915S9Igz5C++BgZ7mvrz9pP4" + "\r\n" +
				" h6TB8VPAPhT4iiQ+FdRma41C3RTtuHBCW6yAHmISfMw74B5xWlqn7D/hn9ob9vr4QeBfC1pD4c" + "\r\n" +
				" 0zxRdW8evPpcSQG3s/tcURliUJsWRvMKKxUjdgnI4oEfCBYY5PTivWP2MP2an/ap+NVv4emupr" + "\r\n" +
				" HS7eB77UriJQZY4FKriPIxvZnRQTkDJODjB+ubzS/h1N8HfifoFz4H03RvDvge1ayh1O4gUPfT" + "\r\n" +
				" iE7njdkD+Ysm1Q25ixYe4pn/AAS20zwx4B/Zx1rxubC9t9TVrhdUvJ3DLPFbgyjyEyMIFbBz8x" + "\r\n" +
				" dTkkAbQrlPJ/Bv7B1trP8AwUO074LeFdSn1LTNQ1GB5p2YC5gsRb/arneVGPMSBJjkAZ2g4GcD" + "\r\n" +
				" 03/gsRN4O+G/wm8B+A/AeiaTpd7FdNqES2sCI9naRxNFtYgbiJHkByfvGAk5IzX27/wQy0j4Cf" + "\r\n" +
				" tSeIPi58T/AIZfDnxXD8RfB6+e/ijWbt/LgN/DcRNHFEL10LsiT8iHCJxlQwB/Pj9uX9oHwF+0" + "\r\n" +
				" j441HwL8LfAtzqPxFvdTh8Pxa9d7EEDRXW0i3CsxKMVZdxCfK7MRxyBzHxSHwDn/APVWtY5a2s" + "\r\n" +
				" FTJb7QQAPwwP1r9QfgZ8D/AAJ+zl4/8P8Awr0vwHZ+K73VNEl1bxB4lvraOVIcFkQNvRgEkdHR" + "\r\n" +
				" UBXaAD85JNcF/wAEjvAXw71T/gqRqviF0sV8J+BfEEuoaLPNtXTLIfbVWGUzOdqbEyY93u4wUz" + "\r\n" +
				" QNbHzF+zF498cfsHftHeFdf8daNr3hjTdVmWK/t9Ts5bVdQsGcJKcOo3bA24Hsyr2PP1B/wUw/" + "\r\n" +
				" Ypb4xQz/ABr/AGbrjTNd0uSzE2rwWUnmvMEzm7iZSVkwmA4BzhAQTkgO/wCCzX7Osd/oVx8TtS" + "\r\n" +
				" 8bam7Wt1FYWWhXYVrUGQgMtoVA8tiEMrbgxbYctwoHl3/BHL4+ato/xb1L4bXM81xovi3T7ma1" + "\r\n" +
				" tzIQtvdwxtNuTPADxpIrDv8AKe1AuU+UJVD2cLrnKyP+I4x/M/56Vee5H6/413f7SXhWz8DfGT" + "\r\n" +
				" xHpWgiMWcOoSSQoowsYbDFFHopJUf7tcALZ3GcsM89az2KPSv2tvEE3izxtq97cSh1Zok3/wCw" + "\r\n" +
				" Bjr35/lXOazpaaRoEUGk6ZFfqrBTEQBkYPzH1Ocfnmugl04eMPDGnXGsbvL1fTrhFZhzuiDfPn" + "\r\n" +
				" viQMf+AivNNH8Xa7Javb6VELtIPl8zyixA7cjg/jmtDM6iXxFafDvS4rmPTrGLVZuFijA+UZGc" + "\r\n" +
				" vjOO3ua7D9n/AMfN4v1XUBLp+n2ZiCOGt02tIWY53HueBXgmoancajevNqUjyTOfmLdfpjt9Kt" + "\r\n" +
				" aD4l1Dw3efaNAu5rWYrtLRtjcPQ+o+tAHvvxBs9H+H3jS48XeOrxNWvgqjRtLI4VlAwzjPKq2T" + "\r\n" +
				" 2GT3Jrz79nbxTHafHjSNT1+ZI1mvg0sn3UVpCRk+i7m/CvP9R1W51e8e41a4muZ5PvySuXZvqT" + "\r\n" +
				" 1p1m2IJiTztFA1ufdXxY+Dtt4l+M3h3xv4z1GxsPDfha1Wa6ErlWaSOUyIOm0qSwzzztwAd3HZ" + "\r\n" +
				" /wDBJ/8AaHtfjR/wVQ0G8u2EH23WdHtdHjlPzG3hv4ywA7sxcyleow3Py1+eWs+Oda8RafDaa/" + "\r\n" +
				" q+qX1pb/6qC4unljj4x8qsSBwB0qLwxrt54b8RWGoeHbu6sNQsrhJ7e5t5WimgkVgVdHUgqwIB" + "\r\n" +
				" BBzmgR9u/tAfs7+IvHvivUdZ/aO+J1ppnw4s7pri2tkYo8cWSUiEW1YzKFygf9459CTivafC2k" + "\r\n" +
				" aR8X/2NZdD/Z4jg0XSdd0mfT9PFwCBEjM8bmTaS25vnLHJO5jnJJFfl/4l8cax4zuUm8Y6vqer" + "\r\n" +
				" TRjasl5dSTso9AXJIq7ofxV8T+GPDdxo/hzxDrdhpN3nzrO3vZI4JM8HKKwBz39e9A7s/oo/4N" + "\r\n" +
				" 6PC/hDwF+wz8WPDHwnu4tVbQvEAtdV1NSuNQvjZozsoBOI1DrGOeqsPmwSfyq/Z3/Zf0L9kf4x" + "\r\n" +
				" 6J4m/az8UaU3irVtQMOh6bays6iaVihuZmKqcAsRkgKCepbAHgf7Hn7fnxe/Yl03xJF+zD40vf" + "\r\n" +
				" C8HimNItUgS1t7uG7CbtjGO4jkVXUO4EigMAxGcE1494v8a6t498Q3Gq+NdRvdV1G6OZbi6lMk" + "\r\n" +
				" j44HJ7ADAA4A4FA/M/Uv9s74C/Ez48azDB4N+Itl4T+H7WQXVraVjE6sHYyOWRR5qFCuUeVVG3" + "\r\n" +
				" 0Oa8s8A+Mfhl498R/A79lL9ny3Nx4W174iW0vj3xaZBFc+LPNntoxbxMBxDCiSbOxdwVH3nm+E" + "\r\n" +
				" tc+K3ifxN4fg0rxH4j17UNLtQBDZ3OoSzW8QHQLGzFV6noO9JoesXvhm50nUvD13c2F/Y3aXVt" + "\r\n" +
				" c28rRTW8qsGSRHUgqylVIYEEEAjmgIn6hf8FIfgVbfFvUPDOq/FbxdpfhT4X+EBPNqUeW+2XMr" + "\r\n" +
				" bQscCbdrMUj2LySpdsK+cV5r/wAE/wAj4/8A7Vnin4uaBocfh7wX8OtBk0nw/aLFjy/3DQxRE8" + "\r\n" +
				" lnETyu5yxBkUZIIJ+FvGPxj8TfFbUbN/i34i8QeIobR/lW8vnmaNSRu8veWCsQOuPTOa+wP2ef" + "\r\n" +
				" +Csfg34PeDLHwfY/DS70Pw3bQ+Sz22qpeyzFgd8kqtDFvZick59cDgCgk+UfjH4tk8Z/E7XNSl" + "\r\n" +
				" DI89y77W6qxOSPzzWIt7DtGS2a+7/2v/2Cvhz8SP2OtC+O37GFxN5uo6lqMGr6Sj+ZbzJBHauZ" + "\r\n" +
				" IUPzwzKbh98RJDDG1UZSH+BMY/hf8qhqwHtP7YvjC30fxWNH8Awm107w9Yx6HZRgZbHlkOxI+8" + "\r\n" +
				" zfNz1y3rXn3hq0m0HwUi21uzXm1n8voWcnjOfwrS/aTke/8e6jHbMfOn1NVRxxtOw8/rXP/EzX" + "\r\n" +
				" 59D060j0yaSOWRiS4PzYUYP6ke9WBxtt4Zv9Q1w2EUO683fMuRhe5JOcAc1ra78K9X8O2kk9xF" + "\r\n" +
				" HcW8KhpZIX3CLPYg4OR14BA+lb3gGObQ/Dl/quoq7Xk0bz/P8AeKqpIH4kGtbwTeXWo/BzVmgh" + "\r\n" +
				" nvLy+aWNgFLNKzkLkcc4z+hoA4bwp8M9b8bWdxceHLJpobYEvIWCLkDOAWIyfYV0vhD9m7xj4q" + "\r\n" +
				" +E3iHxlomjyS+H/D8Ec97O0qI6RM+wSLGzbmXJHIB456A16hoE3/Ctfgcw8RQlGtbRhNFC4Us8" + "\r\n" +
				" jHA3diS4BP1weleteGvi/b/D39irxnP4707zLjxDb6Zp9rp1qPLieZpJJkibOSECQsWUfeAK/w" + "\r\n" +
				" AWQDW58mH4C+J4/hs/iq5sVi0pFWT55AJWjJA8wJ1289+o5GRzWz+y3+yz4r/av+KOkeHPhjHZ" + "\r\n" +
				" wy6jqFvYfbr+RorO2kmkCJ5jqrNjJBIVWOOcV7D+0945vtN+D9hofkqdc8TtFbvDBk9NrSBO5B" + "\r\n" +
				" bYgHcMeau+NdTvf2YP2UtLs/CMsumaxeTQQS3UD7JoZ5AZZHV16EeWVVgcgBSOgNAXZ87/ABT+" + "\r\n" +
				" BHiX4PbJfGdki2NxM0FvewSCW3uSozlD1wRyNwGcHHQ16X4B/Y4gPwLvvHnxp1m60DTvsv2ixt" + "\r\n" +
				" 7a28+aVWwI3cdlZiuB3BBJUV6Z8efBesfFD9nv4deGfhxpNzcfavscxlaPbHZRJbbQZTj5P9bn" + "\r\n" +
				" 1+Rhz39Z+A3xg0/xVpeu6RFsutD8FwW+mPq8mPI1JkhxMwQDaFXaM8kEOD06gJXPFP8AgjT+xj" + "\r\n" +
				" Y/tzftm6P4W8bQSXHhXRbSfxDr0aEqZrS32hYiVIYLJPLbxMVIYLISDkCvO/27fghH8Jfjvrn/" + "\r\n" +
				" AAg/hnVNF8JNKq2cro72sj7Bv8qQ5GNxb5SxIPYAgD6t/wCCT/i+f9lTwB8e/wBqLQ4hY23ky+" + "\r\n" +
				" BPAugLuUa9rOpTrLDAI0IMsNtHAsrxgqTtG07krm/+CjHxa1Oz+C+k+E3t45vEfjaeNbm3tF8x" + "\r\n" +
				" V8oo7rEM7smYxKvqu4ZzQOR8kfAD9mnxX+0nrk9p8N7SDybMA3V7dSGO1tc9N7gEkn+6oZsAnG" + "\r\n" +
				" ATWh8Yv2cfGnwLitn8f6ZD/ZL3n2O21O1nSe1unA3AIwORkAkbgpO1hjKnH1F8SVvP2OP+CfNr" + "\r\n" +
				" pvhYNp3iDVvJt7y4jO2WK4uMvMdwP3lRWjVuMYUjBFbfxP8Ag/4j/aM/Zu+CPw8+DukT3epeJb" + "\r\n" +
				" nR4UkKFYLFTbCPfO/8A3TKTnnhupGKCTzD4Af8Ex7Px58HIfGPxi8USaDbajY/2jbxwRoUtrcp" + "\r\n" +
				" vSaeRzjBQ7yoAwuMtnIHzxB8AvFOotpVzo+k30+i+IdTXStK1WWBoLW+leTZGQz8KGPOWx0Poa" + "\r\n" +
				" +2P2toB8PPhP8AC/4NeHb+5Ft4r1K00O4u3kJke1ieFH5OSuXliOBwFBUDHFdd+1BrunTeKvhL" + "\r\n" +
				" 8M/C0ERmutfs9QNnFkLa6dZFnYAKQUGEwvThHx0oHocH+xB4c8efsz/EHXvhH8YGmj0PXNJu/E" + "\r\n" +
				" OnRQzmeza5jWJJJYT0UvHEY5OAxEUeRgKa+ePFX7PTT+KNSexlsbaB7qVo4cH90pc4X8Bx+Ffc" + "\r\n" +
				" Go+NF8b/ALYN5HHIv9lfDLwVfPdSDAVLy9aIeWxx3iWNuvXPSvhvWf2iIv7Yu8xXIPnP3/2jSa" + "\r\n" +
				" uD3OX+KzS65e3GsaTGZmtZI7/A58xUADcemQT+FcvqnxJ0q7to5ns3uLmLmNJEBEZ+p/pWj8O/" + "\r\n" +
				" HsOj3Lw62DJEYHjBz2K4q3dfAvTfGmbrwJrenQNLybe4mWIg+gDEH8simI5zw58V0VJk8VQtMJ" + "\r\n" +
				" SxzGgIKn+Eqcdu/Pem6j8YL9JUh8Hf8SuyhQokaIpZs8ljwcH6e9dHbfso6gsuNZ17w9agjOGv" + "\r\n" +
				" 4g36uK1dI/Y9a4ugb3xl4WggzyxvYiT+HmUAZWo/F5fE3wtj0W/huG1ANGskpI8uZEIOSc7txI" + "\r\n" +
				" HavRPF/wActG8UaPoENva3n2ey1S31G8t2jUACIOMLzzw7YHAxnNX9J/Ym0zU/JTw5420B5eN4" + "\r\n" +
				" kvImDH2GVx+tfb/wG/4IZ6b8Xvg/Jqll4v0Yava2jTtbh1xJgE8YY+lTzDW58N/Fb9pTQ1khuv" + "\r\n" +
				" h7p63muwxNHBqNzBtFiG6lA3JbjPYD3GRXSfCL9tXwlaR+Gpfjf4Tl8SyaJfWt1c2MltDcWuom" + "\r\n" +
				" GRWORLwu4KQQUYfMRggkVo+If+CYPiuy8T3MF3qugWdnDKVMrT42qDjIBwCfxrtfg7/wRx1b4u" + "\r\n" +
				" fEDStL8OeL9Kmtru6it5pY0SUwbmALbVlOccnBxSuw0Plr4kftS+NvG8uowx6nLpelX7bRY2u1" + "\r\n" +
				" RHHgAJ5gUOcjryAxPTHFepfssftB+Afh78AL7w98R5p4Li5kuPtVqltKx1BJV2/K6DCkp8nzMO" + "\r\n" +
				" mc17/+0f8A8EQx8Ctc1DQNU8WRy63HL/o87GNIXHYeTndznrvOPTrXiFv/AMElPiVcXoEUvh4W" + "\r\n" +
				" zE/vWnlGRkdvL5/PHHWnzCOd8KfthpB8bPhrqM+ktbfDn4Za5Dqem+HrfHyqs0cksrBjted/LU" + "\r\n" +
				" 8nHAGeSx7T46/t7eFblbXUfhVoI1LxVaRvHZ6pqVooGl78BmjByWfj/ZUepGVP0z8Lf+Danxr8" + "\r\n" +
				" TvgO/irwP4/8Py3kUbGTT7m0kQM684SaNnyCM8lRXguof8EetS0u3uIPGPjrQdO1VDtWEBCiHu" + "\r\n" +
				" GLyqf0HShO5bscxoP/AAUU8J+IvhJbaR8e/DF7r2p2kaCRDBDPa30ifdlbew2MSMn5T3xkHFcT" + "\r\n" +
				" o/8AwU3+I3h/44aL4p8P3SWWkaLrVnqsGhRKiwOltKriBpdvmFXCEPggHceAMAej6P8A8EnNMs" + "\r\n" +
				" tRVviD8UfDscAPMNtNBFK49neRgv8A3yail/4I/wB5rWsxjwN8RPDt1YzShQ0uwyRqT/sSEOR9" + "\r\n" +
				" Fz7VRK3Nf4qf8FPfAHiHTbXUtG8F3Or+I7F/tFgurW0Hl6bKcZdJss4IIXlQpbaMleKj8af8FG" + "\r\n" +
				" fh+9zB4q+HfhO81H4jTWA0+3lu4Aoswx3bS4YlwGY4CAFuRuUGqXi7/git4n8OeIZrY+PPCjWc" + "\r\n" +
				" TAfaZT5RA9Su8gfTP41teF/h98FP+Ce7/wBt6/rUHxL+IFqN1nDEFFpYvzhwoJAIP8TM2McAHq" + "\r\n" +
				" BdnXfFLTpf2N/+CX+nXXxAeJviz8afEuqXmtMzD7VDapBYGOCRQeAnnSsAAMNOw5Civz0aSWdi" + "\r\n" +
				" 7uSzncfmPeu7/aD/AGiPEP7SPjGXVPGly0x813iiBISFWx8iDPCgKP8APThQox96pchDJrdSxB" + "\r\n" +
				" HrUI3x8Ru659GIoooiN7gWldjvmlb6uTSpLM5/eSuwHYsTRRRIRtaLrd1pDhrGeSMg5+U4r0Lw" + "\r\n" +
				" 1+0n4z8PH/iS+IdVgHl+WQlwygr0xwaKKkroYN98VdcvrmSW91K9Z3Jdj5zHJ/E16V+zB+2T47" + "\r\n" +
				" /Zw+I1hrfw91/UbSS3lDSIsmVcdD8p4Jx60UUnsSZ/xy/as8c/F3xxqeqeKfE2tXhvLyScLNcs" + "\r\n" +
				" QhJJ4AOB17VyK/GnxPPCqDVboIeOJXBHHqDRRTA+rv2Lv+Cr/wAV/gF8M/E2geGNX82G50yT7J" + "\r\n" +
				" PNGJJbNuOUY9Op9a+Zfi1+0l4z+K/iCXV/GGt3U15J95oz5QP4LxRRRfUDzbWPF+qai+bu+uZD" + "\r\n" +
				" nqzkmn2XxL1+0VRZaxqEYXGAs7KBjp0NFFO7Amv/AIseJb/d9v1vUpd4w2+5ds/maxpbia/kBu" + "\r\n" +
				" 5GfPOCeKKKLsC3aWWbdHB+ZmIHt0q5EFaJSc8gdhRRSA//2Q==" + "\r\n" +
				"UID:" + uid + "\r\n" +
				"REV:" + super.formatAsUTC(new Date()) + "\r\n" +
				"PRODID:-//Apple Inc.//AddressBook 6.0//EN" + "\r\n" +
				"END:VCARD" + "\r\n"
		;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCard(uid, vCard));
        /*
         * verify contact on server
         */
        Contact contact = super.getContact(uid);
        super.rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertTrue("no contact image found", contact.containsImage1());
        /*
         * verify contact on client
         */
        final Map<String, String> eTags = super.syncCollection(syncToken);
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        final List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        final VCardResource card = assertContains(uid, addressData);
        assertNotNull("no PHOTO found in vCard", card.getVCard().getPhotos());
        assertTrue("no PHOTO found in vCard", 0 < card.getVCard().getPhotos().size());
        assertNotNull("no PHOTO data found in vCard", card.getVCard().getPhotos().get(0).getPhoto());
	}
}

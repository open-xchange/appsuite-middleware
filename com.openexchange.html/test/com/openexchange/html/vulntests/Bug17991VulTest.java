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

package com.openexchange.html.vulntests;

import org.junit.Test;
import com.openexchange.html.AbstractSanitizing;
import com.openexchange.html.AssertionHelper;


/**
 * {@link Bug17991VulTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class Bug17991VulTest extends AbstractSanitizing {
    @Test
    public void testSanitize() {
        String javaScript = "<script =" +
            "type=3D\"text/javascript\">//<!--" +
            "function isNodeText(A){return(A.nodeType=3D=3D3);}function object(B){var =" +
            "A=3Dfunction(){};A.prototype=3DB;return new A();" +
            "}function extend(C,A){for(var B in A){C[B]=3DA[B];}return C;}function =" +
            "forEach(B,D){for(var A=3D0;A<B.length;" +
            "A++){var C=3DD(B[A],A);if(C){break;}}}function map(C,D){var B=3D[];for(var=" +
            " A=3D0;A<C.length;A++){if(D){B.push(D(C[A],A));" +
            "}else{B.push(C[A]);}}return B;}function filter(B,D){var C=3D[];for(var =" +
            "A=3D0;A<B.length;A++){if(D(B[A],A)){C.push(B[A]);" +
            "}}return C;}function isArray(A){return A&&typeof =" +
            "A=3D=3D=3D\"object\"&&!(A.propertyIsEnumerable(\"length\"))&&typeof =" +
            "A.length=3D=3D=3D\"number\";" +
            "}var userAgent=3Dnavigator.userAgent.toLowerCase();var =" +
            "browser=3D{version:(userAgent.match(/.+(?:rv|it|ra|ie)[\\/: =" +
            "]([\\d.]+)/)||[])[1],safari:/webkit/.test(userAgent),opera:/opera/.test(use=" +
            "rAgent),msie:/msie/.test(userAgent)&&!/opera/.test(userAgent),mozilla:/moz=" +
            "illa/.test(userAgent)&&!/(compatible|webkit)/.test(userAgent),windows:/win=" +
            "dows/.test(userAgent)};" +
            "function getAssoc(B,A){return B[\"_magicdom_\"+A];}function =" +
            "setAssoc(C,A,B){C[\"_magicdom_\"+A]=3DB;}function =" +
            "binarySearch(A,E){if(A<1){return 0;" +
            "}if(E(0)){return 0;}if(!E(A-1)){return A;}var D=3D0;var =" +
            "B=3DA-1;while((B-D)>1){var C=3DMath.floor((D+B)/2);" +
            "if(E(C)){B=3DC;}else{D=3DC;}}return B;}function =" +
            "binarySearchInfinite(C,B){var A=3D0;while(!B(A)){A+=3DC;}return =" +
            "binarySearch(A,B);" +
            "}function htmlPrettyEscape(A){return =" +
            "A.replace(/&/g,\"&amp;\").replace(/</g,\"&lt;\").replace(/>/g,\"&gt;\").replace(=\n" +
            "/\\r?\\n/g,\"\\\\n\");" +
            "}function newSkipList{var =" +
            "H=3Dwindow.PROFILER;if(!H){H=3Dfunction(){return{start:G,mark:G,literal:G,=" +
            "end:G,cancel:G};" +
            "};}function G(){}var =" +
            "B=3D{key:null,levels:1,upPtrs:[null],downPtrs:[null],downSkips:[1],downSki=" +
            "</script>";
        String content = "<head>" + javaScript + "</head>";

        AssertionHelper.assertSanitizedDoesNotContain(getHtmlService(), content, javaScript);
    }
}

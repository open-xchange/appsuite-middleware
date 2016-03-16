
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

/**
 * {@link Wsdl2JavaInvocationGenerator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Wsdl2JavaInvocationGenerator {

    /**
     * Initializes a new {@link Wsdl2JavaInvocationGenerator}.
     */
    private Wsdl2JavaInvocationGenerator() {
        super();
    }

    /**
     * @param args
     */
    public static void main(final String[] args) throws Exception {
        final String wsdlUrl = args[0]; // "https://ox6-dev.open-xchange.com/servlet/axis2/services/OXUserService?wsdl";
        final String outputDirectory = args[1]; // "./OXUserService/";
        final String namespaces = args[2]; // "http://soap.admin.openexchange.com http://io.java/xsd http://dataobjects.soap.admin.openexchange.com/xsd "+
            //"http://rmi.java/xsd http://dataobjects.rmi.admin.openexchange.com/xsd http://exceptions.rmi.admin.openexchange.com/xsd";
        final String packageAppendix = args[3]; // "user"
        
        final StringBuilder sb = new StringBuilder(1024);
        sb.append("./wsdl2java");
        sb.append(" -b javabindings.xml");
        sb.append(" -impl");
        sb.append(" -frontend jaxws21");
        sb.append(" -wsdlLocation null");
        sb.append(" -server");
        // Output directory
        sb.append(" -d ").append(outputDirectory);
        // Namespace mappings
        for (final String namespace : namespaces.split(" +")) {
            final int pos1 = namespace.indexOf("://") + 3;
            final int pos2 = namespace.indexOf('.', pos1);
            sb.append(" -p ").append(namespace).append("=com.openexchange.admin.soap.").append(packageAppendix).append('.').append(namespace.substring(pos1, pos2));
        }
        sb.append(' ').append(wsdlUrl);
        
        System.out.println(sb.toString());
    }

}

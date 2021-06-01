
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

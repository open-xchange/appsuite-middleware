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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.rest.services.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies the {@link Role role} permitted to access certain REST end-points.
 * <p>
 * This annotation may be used in favor of {@link javax.annotation.security.RolesAllowed} annotation to work with enum-based constants.
 * <p>
 * Example:
 * <pre>
 *
 *   &#64;Path("/rest/v1")
 *   &#64;DenyAll
 *   public class MyRestEndPoint {
 *     // By default every method provided by this end-point is not accessible; unless annotated otherwise
 *
 *     ...
 *
 *     &#64;GET
 *     &#64;Path("/safedata")
 *     &#64;Produces(MediaType.APPLICATION_JSON)
 *     &#64;PermitAll
 *     public void publiclyAccessible() {
 *       // Everyone may access this method
 *       ...
 *     }
 *
 *     &#64;GET
 *     &#64;Path("/unsafedata")
 *     &#64;Produces(MediaType.APPLICATION_JSON)
 *     &#64;RoleAllowed(Role.BASIC_AUTHENTICATED)
 *     public void restrictedAccess() {
 *       // This method is secured with "basic-auth"
 *       ...
 *     }
 *
 *     ...
 *
 *   }
 *
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
@Documented
@Retention (RUNTIME)
@Target({TYPE, METHOD})
public @interface RoleAllowed {
    Role value();
}

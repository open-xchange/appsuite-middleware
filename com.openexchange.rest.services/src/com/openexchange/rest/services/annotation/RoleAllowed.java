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
 * The value of the RoleAllowed annotation is a {@link Role security role}. This annotation can be specified on a class or on method(s).
 * <ul>
 * <li>Specifying it at a class level means that it applies to all the methods in the class.</li>
 * <li>Specifying it on a method means that it is applicable to that method only.</li>
 * <li>If applied at both - the class and methods level, the method value overrides the class value if the two conflict.</li>
 * </ul>
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

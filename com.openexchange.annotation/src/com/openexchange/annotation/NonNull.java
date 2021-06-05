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

package com.openexchange.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Qualifier for a type in a method signature or a local variable declaration:
 * The entity (return value, parameter, field, local variable) whose type has this
 * annotation can never have the value <code>null</code> at runtime.
 * <p>
 * This has two consequences:
 * <ol>
 * <li>Dereferencing the entity is safe, i.e., no <code>NullPointerException</code> can occur at runtime.</li>
 * <li>An attempt to bind a <code>null</code> value to the entity is a compile time error.</li>
 * </ol>
 * For the second case, diagnostics issued by the compiler should distinguish three situations:
 * <ol>
 * <li>Nullness of the value can be statically determined, the entity is definitely bound from either of:
 *     <ul><li>the value <code>null</code>, or</li>
 *         <li>an entity with a {@link Nullable @Nullable} type.</li></ul></li>
 * <li>Nullness cannot definitely be determined, because different code branches yield different results.</li>
 * <li>Nullness cannot be determined, because other program elements are involved for which
 *     null annotations are lacking.</li>
 * </ol>
 * </p>
 * @since 7.4.0
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ FIELD, METHOD, PARAMETER, LOCAL_VARIABLE })
public @interface NonNull {
	// marker annotation with no members
}

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

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be applied to a package, type, method or constructor in order to
 * define that contained entities for which a null annotation is otherwise lacking
 * should be considered as {@link NonNull @NonNull}. Entities affected by
 * <code>@NonNullByDefault</code> are:
 * <ul>
 * <li>method return values</li>
 * <li>parameters of a method or constructor</li>
 * <li>fields.</li>
 * </ul>
 * Local variables are <em>not</em> affected.
 * <dl>
 * <dt>Canceling a default</dt>
 * <dd>By using a <code>@NonNullByDefault</code> annotation with the argument <code>false</code>,
 * a default from any enclosing scope can be canceled for the element being annotated.
 * <dt>Nested defaults</dt>
 * <dd>If a <code>@NonNullByDefault</code>
 * annotation is used within the scope of another <code>@NonNullByDefault</code>
 * annotation, the innermost annotation defines the
 * default applicable at any given position (depending on the parameter {@link #value()}).</dd>
 * </dl>
 * Note that for applying an annotation to a package, a file by the name
 * <code>package-info.java</code> is used.
 *
 * @since 7.4.0
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ PACKAGE, TYPE, METHOD, CONSTRUCTOR })
public @interface NonNullByDefault {
	/**
	 * When parameterized with <code>false</code>, the annotation specifies that the current element should not apply
	 * any default to un-annotated types.
	 */
	boolean value() default true;
}

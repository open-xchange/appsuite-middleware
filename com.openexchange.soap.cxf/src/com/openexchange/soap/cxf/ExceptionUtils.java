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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openexchange.soap.cxf;

/**
 * This class exists for compatibility reasons. Consider using {@link com.openexchange.exception.ExceptionUtils}
 * instead.
 *
 * @see com.openexchange.exception.ExceptionUtils
 */
public class ExceptionUtils {

    /**
     * Checks whether the supplied <tt>Throwable</tt> is one that needs to be re-thrown and swallows all others.
     * This method simply delegates to {@link com.openexchange.exception.ExceptionUtils#handleThrowable(Throwable)}.
     *
     * @param t The <tt>Throwable</tt> to check
     * @see com.openexchange.exception.ExceptionUtils#handleThrowable(Throwable)
     */
    public static void handleThrowable(final Throwable t) {
        com.openexchange.exception.ExceptionUtils.handleThrowable(t);
    }

}

/*
 * Copyright (C) 2015 Marten Gajda <marten@dmfs.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.dmfs.rfc5545;

/**
 * Enumeration of week days. The weekdays are ordered, so you can use the ordinal value to get the week day number (i.e. <code>Weekday.SU.ordinal() == 0</code>,
 * <code>Weekday.MO.ordinal() == 1 </code>...).
 * <p>
 * Please note that the ordinal value is not compatible with the day values of {@link java.util.Calendar}.
 * </p>
 */
public enum Weekday
{
	SU, MO, TU, WE, TH, FR, SA;
}

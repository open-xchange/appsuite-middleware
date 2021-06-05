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

package com.openexchange.authentication;

/**
 * {@link DefaultAuthenticated}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class DefaultAuthenticated implements Authenticated {

   private final String contextInfo;

   private final String userInfo;

   /**
    * Initializes a new {@link DefaultAuthenticated} with context info set to
    * {@code defaultcontext}.
    *
    * @param userInfo
    * @throws IllegalArgumentException if userInfo is null
    */
   public DefaultAuthenticated(String userInfo) {
       this(Authenticated.DEFAULT_CONTEXT_INFO, userInfo);
   }

   /**
    * Initializes a new {@link DefaultAuthenticated}.
    *
    * @param contextInfo
    * @param userInfo
    * @throws IllegalArgumentException if contextInfo or userInfo are null
    */
   public DefaultAuthenticated(String contextInfo, String userInfo) {
       super();
       if (contextInfo == null) {
           throw new IllegalArgumentException("contextInfo is null!");
       }

       if (userInfo == null) {
           throw new IllegalArgumentException("userInfo is null!");
       }

       this.contextInfo = contextInfo;
       this.userInfo = userInfo;
   }

   @Override
   public String getContextInfo() {
       return contextInfo;
   }

   @Override
   public String getUserInfo() {
       return userInfo;
   }

   @Override
   public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result + ((contextInfo == null) ? 0 : contextInfo.hashCode());
       result = prime * result + ((userInfo == null) ? 0 : userInfo.hashCode());
       return result;
   }

   @Override
   public boolean equals(Object obj) {
       if (this == obj)
           return true;
       if (obj == null)
           return false;
       if (getClass() != obj.getClass())
           return false;
       DefaultAuthenticated other = (DefaultAuthenticated) obj;
       if (contextInfo == null) {
           if (other.contextInfo != null)
               return false;
       } else if (!contextInfo.equals(other.contextInfo))
           return false;
       if (userInfo == null) {
           if (other.userInfo != null)
               return false;
       } else if (!userInfo.equals(other.userInfo))
           return false;
       return true;
   }

   @Override
   public String toString() {
       return "DefaultAuthenticated [contextInfo=" + contextInfo + ", userInfo=" + userInfo + "]";
   }

}

/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2015 Dandelion
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Dandelion nor the names of its contributors 
 * may be used to endorse or promote products derived from this software 
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.dandelion.core.util;

/**
 * <p>
 * Collection of utilities to ease working with enumerations.
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class EnumUtils {

   /**
    * Build a String containing all the possible values of the supplied
    * {@code enumClass}.
    * 
    * @param enumClass
    *           The enum from which the possible values must be displayed.
    * @return a String containing all the possible values separated by a comma.
    */
   public static <E extends Enum<E>> String printPossibleValuesOf(Class<E> enumClass) {

      StringBuilder possibleValues = new StringBuilder();
      E[] enumConstants = enumClass.getEnumConstants();

      for (int i = 0; i < enumConstants.length; i++) {
         possibleValues.append("'").append(enumConstants[i].name().toLowerCase()).append("'");
         if (i < enumConstants.length - 2) {
            possibleValues.append(", ");
         }
         else if (i == (enumConstants.length - 2)) {
            possibleValues.append(" and ");
         }
      }
      possibleValues.append(".");
      return possibleValues.toString();
   }

   /**
    * <p>
    * Gets the enum for the class, returning {@code null} if not found.
    * </p>
    * 
    * <p>
    * This method differs from {@link Enum#valueOf} in that it does not throw an
    * exception for an invalid enum name.
    * </p>
    * 
    * @param enumClass
    *           the class of the enum to query, not null.
    * @param enumName
    *           the enum name, null returns null.
    * @return the enum, null if not found.
    */
   public static <E extends Enum<E>> E getEnum(String enumName, Class<E> enumType) {
      if (enumName == null) {
         return null;
      }

      try {
         return Enum.valueOf(enumType, enumName.toUpperCase().trim());
      }
      catch (IllegalArgumentException ex) {
         return null;
      }
   }
}

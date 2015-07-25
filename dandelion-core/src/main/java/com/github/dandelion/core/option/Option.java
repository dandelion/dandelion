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
package com.github.dandelion.core.option;

import java.util.Map;

import com.github.dandelion.core.util.StringUtils;

/**
 * <p>
 * Representation of a component option, which is used to configure different
 * aspects of a component.
 * </p>
 * <p>
 * Each {@link Option} is composed of:
 * </p>
 * <ul>
 * <li>a uniq {@code name}, which may be used in configuration properties</li>
 * <li>an associated {@link OptionProcessor} which indicates how the value of
 * the option should be processed</li>
 * <li>a {@code precedence} to indicate if the {@link Option} should be
 * processed before another one</li>
 * </ul>
 * <p>
 * Options are registered in multiple ways:
 * </p>
 * <ul>
 * <li>Using one or more properties files</li>
 * <li>Using a JSP taglib</li>
 * <li>Using a Thymeleaf dialect</li>
 * <li>Using the API</li>
 * </ul>
 * <p>
 * Options are usually stored in {@link Map} structures, such as
 * {@code Map<Option<?>, Object>} where {@code Object} is the value of the
 * option.
 * </p>
 * 
 * @param <T>
 *           Type of the option.
 * @author Thibault Duchateau
 * @since 1.1.0
 * @see OptionProcessor
 */
public class Option<T> implements Comparable<Option<T>> {

   /**
    * Name of the option.
    */
   private final String name;

   /**
    * Associated option processor.
    */
   private final OptionProcessor processor;

   /**
    * Precedence of the option.
    */
   private final int precedence;

   /**
    * <p>
    * Constructor of {@link Option}.
    * </p>
    * 
    * @param name
    *           The name of the option.
    * @param processor
    *           The associated {@link OptionProcessor}.
    * @param precedence
    *           The precedence of the option.
    */
   public Option(String name, OptionProcessor processor, int precedence) {
      this.name = name;
      this.processor = processor;
      this.precedence = precedence;
   }

   /**
    * @return the name of the option.
    */
   public String getName() {
      return name;
   }

   /**
    * @return the {@link OptionProcessor} to be applied on the value associated
    *         with the option.
    */
   public OptionProcessor getProcessor() {
      return processor;
   }

   /**
    * @return the precedence of the option.
    */
   public int getPrecedence() {
      return this.precedence;
   }

   /**
    * <p>
    * Type-safe utility intended to retrieve the value associated to an
    * {@link Option}.
    * </p>
    * 
    * @param options
    *           A map containing {@link Option}s and their value.
    * @return the type-safe value associated to the option.
    */
   @SuppressWarnings("unchecked")
   public T valueFrom(Map<Option<?>, Object> options) {
      return (T) options.get(this);
   }

   /**
    * <p>
    * Type-safe utility intended to update a {@link Option}/value entry in the
    * passed {@code map}.
    * </p>
    * 
    * @param options
    *           A map containing {@link Option}s and their value.
    * @param value
    *           The value to set in the new entry.
    */
   public void setIn(Map<Option<?>, Object> options, T value) {
      options.put(this, (T) value);
   }

   /**
    * <p>
    * Type-safe utility intended to update {@link Option}/value entry in the
    * passed {@code map}, where the value is a {@link StringBuilder}.
    * </p>
    * <p>
    * If the entry already exists, the new value is appended to the existing
    * {@link StringBuilder}.
    * </p>
    * 
    * @param options
    *           A map containing {@link Option}s and their value.
    * @param value
    *           The value to set in the new entry.
    */
   public void appendIn(Map<Option<?>, Object> configurations, String value) {
      Object existingValue = configurations.get(this);
      if (StringUtils.isNotBlank(value)) {
         if (existingValue != null) {
            ((StringBuilder) existingValue).append(value);
         }
         else {
            configurations.put(this, new StringBuilder(value));
         }
      }
   }

   /**
    * <p>
    * Type-safe utility intended to update {@link Option}/value entry in the
    * passed {@code map}, where the value is a single character.
    * </p>
    * <p>
    * If the entry already exists, the new value is appended to the existing
    * {@link StringBuilder}.
    * </p>
    * 
    * @param options
    *           A map containing {@link Option}s and their value.
    * @param value
    *           The value to set in the new entry.
    */
   public void appendIn(Map<Option<?>, Object> options, char value) {
      Object existingValue = options.get(this);
      if (existingValue != null) {
         ((StringBuilder) existingValue).append(value);
      }
      else {
         options.put(this, new StringBuilder(value));
      }
   }

   @Override
   public String toString() {
      return "[" + this.name + "|" + this.processor.getClass().getSimpleName() + "|" + this.precedence + "]";
   }

   /**
    * <p>
    * Compare options according to their precedence.
    * </p>
    * 
    * <p>
    * Be careful: This implementation of compareTo breaks
    * <tt>(o1.compareTo(o2) == 0) == (o1.equals(o2))</tt>, as two different
    * options can have the same precedence.
    * </p>
    * 
    * @param o
    *           the object to compare to.
    * @return the comparison result.
    */
   @Override
   public int compareTo(Option<T> o) {
      if (o == null) {
         return 1;
      }
      if (!(o instanceof Option)) {
         // The other object does not rely on precedence, so we should delegate
         // to the other object (and its comparison policy) and invert the
         // result
         final int result = o.compareTo(this);
         return (-1) * result;
      }
      int thisPrecedence = this.getPrecedence();
      int otherPrecedence = o.getPrecedence();
      if (thisPrecedence < otherPrecedence) {
         return -1;
      }
      if (thisPrecedence > otherPrecedence) {
         return 1;
      }
      return 0;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof Option))
         return false;
      Option<?> other = (Option<?>) obj;
      if (name == null) {
         if (other.name != null)
            return false;
      }
      else if (!name.equals(other.name))
         return false;
      return true;
   }
}
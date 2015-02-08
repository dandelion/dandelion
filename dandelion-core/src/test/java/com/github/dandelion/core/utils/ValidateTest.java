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
package com.github.dandelion.core.utils;

import java.util.ArrayList;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.dandelion.core.util.Validate;

public class ValidateTest {

   @Rule
   public ExpectedException exception = ExpectedException.none();

   @Test
   public void should_fail_if_object_is_null() {
      exception.expect(IllegalArgumentException.class);
      exception.expectMessage("Cannot be null");
      Validate.notNull(null, "Cannot be null");
   }

   @Test
   public void should_fail_if_string_is_empty() {
      exception.expect(IllegalArgumentException.class);
      exception.expectMessage("Cannot be blank");
      Validate.notBlank("", "Cannot be blank");
   }

   @Test
   public void should_fail_if_array_is_empty() {
      exception.expect(IllegalArgumentException.class);
      exception.expectMessage("Cannot be empty");
      Validate.notEmpty(new ArrayList<String>(), "Cannot be empty");
   }

   @Test
   public void should_fail_if_boolean_is_false() {
      exception.expect(IllegalArgumentException.class);
      exception.expectMessage("Cannot be false");
      Validate.isTrue(false, "Cannot be false");
   }
}

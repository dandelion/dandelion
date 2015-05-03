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
package com.github.dandelion.core.web.handler;

import org.slf4j.Logger;

/**
 * <p>
 * Abstract base class for handlers.
 * </p>
 * <p>
 * Consider extending this class if you wish to insert a custom handler into the
 * chain.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public abstract class AbstractHandlerChain implements HandlerChain {

   /**
    * The next handler to be executed in the chain.
    */
   private HandlerChain nextHandler;

   @Override
   public void setNext(HandlerChain nextHandler) {
      this.nextHandler = nextHandler;
   }

   @Override
   public final void doHandle(HandlerContext context) {

      boolean shouldContinue = true;

      getLogger().trace("Checking applicability of {}", this.getClass().getSimpleName());
      boolean isApplicable = isApplicable(context);
      getLogger().trace("Handler {} is applicable: {}", this.getClass().getSimpleName(), isApplicable);

      if (isApplicable) {
         shouldContinue = this.handle(context);
         getLogger().trace("Handler chain continues: {}", shouldContinue);
      }

      if (nextHandler != null && shouldContinue) {
         nextHandler.doHandle(context);
      }
   }

   /**
    * @return the actual logger of the handler.
    */
   protected abstract Logger getLogger();

   /**
    * Called by start().
    * 
    * @param request
    *           the request parameter
    * @return a boolean that indicates whether the handler chain should continue
    *         handling request or not.
    */
   protected abstract boolean handle(HandlerContext context);

   /**
    * <p>
    * Compare (and therefore order) handlers according to their rank.
    * </p>
    * <p>
    * Warning: this implementation of compareTo breaks
    * <tt>(o1.compareTo(o2) == 0) == (o1.equals(o2))</tt>, as two different
    * handlers can have the same precedence.
    * </p>
    * 
    * @param o
    *           the object to compare to.
    * @return the comparison result.
    */
   @Override
   public int compareTo(HandlerChain o) {
      if (o == null) {
         return 1;
      }
      if (!(o instanceof AbstractHandlerChain)) {
         // The other object does not rely on rank, so we should delegate to
         // the other object (and its comparison policy) and invert the
         // result
         final int result = o.compareTo(this);
         return (-1) * result;
      }
      int thisRank = getRank();
      int otherRank = ((AbstractHandlerChain) o).getRank();
      if (thisRank > otherRank) {
         return 1;
      }
      if (thisRank < otherRank) {
         return -1;
      }
      return 0;
   }
}

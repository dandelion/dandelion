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
 * 
 * Copyright (c) 2002 Douglas Crockford (www.crockford.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * The Software shall be used for Good, not Evil.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.dandelion.core.asset.processor.vendor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;

/**
 * <p>
 * Copyright (c) 2006 John Reilly (www.inconspicuous.org) This work is a
 * translation from C to Java of jsmin.c published by Douglas Crockford.
 * Permission is hereby granted to use the Java version under the same
 * conditions as the jsmin.c on which it is based.
 * </p>
 * <p>
 * The original class has been slightly adapted to suit the Dandelion dev
 * guidelines.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 0.10.0
 */
public class JSMin {

   private static final int EOF = -1;

   private PushbackInputStream in;
   private OutputStream out;

   private int theA;
   private int theB;

   private int line;

   private int column;

   public JSMin(InputStream in, OutputStream out) {
      this.in = new PushbackInputStream(in);
      this.out = out;
      this.line = 0;
      this.column = 0;
   }

   /**
    * isAlphanum -- return true if the character is a letter, digit, underscore,
    * dollar sign, or non-ASCII character.
    */
   static boolean isAlphanum(int c) {
      return ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || c == '_' || c == '$'
            || c == '\\' || c > 126);
   }

   /**
    * get -- return the next character from stdin. Watch out for lookahead. If
    * the character is a control character, translate it to a space or linefeed.
    */
   int get() throws IOException {
      int c = in.read();

      if (c == '\n') {
         line++;
         column = 0;
      }
      else {
         column++;
      }

      if (c >= ' ' || c == '\n' || c == EOF) {
         return c;
      }

      if (c == '\r') {
         column = 0;
         return '\n';
      }

      return ' ';
   }

   /**
    * Get the next character without getting it.
    */
   int peek() throws IOException {
      int lookaheadChar = in.read();
      in.unread(lookaheadChar);
      return lookaheadChar;
   }

   /**
    * next -- get the next character, excluding comments. peek() is used to see
    * if a '/' is followed by a '/' or '*'.
    */
   int next() throws IOException, UnterminatedCommentException {
      int c = get();
      if (c == '/') {
         switch (peek()) {
         case '/':
            for (;;) {
               c = get();
               if (c <= '\n') {
                  return c;
               }
            }

         case '*':
            get();
            for (;;) {
               switch (get()) {
               case '*':
                  if (peek() == '/') {
                     get();
                     return ' ';
                  }
                  break;
               case EOF:
                  throw new UnterminatedCommentException(line, column);
               }
            }

         default:
            return c;
         }

      }
      return c;
   }

   /**
    * action -- do something! What you do is determined by the argument: 1
    * Output A. Copy B to A. Get the next B. 2 Copy B to A. Get the next B.
    * (Delete A). 3 Get the next B. (Delete B). action treats a string as a
    * single character. Wow! action recognizes a regular expression if it is
    * preceded by ( or , or =.
    */

   void action(int d) throws IOException, UnterminatedRegExpLiteralException, UnterminatedCommentException,
         UnterminatedStringLiteralException {
      switch (d) {
      case 1:
         out.write(theA);
      case 2:
         theA = theB;

         if (theA == '\'' || theA == '"') {
            for (;;) {
               out.write(theA);
               theA = get();
               if (theA == theB) {
                  break;
               }
               if (theA <= '\n') {
                  throw new UnterminatedStringLiteralException(line, column);
               }
               if (theA == '\\') {
                  out.write(theA);
                  theA = get();
               }
            }
         }

      case 3:
         theB = next();
         if (theB == '/' && (theA == '(' || theA == ',' || theA == '=' || theA == ':')) {
            out.write(theA);
            out.write(theB);
            for (;;) {
               theA = get();
               if (theA == '/') {
                  break;
               }
               else if (theA == '\\') {
                  out.write(theA);
                  theA = get();
               }
               else if (theA <= '\n') {
                  throw new UnterminatedRegExpLiteralException(line, column);
               }
               out.write(theA);
            }
            theB = next();
         }
      }
   }

   /**
    * jsmin -- Copy the input to the output, deleting the characters which are
    * insignificant to JavaScript. Comments will be removed. Tabs will be
    * replaced with spaces. Carriage returns will be replaced with linefeeds.
    * Most spaces and linefeeds will be removed.
    */
   public void jsmin() throws IOException, UnterminatedRegExpLiteralException, UnterminatedCommentException,
         UnterminatedStringLiteralException {
      theA = '\n';
      action(3);
      while (theA != EOF) {
         switch (theA) {
         case ' ':
            if (isAlphanum(theB)) {
               action(1);
            }
            else {
               action(2);
            }
            break;
         case '\n':
            switch (theB) {
            case '{':
            case '[':
            case '(':
            case '+':
            case '-':
               action(1);
               break;
            case ' ':
               action(3);
               break;
            default:
               if (isAlphanum(theB)) {
                  action(1);
               }
               else {
                  action(2);
               }
            }
            break;
         default:
            switch (theB) {
            case ' ':
               if (isAlphanum(theA)) {
                  action(1);
                  break;
               }
               action(3);
               break;
            case '\n':
               switch (theA) {
               case '}':
               case ']':
               case ')':
               case '+':
               case '-':
               case '"':
               case '\'':
                  action(1);
                  break;
               default:
                  if (isAlphanum(theA)) {
                     action(1);
                  }
                  else {
                     action(3);
                  }
               }
               break;
            default:
               action(1);
               break;
            }
         }
      }
      out.flush();
   }

   static class UnterminatedCommentException extends Exception {
      private static final long serialVersionUID = -1031883064286838803L;

      public UnterminatedCommentException(int line, int column) {
         super("Unterminated comment at line " + line + " and column " + column);
      }
   }

   static class UnterminatedStringLiteralException extends Exception {
      private static final long serialVersionUID = 5853975704595490818L;

      public UnterminatedStringLiteralException(int line, int column) {
         super("Unterminated string literal at line " + line + " and column " + column);
      }
   }

   static class UnterminatedRegExpLiteralException extends Exception {
      private static final long serialVersionUID = 2906145391620285654L;

      public UnterminatedRegExpLiteralException(int line, int column) {
         super("Unterminated regular expression at line " + line + " and column " + column);
      }
   }
}
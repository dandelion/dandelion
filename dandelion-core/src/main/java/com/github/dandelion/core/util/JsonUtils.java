package com.github.dandelion.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>
 * Utilities for dealing with Jackson mapper (serializing, deserializing).
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 1.0.0
 */
public final class JsonUtils {

   /**
    * Jackson mapper.
    */
   private static ObjectMapper mapper;

   static {
      mapper = new ObjectMapper();
      mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
      mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
      mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
   }

   public static <T> T read(InputStream source, Class<T> valueType) throws IOException {
      T retval = null;
      try {
         retval = mapper.readValue(source, valueType);
      }
      catch (IOException e) {
         throw e;
      }

      return retval;
   }

   public static <T> T read(String source, Class<T> valueType) {
      T retval = null;
      try {
         retval = mapper.readValue(source, valueType);
      }
      catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return retval;
   }

   public static <T> T readFile(StringBuilder filePath, Class<T> valueType) {
      return readFile(filePath.toString(), valueType);
   }

   public static <T> T readFile(String filePath, Class<T> valueType) {

      T retval = null;
      try {
         retval = mapper.readValue(new File(filePath), valueType);
      }
      catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return retval;
   }

   /**
    * <p>
    * Suppress default constructor for noninstantiability.
    * </p>
    */
   private JsonUtils() {
      throw new AssertionError();
   }
}

/*
 *  Copyright 2017 TWO SIGMA OPEN SOURCE, LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.twosigma.beakerx.inspect;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class MethodInspect {
  
    final static Pattern RETURN_TYPE_PATTERN = Pattern.compile("(.*) -> ([$a-zA-z0-9.]*)$");
    
    final static class MethodInspectDeserializer implements JsonDeserializer<MethodInspect> {

      @Override
      public MethodInspect deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject obj = json.getAsJsonObject();
        return new MethodInspect(obj.get("methodName").getAsString(), obj.get("javadoc").getAsString(), obj.get("signature").getAsString());
      }
    }
  
    String methodName;
    String javadoc;
    String signature;
    String returnType;
    
    public MethodInspect(String methodName, String javadoc, String signature) {
        this.methodName = methodName;
        this.javadoc = javadoc;
        initSignature(signature);
    }

    /**
     * Set the signature to the given value, after checking for return type and parsing it out separately
     */
    private void initSignature(String signature) {
      var m = RETURN_TYPE_PATTERN.matcher(signature);
      if(m.matches()) {
          this.signature = m.group(1);
          this.returnType = m.group(2);
      }
      else {
        this.signature = signature;
      }
    }

    public String getMethodName() {
        return methodName;
    }

    public String getJavadoc() {
        return javadoc;
    }

    public String getSignature() {
        return signature;
    }

    /**
     * The return type, if any was specified in the given signature, otherwise null
     */
    public String getReturnType() {
      return returnType;
    }
}

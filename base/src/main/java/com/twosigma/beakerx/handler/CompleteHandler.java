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
package com.twosigma.beakerx.handler;

import static com.twosigma.beakerx.handler.KernelHandlerWrapper.wrapBusyIdle;
import static com.twosigma.beakerx.kernel.msg.JupyterMessages.COMPLETE_REPLY;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.twosigma.beakerx.autocomplete.AutocompleteResult;
import com.twosigma.beakerx.kernel.KernelFunctionality;
import com.twosigma.beakerx.message.Header;
import com.twosigma.beakerx.message.Message;

public class CompleteHandler extends KernelHandler<Message> {

  public static final String STATUS = "status";
  public static final String MATCHES = "matches";
  public static final String CURSOR_END = "cursor_end";
  public static final String CURSOR_START = "cursor_start";
  public static final String CODE = "code";
  public static final String CURSOR_POS = "cursor_pos";


  public CompleteHandler(KernelFunctionality kernel) {
    super(kernel);
  }

  @Override
  public void handle(Message message) {
    wrapBusyIdle(kernel, message, () -> {
      handleMsg(message);
    });
  }

  private void handleMsg(Message message) {
    String code = ((String) message.getContent().get(CODE));
    int cursorPos = ((int) message.getContent().get(CURSOR_POS));
    AutocompleteResult autocomplete = kernel.autocomplete(code, cursorPos);
    Message reply = createMsg(message, cursorPos, autocomplete);
    send(reply);
  }

  private Message createMsg(Message message, int cursorPos, AutocompleteResult autocomplete) {
    Message reply = new Message(new Header(COMPLETE_REPLY, message.getHeader().getSession()));
    reply.setIdentities(message.getIdentities());
    reply.setParentHeader(message.getHeader());
    Map<String, Serializable> content = new HashMap<>();
    List<String> matches = autocomplete.getMatches();
    
    content.put(STATUS, "ok");
    content.put(MATCHES, matches.toArray());
    content.put(CURSOR_END, cursorPos);
    content.put(CURSOR_START, autocomplete.getStartIndex());
    
    List<String> typeInfos = autocomplete.getTypeInfos();
    if(typeInfos != null) {
      var metadata = new HashMap<String,Object>();
      var types = new ArrayList<Map<String,String>>();
      
      for (int i = 0; i < matches.size(); i++) {
        String match = matches.get(i);
        String matchTypeInfo = typeInfos.get(i);
        var typeInfo = new HashMap<String,String>();
        typeInfo.put("text", match);
        typeInfo.put("type", matchTypeInfo);
        typeInfo.put("label", autocomplete.getMatchInfos().get(i).label);
        types.add(typeInfo);
      }

      metadata.put("_jupyter_types_experimental", types);
      content.put("metadata", metadata);
    reply.setContent(content);
    return reply;
  }
}
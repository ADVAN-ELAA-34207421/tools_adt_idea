/*
* Copyright (C) 2015 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* THIS WILL BE REMOVED ONCE THE CODE GENERATOR IS INTEGRATED INTO THE BUILD.
*/
package com.android.tools.idea.editors.gfxtrace.rpc;

import com.android.tools.rpclib.binary.BinaryObject;
import com.android.tools.rpclib.binary.Decoder;
import com.android.tools.rpclib.binary.Encoder;
import com.android.tools.rpclib.binary.ObjectTypeID;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class AtomInfo implements BinaryObject {
  int Type;
  String Name;
  ParameterInfo[] Parameters;
  boolean IsCommand;
  boolean IsDrawCall;
  boolean IsEndOfFrame;
  String DocumentationUrl;

  // Constructs a default-initialized {@link AtomInfo}.
  public AtomInfo() {
  }

  // Constructs and decodes a {@link AtomInfo} from the {@link Decoder} d.
  public AtomInfo(Decoder d) throws IOException {
    decode(d);
  }

  // Getters
  public int getType() {
    return Type;
  }

  // Setters
  public void setType(int v) {
    Type = v;
  }

  public String getName() {
    return Name;
  }

  public void setName(String v) {
    Name = v;
  }

  public ParameterInfo[] getParameters() {
    return Parameters;
  }

  public void setParameters(ParameterInfo[] v) {
    Parameters = v;
  }

  public boolean getIsCommand() {
    return IsCommand;
  }

  public void setIsCommand(boolean v) {
    IsCommand = v;
  }

  public boolean getIsDrawCall() {
    return IsDrawCall;
  }

  public void setIsDrawCall(boolean v) {
    IsDrawCall = v;
  }

  public boolean getIsEndOfFrame() {
    return IsEndOfFrame;
  }

  public void setIsEndOfFrame(boolean v) {
    IsEndOfFrame = v;
  }

  public String getDocumentationUrl() {
    return DocumentationUrl;
  }

  public void setDocumentationUrl(String v) {
    DocumentationUrl = v;
  }

  @Override
  public void encode(@NotNull Encoder e) throws IOException {
    ObjectFactory.encode(e, this);
  }

  @Override
  public void decode(@NotNull Decoder d) throws IOException {
    ObjectFactory.decode(d, this);
  }

  @Override
  public ObjectTypeID type() {
    return ObjectFactory.AtomInfoID;
  }
}
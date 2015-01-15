/*
 * Copyright (C) 2014 The Android Open Source Project
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
 */
package com.android.tools.idea.editors.theme;

import org.jetbrains.annotations.NotNull;

public class TableLabel {
  public final @NotNull String myLabelName;
  public final int myRowPosition;

  public TableLabel(@NotNull String labelName, int rowPosition) {
    myLabelName = labelName;
    myRowPosition = rowPosition;
  }

  @NotNull
  public String getLabelName() {
    return myLabelName;
  }

  public int getRowPosition() {
    return myRowPosition;
  }

  @Override
  public String toString() {
    return myLabelName;
  }
}

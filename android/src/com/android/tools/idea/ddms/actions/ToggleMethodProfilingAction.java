/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.tools.idea.ddms.actions;

import com.android.ddmlib.Client;
import com.android.ddmlib.ClientData;
import com.android.tools.idea.ddms.DeviceContext;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import icons.AndroidIcons;
import org.jetbrains.android.util.AndroidBundle;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ToggleMethodProfilingAction extends ToggleAction {
  private final Project myProject;
  private final DeviceContext myDeviceContext;

  public ToggleMethodProfilingAction(@NotNull Project p, @NotNull DeviceContext context) {
    super(AndroidBundle.message("android.ddms.actions.methodprofile.start"),
          null,
          AndroidIcons.Ddms.StartMethodProfiling);

    myProject = p;
    myDeviceContext = context;
  }

  @Override
  public boolean isSelected(AnActionEvent e) {
    Client c = myDeviceContext.getSelectedClient();
    if (c == null) {
      return false;
    }

    ClientData cd = c.getClientData();
    return cd.getMethodProfilingStatus() == ClientData.MethodProfilingStatus.TRACER_ON ||
           cd.getMethodProfilingStatus() == ClientData.MethodProfilingStatus.SAMPLER_ON;
  }

  @Override
  public void setSelected(AnActionEvent e, boolean state) {
    Client c = myDeviceContext.getSelectedClient();
    if (c == null) {
      return;
    }

    ClientData cd = c.getClientData();
    try {
      if (cd.getMethodProfilingStatus() == ClientData.MethodProfilingStatus.TRACER_ON) {
        c.stopMethodTracer();
      }
      else {
        c.startMethodTracer();
      }
    }
    catch (IOException e1) {
      Messages.showErrorDialog(myProject, "Unexpected error while toggling method profiling: " + e1, "Method Profiling");
    }
  }

  @Override
  public void update(AnActionEvent e) {
    super.update(e);

    Client c = myDeviceContext.getSelectedClient();
    if (c == null) {
      e.getPresentation().setEnabled(false);
      return;
    }

    String text = c.getClientData().getMethodProfilingStatus() == ClientData.MethodProfilingStatus.TRACER_ON ?
                  AndroidBundle.message("android.ddms.actions.methodprofile.stop") :
                  AndroidBundle.message("android.ddms.actions.methodprofile.start");
    e.getPresentation().setText(text);
    e.getPresentation().setEnabled(true);
  }
}

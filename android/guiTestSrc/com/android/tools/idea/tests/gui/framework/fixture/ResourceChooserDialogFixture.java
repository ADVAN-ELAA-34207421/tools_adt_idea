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
package com.android.tools.idea.tests.gui.framework.fixture;

import org.fest.swing.core.Robot;
import org.fest.swing.core.matcher.DialogMatcher;
import org.fest.swing.core.matcher.JButtonMatcher;
import org.fest.swing.core.matcher.JLabelMatcher;
import org.fest.swing.driver.JTextComponentDriver;
import org.fest.swing.fixture.ComponentFixture;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class ResourceChooserDialogFixture extends ComponentFixture<Dialog> {

  @NotNull
  public static ResourceChooserDialogFixture findDialog(@NotNull Robot robot) {
    Dialog jDialog = robot.finder().find(DialogMatcher.withTitle("Select Resource Directory").andShowing());

    return new ResourceChooserDialogFixture(robot, jDialog);
  }

  private ResourceChooserDialogFixture(@NotNull Robot robot, Dialog target) {
    super(robot, target);
  }

  public void setDirectoryName(@NotNull String directory) {
    Container parent = robot.finder().find(target, JLabelMatcher.withText("Directory name:")).getParent();

    JTextComponent directoryField = robot.finder().findByType(parent, JTextComponent.class, true);
    JTextComponentDriver driver = new JTextComponentDriver(robot);
    driver.selectAll(directoryField);
    driver.setText(directoryField, directory);
  }

  @NotNull
  public ResourceChooserDialogFixture clickOK() {
    JButton button = robot.finder().find(target, JButtonMatcher.withText("OK").andShowing());
    robot.click(button);
    return this;
  }
}

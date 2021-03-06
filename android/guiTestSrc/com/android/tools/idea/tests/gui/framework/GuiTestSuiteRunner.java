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
package com.android.tools.idea.tests.gui.framework;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.Statement;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static com.android.SdkConstants.DOT_CLASS;
import static com.android.tools.idea.tests.gui.framework.GuiTests.GUI_TESTS_RUNNING_IN_SUITE_PROPERTY;
import static com.intellij.openapi.util.io.FileUtil.notNullize;

/**
 * Test runner that automatically includes all test classes that extend {@link GuiTestCase}.
 */
public class GuiTestSuiteRunner extends Suite {
  public GuiTestSuiteRunner(Class<?> suiteClass, RunnerBuilder builder) throws InitializationError {
    super(builder, suiteClass, getGuiTestClasses(suiteClass));
    System.setProperty(GUI_TESTS_RUNNING_IN_SUITE_PROPERTY, "true");
  }

  @NotNull
  private static Class<?>[] getGuiTestClasses(@NotNull Class<?> suiteClass) throws InitializationError {
    List<File> guiTestClassFiles = Lists.newArrayList();
    File parentDir = getParentDir(suiteClass);

    String packagePath = suiteClass.getPackage().getName().replace('.', File.separatorChar);
    int packagePathIndex = parentDir.getPath().indexOf(packagePath);
    assert packagePathIndex > -1;
    String testDirPath = parentDir.getPath().substring(0, packagePathIndex);

    findPotentialGuiTestClassFiles(parentDir, guiTestClassFiles);
    List<Class<?>> guiTestClasses = Lists.newArrayList();
    ClassLoader classLoader = suiteClass.getClassLoader();
    for (File classFile : guiTestClassFiles) {
      String path = classFile.getPath();
      String className = path.substring(testDirPath.length(), path.indexOf(DOT_CLASS)).replace(File.separatorChar, '.');
      try {
        Class<?> testClass = classLoader.loadClass(className);
        if (GuiTestCase.class.isAssignableFrom(testClass)) {
          guiTestClasses.add(testClass);
        }
      }
      catch (ClassNotFoundException e) {
        throw new InitializationError(e);
      }
    }
    return guiTestClasses.toArray(new Class<?>[guiTestClasses.size()]);
  }

  private static void findPotentialGuiTestClassFiles(@NotNull File directory, @NotNull List<File> guiTestClassFiles) {
    File[] children = notNullize(directory.listFiles());
    for (File child : children) {
      if (child.isDirectory()) {
        findPotentialGuiTestClassFiles(child, guiTestClassFiles);
        continue;
      }
      if (child.isFile() && !child.isHidden() && child.getName().endsWith("Test.class")) {
        guiTestClassFiles.add(child);
      }
    }
  }

  @NotNull
  private static File getParentDir(@NotNull Class<?> clazz) throws InitializationError {
    URL classUrl = clazz.getResource(clazz.getSimpleName() + DOT_CLASS);
    try {
      return new File(classUrl.toURI()).getParentFile();
    }
    catch (URISyntaxException e) {
      throw new InitializationError(e);
    }
  }

  @Override
  @NotNull
  protected Statement childrenInvoker(final RunNotifier notifier) {
    return new Statement() {
      @Override
      public void evaluate() {
        // Run all the tests and dispose IdeTestApplication at the end.
        for (final Runner child : getChildren()) {
          runChild(child, notifier);
        }
        IdeTestApplication.disposeInstance();
      }
    };
  }
}

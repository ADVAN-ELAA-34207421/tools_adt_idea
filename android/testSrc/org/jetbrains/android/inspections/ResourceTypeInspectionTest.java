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
package org.jetbrains.android.inspections;

import com.android.resources.ResourceType;
import com.android.tools.idea.startup.ExternalAnnotationsSupport;
import com.google.common.collect.Lists;
import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.siyeh.ig.LightInspectionTestCase;
import org.intellij.lang.annotations.Language;
import org.jetbrains.android.AndroidTestBase;
import org.jetbrains.android.AndroidTestCase;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ResourceTypeInspectionTest extends LightInspectionTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    //noinspection StatementWithEmptyBody
    if (getName().equals("testNotAndroid")) {
      // Don't add an Android facet here; we're testing that we're a no-op outside of Android projects
      // since the inspection is registered at the .java source type level
      return;
    }

    // Module must have Android facet or resource type inspection will become a no-op
    if (AndroidFacet.getInstance(myModule) == null) {
      String sdkPath = AndroidTestBase.getDefaultTestSdkPath();
      String platform = AndroidTestBase.getDefaultPlatformDir();
      AndroidTestCase.addAndroidFacet(myModule, sdkPath, platform, true);
      Sdk sdk = ModuleRootManager.getInstance(myModule).getSdk();
      assertNotNull(sdk);
      @SuppressWarnings("SpellCheckingInspection") SdkModificator sdkModificator = sdk.getSdkModificator();
      ExternalAnnotationsSupport.attachJdkAnnotations(sdkModificator);
      sdkModificator.commitChanges();
    }
  }

  public void testTypes() {
    doCheck("import android.annotation.SuppressLint;\n" +
            "import android.annotation.TargetApi;\n" +
            "import android.app.Notification;\n" +
            "import android.content.Context;\n" +
            "import android.content.Intent;\n" +
            "import android.content.ServiceConnection;\n" +
            "import android.content.res.Resources;\n" +
            "import android.os.Build;\n" +
            "import android.support.annotation.DrawableRes;\n" +
            "import android.view.View;\n" +
            "\n" +
            "import static android.content.Context.CONNECTIVITY_SERVICE;\n" +
            "\n" +
            "@SuppressWarnings(\"UnusedDeclaration\")\n" +
            "public class X {\n" +
            "    public void testResourceTypeParameters(Context context, int unknown) {\n" +
            "        Resources resources = context.getResources();\n" +
            "        String ok1 = resources.getString(R.string.app_name);\n" +
            "        String ok2 = resources.getString(unknown);\n" +
            "        String ok3 = resources.getString(android.R.string.ok);\n" +
            "        int ok4 = resources.getColor(android.R.color.black);\n" +
            "        if (testResourceTypeReturnValues(context, true) == R.drawable.ic_launcher) { // ok\n" +
            "        }\n" +
            "\n" +
            "        //String ok2 = resources.getString(R.string.app_name, 1, 2, 3);\n" +
            "        float error1 = resources.getDimension(/*Expected resource of type dimen*/R.string.app_name/**/);\n" +
            "        boolean error2 = resources.getBoolean(/*Expected resource of type bool*/R.string.app_name/**/);\n" +
            "        boolean error3 = resources.getBoolean(/*Expected resource of type bool*/android.R.drawable.btn_star/**/);\n" +
            "        if (testResourceTypeReturnValues(context, true) == /*Expected resource of type drawable*/R.string.app_name/**/) {\n" +
            "        }\n" +
            "        @SuppressWarnings(\"UnnecessaryLocalVariable\")\n" +
            "        int flow = R.string.app_name;\n" +
            "        @SuppressWarnings(\"UnnecessaryLocalVariable\")\n" +
            "        int flow2 = flow;\n" +
            "        boolean error4 = resources.getBoolean(/*Expected resource of type bool*/flow2/**/);\n" +
            "    }\n" +
            "\n" +
            "    @android.support.annotation.DrawableRes\n" +
            "    public int testResourceTypeReturnValues(Context context, boolean useString) {\n" +
            "        if (useString) {\n" +
            "            return /*Expected resource of type drawable*/R.string.app_name/**/; // error\n" +
            "        } else {\n" +
            "            return R.drawable.ic_launcher; // ok\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    public static final class R {\n" +
            "        public static final class drawable {\n" +
            "            public static final int ic_launcher=0x7f020057;\n" +
            "        }\n" +
            "        public static final class string {\n" +
            "            public static final int app_name=0x7f0a000e;\n" +
            "        }\n" +
            "    }\n" +
            "}\n");
  }

  public void testIntDef() {
    doCheck("import android.annotation.SuppressLint;\n" +
            "import android.annotation.TargetApi;\n" +
            "import android.app.Notification;\n" +
            "import android.content.Context;\n" +
            "import android.content.Intent;\n" +
            "import android.content.ServiceConnection;\n" +
            "import android.content.res.Resources;\n" +
            "import android.os.Build;\n" +
            "import android.support.annotation.DrawableRes;\n" +
            "import android.view.View;\n" +
            "\n" +
            "import static android.content.Context.CONNECTIVITY_SERVICE;\n" +
            "\n" +
            "@SuppressWarnings(\"UnusedDeclaration\")\n" +
            "public class X {\n" +
            "\n" +
            "    @TargetApi(Build.VERSION_CODES.KITKAT)\n" +
            "    public void testStringDef(Context context, String unknown) {\n" +
            "        Object ok1 = context.getSystemService(unknown);\n" +
            "        Object ok2 = context.getSystemService(Context.CLIPBOARD_SERVICE);\n" +
            "        Object ok3 = context.getSystemService(android.content.Context.WINDOW_SERVICE);\n" +
            "        Object ok4 = context.getSystemService(CONNECTIVITY_SERVICE);\n" +
            "    }\n" +
            "\n" +
            "    @SuppressLint(\"UseCheckPermission\")\n" +
            "    @TargetApi(Build.VERSION_CODES.KITKAT)\n" +
            "    public void testIntDef(Context context, int unknown, View view) {\n" +
            "        view.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); // OK\n" +
            "        view.setLayoutDirection(/*Must be one of: View.LAYOUT_DIRECTION_LTR, View.LAYOUT_DIRECTION_RTL, View.LAYOUT_DIRECTION_INHERIT, View.LAYOUT_DIRECTION_LOCALE*/View.TEXT_ALIGNMENT_TEXT_START/**/); // Error\n" +
            "        view.setLayoutDirection(/*Must be one of: View.LAYOUT_DIRECTION_LTR, View.LAYOUT_DIRECTION_RTL, View.LAYOUT_DIRECTION_INHERIT, View.LAYOUT_DIRECTION_LOCALE*/View.LAYOUT_DIRECTION_RTL | View.LAYOUT_DIRECTION_RTL/**/); // Error\n" +
            "    }\n" +
            "\n" +
            "    @TargetApi(Build.VERSION_CODES.KITKAT)\n" +
            "    public void testIntDefFlags(Context context, int unknown, Intent intent,\n" +
            "                           ServiceConnection connection) {\n" +
            "        // Flags\n" +
            "        Object ok1 = context.bindService(intent, connection, 0);\n" +
            "        Object ok2 = context.bindService(intent, connection, -1);\n" +
            "        Object ok3 = context.bindService(intent, connection, Context.BIND_ABOVE_CLIENT);\n" +
            "        Object ok4 = context.bindService(intent, connection, Context.BIND_ABOVE_CLIENT\n" +
            "                | Context.BIND_AUTO_CREATE);\n" +
            "        int flags1 = Context.BIND_ABOVE_CLIENT | Context.BIND_AUTO_CREATE;\n" +
            "        Object ok5 = context.bindService(intent, connection, flags1);\n" +
            "\n" +
            "        Object error1 = context.bindService(intent, connection,\n" +
            "                /*Must be one or more of: Context.BIND_AUTO_CREATE, Context.BIND_DEBUG_UNBIND, Context.BIND_NOT_FOREGROUND, Context.BIND_ABOVE_CLIENT, Context.BIND_ALLOW_OOM_MANAGEMENT, Context.BIND_WAIVE_PRIORITY, Context.BIND_IMPORTANT, Context.BIND_ADJUST_WITH_ACTIVITY*/Context.BIND_ABOVE_CLIENT | Context.CONTEXT_IGNORE_SECURITY/**/);\n" +
            "        int flags2 = Context.BIND_ABOVE_CLIENT | Context.CONTEXT_IGNORE_SECURITY;\n" +
            "        Object error2 = context.bindService(intent, connection, /*Must be one or more of: Context.BIND_AUTO_CREATE, Context.BIND_DEBUG_UNBIND, Context.BIND_NOT_FOREGROUND, Context.BIND_ABOVE_CLIENT, Context.BIND_ALLOW_OOM_MANAGEMENT, Context.BIND_WAIVE_PRIORITY, Context.BIND_IMPORTANT, Context.BIND_ADJUST_WITH_ACTIVITY*/flags2/**/);\n" +
            "    }\n" +
            "}\n");
  }

  public void testFlow() {
    doCheck("import android.content.res.Resources;\n" +
            "import android.support.annotation.DrawableRes;\n" +
            "import android.support.annotation.StringRes;\n" +
            "import android.support.annotation.StyleRes;\n" +
            "\n" +
            "import java.util.Random;\n" +
            "\n" +
            "@SuppressWarnings(\"UnusedDeclaration\")\n" +
            "public class X {\n" +
            "    public void testLiterals(Resources resources) {\n" +
            "        resources.getDrawable(0); // OK\n" +
            "        resources.getDrawable(-1); // OK\n" +
            "        resources.getDrawable(/*Expected resource of type drawable*/10/**/); // ERROR\n" +
            "    }\n" +
            "\n" +
            "    public void testConstants(Resources resources) {\n" +
            "        resources.getDrawable(R.drawable.my_drawable); // OK\n" +
            "        resources.getDrawable(/*Expected resource of type drawable*/R.string.my_string/**/); // ERROR\n" +
            "    }\n" +
            "\n" +
            "    public void testFields(String fileExt, Resources resources) {\n" +
            "        int mimeIconId = MimeTypes.styleAndDrawable;\n" +
            "        resources.getDrawable(mimeIconId); // OK\n" +
            "\n" +
            "        int s1 = MimeTypes.style;\n" +
            "        resources.getDrawable(/*Expected resource of type drawable*/s1/**/); // ERROR\n" +
            "        int s2 = MimeTypes.styleAndDrawable;\n" +
            "        resources.getDrawable(s2); // OK\n" +
            "        int w3 = MimeTypes.drawable;\n" +
            "        resources.getDrawable(w3); // OK\n" +
            "\n" +
            "        // Direct reference\n" +
            "        resources.getDrawable(/*Expected resource of type drawable*/MimeTypes.style/**/); // ERROR\n" +
            "        resources.getDrawable(MimeTypes.styleAndDrawable); // OK\n" +
            "        resources.getDrawable(MimeTypes.drawable); // OK\n" +
            "    }\n" +
            "\n" +
            "    public void testCalls(String fileExt, Resources resources) {\n" +
            "        int mimeIconId = MimeTypes.getIconForExt(fileExt);\n" +
            "        resources.getDrawable(mimeIconId); // OK\n" +
            "        resources.getDrawable(MimeTypes.getInferredString()); // OK (wrong but can't infer type)\n" +
            "        resources.getDrawable(MimeTypes.getInferredDrawable()); // OK\n" +
            "        resources.getDrawable(/*Expected resource of type drawable*/MimeTypes.getAnnotatedString()/**/); // Error\n" +
            "        resources.getDrawable(MimeTypes.getAnnotatedDrawable()); // OK\n" +
            "        resources.getDrawable(MimeTypes.getUnknownType()); // OK (unknown/uncertain)\n" +
            "    }\n" +
            "\n" +
            "    private static class MimeTypes {\n" +
            "        @android.support.annotation.StyleRes\n" +
            "        @android.support.annotation.DrawableRes\n" +
            "        public static int styleAndDrawable;\n" +
            "\n" +
            "        @android.support.annotation.StyleRes\n" +
            "        public static int style;\n" +
            "\n" +
            "        @android.support.annotation.DrawableRes\n" +
            "        public static int drawable;\n" +
            "\n" +
            "        @android.support.annotation.DrawableRes\n" +
            "        public static int getIconForExt(String ext) {\n" +
            "            return R.drawable.my_drawable;\n" +
            "        }\n" +
            "\n" +
            "        public static int getInferredString() {\n" +
            "            // Implied string - can we handle this?\n" +
            "            return R.string.my_string;\n" +
            "        }\n" +
            "\n" +
            "        public static int getInferredDrawable() {\n" +
            "            // Implied drawable - can we handle this?\n" +
            "            return R.drawable.my_drawable;\n" +
            "        }\n" +
            "\n" +
            "        @android.support.annotation.StringRes\n" +
            "        public static int getAnnotatedString() {\n" +
            "            return R.string.my_string;\n" +
            "        }\n" +
            "\n" +
            "        @android.support.annotation.DrawableRes\n" +
            "        public static int getAnnotatedDrawable() {\n" +
            "            return R.drawable.my_drawable;\n" +
            "        }\n" +
            "\n" +
            "        public static int getUnknownType() {\n" +
            "            return new Random(1000).nextInt();\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    public static final class R {\n" +
            "        public static final class drawable {\n" +
            "            public static final int my_drawable =0x7f020057;\n" +
            "        }\n" +
            "        public static final class string {\n" +
            "            public static final int my_string =0x7f0a000e;\n" +
            "        }\n" +
            "    }\n" +
            "}\n");
  }

  public void testColorAsDrawable() {
    doCheck("package p1.p2;\n" +
            "\n" +
            "import android.content.Context;\n" +
            "import android.view.View;\n" +
            "\n" +
            "public class X {\n" +
            "    static void test(Context context) {\n" +
            "        View separator = new View(context);\n" +
            "        separator.setBackgroundResource(android.R.color.black);\n" +
            "    }\n" +
            "}\n");
  }

  public void testMipmap() {
    doCheck("package p1.p2;\n" +
            "\n" +
            "import android.app.Activity;\n" +
            "\n" +
            "public class X extends Activity {\n" +
            "  public void test() {\n" +
            "    Object o = getResources().getDrawable(R.mipmap.ic_launcher);\n" +
            "  }\n" +
            "\n" +
            "  public static final class R {\n" +
            "    public static final class drawable {\n" +
            "      public static int icon=0x7f020000;\n" +
            "    }\n" +
            "    public static final class mipmap {\n" +
            "      public static int ic_launcher=0x7f020001;\n" +
            "    }\n" +
            "  }\n" +
            "}");
  }

  public void testRanges() {
    doCheck("import android.support.annotation.FloatRange;\n" +
            "import android.support.annotation.IntRange;\n" +
            "import android.support.annotation.Size;\n" +
            "\n" +
            "@SuppressWarnings(\"UnusedDeclaration\")\n" +
            "class X {\n" +
            "    public void printExact(@Size(5) String arg) { System.out.println(arg); }\n" +
            "    public void printMin(@Size(min=5) String arg) { }\n" +
            "    public void printMax(@Size(max=8) String arg) { }\n" +
            "    public void printRange(@Size(min=4,max=6) String arg) { }\n" +
            "    public void printExact(@Size(5) int[] arg) { }\n" +
            "    public void printMin(@Size(min=5) int[] arg) { }\n" +
            "    public void printMax(@Size(max=8) int[] arg) { }\n" +
            "    public void printRange(@Size(min=4,max=6) int[] arg) { }\n" +
            "    public void printMultiple(@Size(multiple=3) int[] arg) { }\n" +
            "    public void printMinMultiple(@Size(min=4,multiple=3) int[] arg) { }\n" +
            "    public void printAtLeast(@IntRange(from=4) int arg) { }\n" +
            "    public void printAtMost(@IntRange(to=7) int arg) { }\n" +
            "    public void printBetween(@IntRange(from=4,to=7) int arg) { }\n" +
            "    public void printAtLeastInclusive(@FloatRange(from=2.5) float arg) { }\n" +
            "    public void printAtLeastExclusive(@FloatRange(from=2.5,fromInclusive=false) float arg) { }\n" +
            "    public void printAtMostInclusive(@FloatRange(to=7) double arg) { }\n" +
            "    public void printAtMostExclusive(@FloatRange(to=7,toInclusive=false) double arg) { }\n" +
            "    public void printBetweenFromInclusiveToInclusive(@FloatRange(from=2.5,to=5.0) float arg) { }\n" +
            "    public void printBetweenFromExclusiveToInclusive(@FloatRange(from=2.5,to=5.0,fromInclusive=false) float arg) { }\n" +
            "    public void printBetweenFromInclusiveToExclusive(@FloatRange(from=2.5,to=5.0,toInclusive=false) float arg) { }\n" +
            "    public void printBetweenFromExclusiveToExclusive(@FloatRange(from=2.5,to=5.0,fromInclusive=false,toInclusive=false) float arg) { }\n" +
            "\n" +
            "    public void testLength() {\n" +
            "        String arg = \"1234\";\n" +
            "        printExact(/*Length must be exactly 5*/arg/**/); // ERROR\n" +
            "\n" +
            "\n" +
            "        printExact(/*Length must be exactly 5*/\"1234\"/**/); // ERROR\n" +
            "        printExact(\"12345\"); // OK\n" +
            "        printExact(/*Length must be exactly 5*/\"123456\"/**/); // ERROR\n" +
            "\n" +
            "        printMin(/*Length must be at least 5 (was 4)*/\"1234\"/**/); // ERROR\n" +
            "        printMin(\"12345\"); // OK\n" +
            "        printMin(\"123456\"); // OK\n" +
            "\n" +
            "        printMax(\"123456\"); // OK\n" +
            "        printMax(\"1234567\"); // OK\n" +
            "        printMax(\"12345678\"); // OK\n" +
            "        printMax(/*Length must be at most 8 (was 9)*/\"123456789\"/**/); // ERROR\n" +
            "        printAtMost(1 << 2); // OK\n" +
            "        printMax(\"123456\" + \"\"); //OK\n" +
            "        printAtMost(/*Value must be ≤ 7 (was 8)*/1 << 2 + 1/**/); // ERROR\n" +
            "        printAtMost(/*Value must be ≤ 7 (was 32)*/1 << 5/**/); // ERROR\n" +
            "        printMax(/*Length must be at most 8 (was 11)*/\"123456\" + \"45678\"/**/); //ERROR\n" +
            "\n" +
            "        printRange(/*Length must be at least 4 and at most 6 (was 3)*/\"123\"/**/); // ERROR\n" +
            "        printRange(\"1234\"); // OK\n" +
            "        printRange(\"12345\"); // OK\n" +
            "        printRange(\"123456\"); // OK\n" +
            "        printRange(/*Length must be at least 4 and at most 6 (was 7)*/\"1234567\"/**/); // ERROR\n" +
            "    }\n" +
            "\n" +
            "    public void testSize() {\n" +
            "        printExact(/*Size must be exactly 5*/new int[]{1, 2, 3, 4}/**/); // ERROR\n" +
            "        printExact(new int[]{1, 2, 3, 4, 5}); // OK\n" +
            "        printExact(/*Size must be exactly 5*/new int[]{1, 2, 3, 4, 5, 6}/**/); // ERROR\n" +
            "\n" +
            "        printMin(/*Size must be at least 5 (was 4)*/new int[]{1, 2, 3, 4}/**/); // ERROR\n" +
            "        printMin(new int[]{1, 2, 3, 4, 5}); // OK\n" +
            "        printMin(new int[]{1, 2, 3, 4, 5, 6}); // OK\n" +
            "\n" +
            "        printMax(new int[]{1, 2, 3, 4, 5, 6}); // OK\n" +
            "        printMax(new int[]{1, 2, 3, 4, 5, 6, 7}); // OK\n" +
            "        printMax(new int[]{1, 2, 3, 4, 5, 6, 7, 8}); // OK\n" +
            "        printMax(new int[]{1, 2, 3, 4, 5, 6, 7, 8}); // OK\n" +
            "        printMax(/*Size must be at most 8 (was 9)*/new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}/**/); // ERROR\n" +
            "\n" +
            "        printRange(/*Size must be at least 4 and at most 6 (was 3)*/new int[] {1,2,3}/**/); // ERROR\n" +
            "        printRange(new int[] {1,2,3,4}); // OK\n" +
            "        printRange(new int[] {1,2,3,4,5}); // OK\n" +
            "        printRange(new int[] {1,2,3,4,5,6}); // OK\n" +
            "        printRange(/*Size must be at least 4 and at most 6 (was 7)*/new int[] {1,2,3,4,5,6,7}/**/); // ERROR\n" +
            "\n" +
            "        printMultiple(new int[] {1,2,3}); // OK\n" +
            "        printMultiple(/*Size must be a multiple of 3 (was 4)*/new int[] {1,2,3,4}/**/); // ERROR\n" +
            "        printMultiple(/*Size must be a multiple of 3 (was 5)*/new int[] {1,2,3,4,5}/**/); // ERROR\n" +
            "        printMultiple(new int[] {1,2,3,4,5,6}); // OK\n" +
            "        printMultiple(/*Size must be a multiple of 3 (was 7)*/new int[] {1,2,3,4,5,6,7}/**/); // ERROR\n" +
            "\n" +
            "        printMinMultiple(new int[] {1,2,3,4,5,6}); // OK\n" +
            "        printMinMultiple(/*Size must be at least 4 and a multiple of 3 (was 3)*/new int[]{1, 2, 3}/**/); // ERROR\n" +
            "    }\n" +
            "\n" +
            "    public void testIntRange() {\n" +
            "        printAtLeast(/*Value must be ≥ 4 (was 3)*/3/**/); // ERROR\n" +
            "        printAtLeast(4); // OK\n" +
            "        printAtLeast(5); // OK\n" +
            "\n" +
            "        printAtMost(5); // OK\n" +
            "        printAtMost(6); // OK\n" +
            "        printAtMost(7); // OK\n" +
            "        printAtMost(/*Value must be ≤ 7 (was 8)*/8/**/); // ERROR\n" +
            "\n" +
            "        printBetween(/*Value must be ≥ 4 and ≤ 7 (was 3)*/3/**/); // ERROR\n" +
            "        printBetween(4); // OK\n" +
            "        printBetween(5); // OK\n" +
            "        printBetween(6); // OK\n" +
            "        printBetween(7); // OK\n" +
            "        printBetween(/*Value must be ≥ 4 and ≤ 7 (was 8)*/8/**/); // ERROR\n" +
            "    }\n" +
            "\n" +
            "    public void testFloatRange() {\n" +
            "        printAtLeastInclusive(/*Value must be ≥ 2.5 (was 2.49f)*/2.49f/**/); // ERROR\n" +
            "        printAtLeastInclusive(2.5f); // OK\n" +
            "        printAtLeastInclusive(2.6f); // OK\n" +
            "\n" +
            "        printAtLeastExclusive(/*Value must be > 2.5 (was 2.49f)*/2.49f/**/); // ERROR\n" +
            "        printAtLeastExclusive(/*Value must be > 2.5 (was 2.5f)*/2.5f/**/); // ERROR\n" +
            "        printAtLeastExclusive(2.501f); // OK\n" +
            "\n" +
            "        printAtMostInclusive(6.8f); // OK\n" +
            "        printAtMostInclusive(6.9f); // OK\n" +
            "        printAtMostInclusive(7.0f); // OK\n" +
            "        printAtMostInclusive(/*Value must be ≤ 7.0 (was 7.1f)*/7.1f/**/); // ERROR\n" +
            "\n" +
            "        printAtMostExclusive(6.9f); // OK\n" +
            "        printAtMostExclusive(6.99f); // OK\n" +
            "        printAtMostExclusive(/*Value must be < 7.0 (was 7.0f)*/7.0f/**/); // ERROR\n" +
            "        printAtMostExclusive(/*Value must be < 7.0 (was 7.1f)*/7.1f/**/); // ERROR\n" +
            "\n" +
            "        printBetweenFromInclusiveToInclusive(/*Value must be ≥ 2.5 and ≤ 5.0 (was 2.4f)*/2.4f/**/); // ERROR\n" +
            "        printBetweenFromInclusiveToInclusive(2.5f); // OK\n" +
            "        printBetweenFromInclusiveToInclusive(3f); // OK\n" +
            "        printBetweenFromInclusiveToInclusive(5.0f); // OK\n" +
            "        printBetweenFromInclusiveToInclusive(/*Value must be ≥ 2.5 and ≤ 5.0 (was 5.1f)*/5.1f/**/); // ERROR\n" +
            "\n" +
            "        printBetweenFromExclusiveToInclusive(/*Value must be > 2.5 and ≤ 5.0 (was 2.4f)*/2.4f/**/); // ERROR\n" +
            "        printBetweenFromExclusiveToInclusive(/*Value must be > 2.5 and ≤ 5.0 (was 2.5f)*/2.5f/**/); // ERROR\n" +
            "        printBetweenFromExclusiveToInclusive(5.0f); // OK\n" +
            "        printBetweenFromExclusiveToInclusive(/*Value must be > 2.5 and ≤ 5.0 (was 5.1f)*/5.1f/**/); // ERROR\n" +
            "\n" +
            "        printBetweenFromInclusiveToExclusive(/*Value must be ≥ 2.5 and < 5.0 (was 2.4f)*/2.4f/**/); // ERROR\n" +
            "        printBetweenFromInclusiveToExclusive(2.5f); // OK\n" +
            "        printBetweenFromInclusiveToExclusive(3f); // OK\n" +
            "        printBetweenFromInclusiveToExclusive(4.99f); // OK\n" +
            "        printBetweenFromInclusiveToExclusive(/*Value must be ≥ 2.5 and < 5.0 (was 5.0f)*/5.0f/**/); // ERROR\n" +
            "\n" +
            "        printBetweenFromExclusiveToExclusive(/*Value must be > 2.5 and < 5.0 (was 2.4f)*/2.4f/**/); // ERROR\n" +
            "        printBetweenFromExclusiveToExclusive(/*Value must be > 2.5 and < 5.0 (was 2.5f)*/2.5f/**/); // ERROR\n" +
            "        printBetweenFromExclusiveToExclusive(2.51f); // OK\n" +
            "        printBetweenFromExclusiveToExclusive(4.99f); // OK\n" +
            "        printBetweenFromExclusiveToExclusive(/*Value must be > 2.5 and < 5.0 (was 5.0f)*/5.0f/**/); // ERROR\n" +
            "    }\n" +
            "}\n");
  }

  public void testColorInt() {
    doCheck("import android.app.Activity;\n" +
            "import android.graphics.Paint;\n" +
            "import android.widget.TextView;\n" +
            "\n" +
            "public class X extends Activity {\n" +
            "    public void foo(TextView textView, int foo) {\n" +
            "        Paint paint2 = new Paint();\n" +
            "        paint2.setColor(/*Should pass resolved color instead of resource id here: `getResources().getColor(R.color.blue)`*/R.color.blue/**/);\n" +
            "        // Wrong\n" +
            "        textView.setTextColor(/*Should pass resolved color instead of resource id here: `getResources().getColor(R.color.red)`*/R.color.red/**/);\n" +
            "        textView.setTextColor(/*Should pass resolved color instead of resource id here: `getResources().getColor(android.R.color.black)`*/android.R.color.black/**/);\n" +
            "        textView.setTextColor(/*Should pass resolved color instead of resource id here: `getResources().getColor(foo > 0 ? R.color.green : R.color.blue)`*/foo > 0 ? R.color.green : R.color.blue/**/);\n" +
            "        // OK\n" +
            "        textView.setTextColor(getResources().getColor(R.color.red));\n" +
            "        // OK\n" +
            "        foo1(R.color.blue);\n" +
            "        foo2(0xffff0000);\n" +
            "        // Wrong\n" +
            "        foo1(/*Expected resource of type color*/0xffff0000/**/);\n" +
            "        foo2(/*Should pass resolved color instead of resource id here: `getResources().getColor(R.color.blue)`*/R.color.blue/**/);\n" +
            "    }\n" +
            "\n" +
            "    private void foo1(@android.support.annotation.ColorRes int c) {\n" +
            "    }\n" +
            "\n" +
            "    private void foo2(@android.support.annotation./*Cannot resolve symbol 'ColorInt'*/ColorInt/**/ int c) {\n" +
            "    }\n" +
            "\n" +
            "    private static class R {\n" +
            "        private static class color {\n" +
            "            public static final int red=0x7f060000;\n" +
            "            public static final int green=0x7f060001;\n" +
            "            public static final int blue=0x7f060002;\n" +
            "        }\n" +
            "    }\n" +
            "}\n");
  }

  public void testCheckResult() {
    doCheck("import android.Manifest;\n" +
            "import android.content.Context;\n" +
            "import android.content.pm.PackageManager;\n" +
            "import android.graphics.Bitmap;\n" +
            "\n" +
            "public class X {\n" +
            "    private void foo(Context context) {\n" +
            "        /*The result of 'checkCallingOrSelfPermission' is not used; did you mean to call 'enforceCallingOrSelfPermission(String,String)'?*/context.checkCallingOrSelfPermission(Manifest.permission.INTERNET)/**/; // WRONG\n" +
            "        /*The result of 'checkPermission' is not used; did you mean to call 'enforcePermission(String,int,int,String)'?*/context.checkPermission(Manifest.permission.INTERNET, 1, 1)/**/;\n" +
            "        check(context.checkCallingOrSelfPermission(Manifest.permission.INTERNET)); // OK\n" +
            "        int check = context.checkCallingOrSelfPermission(Manifest.permission.INTERNET); // OK\n" +
            "        if (context.checkCallingOrSelfPermission(Manifest.permission.INTERNET) // OK\n" +
            "                != PackageManager.PERMISSION_GRANTED) {\n" +
            "            showAlert(context, \"Error\",\n" +
            "                    \"Application requires permission to access the Internet\");\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    private Bitmap checkResult(Bitmap bitmap) {\n" +
            "        /*The result 'extractAlpha' is not used*/bitmap.extractAlpha()/**/; // WARNING\n" +
            "        Bitmap bitmap2 = bitmap.extractAlpha(); // OK\n" +
            "        call(bitmap.extractAlpha()); // OK\n" +
            "        return bitmap.extractAlpha(); // OK\n" +
            "    }\n" +
            "\n" +
            "    private void showAlert(Context context, String error, String s) {\n" +
            "    }\n" +
            "\n" +
            "    private void check(int i) {\n" +
            "    }\n" +
            "    private void call(Bitmap bitmap) {\n" +
            "    }\n" +
            "}");
  }

  public void testNotAndroid() {
    doCheck("import android.Manifest;\n" +
            "import android.content.Context;\n" +
            "import android.content.pm.PackageManager;\n" +
            "import android.graphics.Bitmap;\n" +
            "\n" +
            "public class X {\n" +
            "    private Bitmap checkResult(Bitmap bitmap) {\n" +
            "        /*The result 'extractAlpha' is not used*/bitmap.extractAlpha()/**/; // WARNING\n" +
            "        Bitmap bitmap2 = bitmap.extractAlpha(); // OK\n" +
            "        call(bitmap.extractAlpha()); // OK\n" +
            "        return bitmap.extractAlpha(); // OK\n" +
            "    }\n" +
            "\n" +
            "    private void showAlert(Context context, String error, String s) {\n" +
            "    }\n" +
            "\n" +
            "    private void check(int i) {\n" +
            "    }\n" +
            "    private void call(Bitmap bitmap) {\n" +
            "    }\n" +
            "}");
  }

  @Override
  protected String[] getEnvironmentClasses() {
    String header = "package android.support.annotation;\n" +
                    "\n" +
                    "import java.lang.annotation.Documented;\n" +
                    "import java.lang.annotation.Retention;\n" +
                    "import java.lang.annotation.Target;\n" +
                    "\n" +
                    "import static java.lang.annotation.ElementType.CONSTRUCTOR;\n" +
                    "import static java.lang.annotation.ElementType.FIELD;\n" +
                    "import static java.lang.annotation.ElementType.LOCAL_VARIABLE;\n" +
                    "import static java.lang.annotation.ElementType.METHOD;\n" +
                    "import static java.lang.annotation.ElementType.PARAMETER;\n" +
                    "import static java.lang.annotation.RetentionPolicy.SOURCE;\n" +
                    "import static java.lang.annotation.RetentionPolicy.CLASS;\n" +
                    "\n";

    List<String> classes = Lists.newArrayList();
    classes.add(header +
                "@Retention(CLASS)\n" +
                "@Target({CONSTRUCTOR,METHOD,PARAMETER,FIELD,LOCAL_VARIABLE})\n" +
                "public @interface FloatRange {\n" +
                "    double from() default Double.NEGATIVE_INFINITY;\n" +
                "    double to() default Double.POSITIVE_INFINITY;\n" +
                "    boolean fromInclusive() default true;\n" +
                "    boolean toInclusive() default true;\n" +
                "}");

    classes.add(header +
                "@Retention(CLASS)\n" +
                "@Target({CONSTRUCTOR,METHOD,PARAMETER,FIELD,LOCAL_VARIABLE})\n" +
                "public @interface IntRange {\n" +
                "    long from() default Long.MIN_VALUE;\n" +
                "    long to() default Long.MAX_VALUE;\n" +
                "}");

    classes.add(header +
                "@Retention(CLASS)\n" +
                "@Target({PARAMETER, LOCAL_VARIABLE, METHOD, FIELD})\n" +
                "public @interface Size {\n" +
                "    long value() default -1;\n" +
                "    long min() default Long.MIN_VALUE;\n" +
                "    long max() default Long.MAX_VALUE;\n" +
                "    long multiple() default 1;\n" +
                "}");

    for (ResourceType type : ResourceType.values()) {
      if (type == ResourceType.FRACTION) {
        continue;
      }
      classes.add(header +
                  "@Documented\n" +
                  "@Retention(SOURCE)\n" +
                  "@Target({METHOD, PARAMETER, FIELD})\n" +
                  "public @interface " + StringUtil.capitalize(type.getName()) + "Res {\n" +
                  "}");
    }
    classes.add(header +
                "@Documented\n" +
                "@Retention(SOURCE)\n" +
                "@Target({METHOD, PARAMETER, FIELD})\n" +
                "public @interface AnyRes {\n" +
                "}");
    return ArrayUtil.toStringArray(classes);
  }

  // Like doTest in parent class, but uses <error> instead of <warning>
  protected final void doCheck(@Language("JAVA") @NotNull @NonNls String classText) {
    @NonNls final StringBuilder newText = new StringBuilder();
    int start = 0;
    int end = classText.indexOf("/*");
    while (end >= 0) {
      newText.append(classText, start, end);
      start = end + 2;
      end = classText.indexOf("*/", end);
      if (end < 0) {
        throw new IllegalArgumentException("invalid class text");
      }
      final String warning = classText.substring(start, end);
      if (warning.isEmpty()) {
        newText.append("</error>");
      }
      else {
        newText.append("<error descr=\"").append(warning).append("\">");
      }
      start = end + 2;
      end = classText.indexOf("/*", end + 1);
    }
    newText.append(classText, start, classText.length());

    // Now delegate to the real test implementation (it won't find comments to replace with <warning>)
    super.doTest(newText.toString());
  }

  @Nullable
  @Override
  protected InspectionProfileEntry getInspection() {
    return new ResourceTypeInspection();
  }
}

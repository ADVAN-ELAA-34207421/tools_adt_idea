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
package com.android.tools.idea.wizard;

import com.android.sdklib.repository.descriptors.IPkgDescAddon;
import com.android.sdklib.repository.descriptors.PkgType;
import com.android.tools.idea.configurations.DeviceMenuAction;
import com.android.tools.idea.sdk.remote.RemotePkgInfo;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.android.tools.idea.templates.TemplateMetadata.*;
import static com.android.tools.idea.wizard.FormFactorApiComboBox.AndroidTargetComboBoxItem;
import static com.android.tools.idea.wizard.ScopedStateStore.Key;
import static com.android.tools.idea.wizard.ScopedStateStore.Scope.STEP;
import static com.android.tools.idea.wizard.ScopedStateStore.Scope.WIZARD;
import static com.android.tools.idea.wizard.ScopedStateStore.createKey;
import static com.android.tools.idea.wizard.WizardConstants.INVALID_FILENAME_CHARS;

/**
 * Utility methods for dealing with Form Factors in Wizards.
 */
public class FormFactorUtils {
  public static final String INCLUDE_FORM_FACTOR = "included";
  public static final String ATTR_MODULE_NAME = "projectName";

  /** TODO: Turn into an enum and combine with {@link com.android.tools.idea.configurations.DeviceMenuAction.FormFactor} */
  public static class FormFactor {
    public static final FormFactor MOBILE = new FormFactor("Mobile", DeviceMenuAction.FormFactor.MOBILE, "Phone and Tablet", 15,
                                                           Lists.newArrayList("20", "Glass", "Google APIs"), null);
    public static final FormFactor WEAR = new FormFactor("Wear", DeviceMenuAction.FormFactor.WEAR, "Wear", 21,
                                                         null, Lists.newArrayList("20", "21"));
    public static final FormFactor GLASS = new FormFactor("Glass", DeviceMenuAction.FormFactor.GLASS, "Glass", 19,
                                                          null, Lists.newArrayList("Glass", "google_gdk"));
    public static final FormFactor TV = new FormFactor("TV", DeviceMenuAction.FormFactor.TV, "TV", 21,
                                                       Lists.newArrayList("20"), null);

    private static final Map<String, FormFactor> myFormFactors = new ImmutableMap.Builder<String, FormFactor>()
        .put(MOBILE.id, MOBILE)
        .put(WEAR.id, WEAR)
        .put(GLASS.id, GLASS)
        .put(TV.id, TV).build();

    public final String id;
    @Nullable private String displayName;
    public final int defaultApi;
    @NotNull private final List<String> myApiBlacklist;
    @NotNull private final List<String> myApiWhitelist;
    @NotNull private final DeviceMenuAction.FormFactor myEnumValue;

    FormFactor(@NotNull String id, @NotNull DeviceMenuAction.FormFactor enumValue, @Nullable String displayName,
               int defaultApi, @Nullable List<String> apiBlacklist, @Nullable List<String> apiWhitelist) {
      this.id = id;
      myEnumValue = enumValue;
      this.displayName = displayName;
      this.defaultApi = defaultApi;
      myApiBlacklist = apiBlacklist != null ? apiBlacklist : Collections.<String>emptyList();
      myApiWhitelist = apiWhitelist != null ? apiWhitelist : Collections.<String>emptyList();
    }

    @Nullable
    public static FormFactor get(@NotNull String id) {
      if (myFormFactors.containsKey(id)) {
        return myFormFactors.get(id);
      }
      return new FormFactor(id, DeviceMenuAction.FormFactor.MOBILE, id, 1, null, null);
    }

    @NotNull
    public DeviceMenuAction.FormFactor getEnumValue() {
      return myEnumValue;
    }

    @Override
    public String toString() {
      return displayName == null ? id : displayName;
    }

    @NotNull
    public Icon getIcon() {
      return myEnumValue.getIcon64();
    }

    public static Iterator<FormFactor> iterator() {
      return myFormFactors.values().iterator();
    }
  }

  @NotNull
  public static Key<AndroidTargetComboBoxItem> getTargetComboBoxKey(@NotNull FormFactor formFactor) {
    return createKey(formFactor.id + ATTR_MIN_API + "combo", STEP, AndroidTargetComboBoxItem.class);
  }

  @NotNull
  public static Key<Integer> getMinApiLevelKey(@NotNull FormFactor formFactor) {
    return createKey(formFactor.id + ATTR_MIN_API_LEVEL, WIZARD, Integer.class);
  }

  @NotNull
  public static Key<String> getMinApiKey(@NotNull FormFactor formFactor) {
    return createKey(formFactor.id + ATTR_MIN_API, WIZARD, String.class);
  }

  @NotNull
  public static Key<String> getBuildApiKey(@NotNull FormFactor formFactor) {
    return createKey(formFactor.id + ATTR_BUILD_API_STRING, WIZARD, String.class);
  }

  @NotNull
  public static Key<Integer> getTargetApiLevelKey(@NotNull FormFactor formFactor) {
    return createKey(formFactor.id + ATTR_TARGET_API, WIZARD, Integer.class);
  }

  @NotNull
  public static Key<String> getTargetApiStringKey(@NotNull FormFactor formFactor) {
    return createKey(formFactor.id + ATTR_TARGET_API_STRING, WIZARD, String.class);
  }

  @NotNull
  public static Key<Integer> getBuildApiLevelKey(@NotNull FormFactor formFactor) {
    return createKey(formFactor.id + ATTR_BUILD_API, WIZARD, Integer.class);
  }

  @NotNull
  public static Key<String> getLanguageLevelKey(@NotNull FormFactor formFactor) {
    return createKey(formFactor.id + ATTR_JAVA_VERSION, WIZARD, String.class);
  }

  @NotNull
  public static Key<Boolean> getInclusionKey(@NotNull FormFactor formFactor) {
    return createKey(formFactor.id + INCLUDE_FORM_FACTOR, WIZARD, Boolean.class);
  }

  @NotNull
  public static Key<String> getModuleNameKey(@NotNull FormFactor formFactor) {
    return createKey(formFactor.id + ATTR_MODULE_NAME, WIZARD, String.class);
  }

  public static Map<String, Object> scrubFormFactorPrefixes(@NotNull FormFactor formFactor, @NotNull Map<String, Object> values) {
    Map<String, Object> toReturn = Maps.newHashMapWithExpectedSize(values.size());
    for (String key : values.keySet()) {
      if (key.startsWith(formFactor.id)) {
        toReturn.put(key.substring(formFactor.id.length()), values.get(key));
      } else {
        toReturn.put(key, values.get(key));
      }
    }
    return toReturn;
  }

  public static String getPropertiesComponentMinSdkKey(@NotNull FormFactor formFactor) {
    return formFactor.id + ATTR_MIN_API;
  }

  @NotNull
  public static String getModuleName(@NotNull FormFactor formFactor) {
    String name = formFactor.id.replaceAll(INVALID_FILENAME_CHARS, "");
    name = name.replaceAll("\\s", "_");
    return name.toLowerCase();
  }

  public static Predicate<AndroidTargetComboBoxItem> getMinSdkComboBoxFilter(@NotNull final FormFactor formFactor, final int minSdkLevel) {
    return new Predicate<AndroidTargetComboBoxItem>() {
      @Override
      public boolean apply(@Nullable AndroidTargetComboBoxItem input) {
        if (input == null) {
          return false;
        }

        return doFilter(formFactor, minSdkLevel, input.target != null ? input.target.getName() : null, input.apiLevel) ||
               (input.target != null && input.target.getVersion().isPreview());
      }
    };
  }

  public static Predicate<RemotePkgInfo> getMinSdkPackageFilter(
    @NotNull final FormFactor formFactor, final int minSdkLevel) {
    return new Predicate<RemotePkgInfo>() {
      @Override
      public boolean apply(@Nullable RemotePkgInfo input) {
        if (input == null) {
          return false;
        }
        if (input.getPkgDesc().getType() == PkgType.PKG_ADDON) {
          IPkgDescAddon addon = (IPkgDescAddon)input.getPkgDesc();
          return doFilter(formFactor, minSdkLevel, addon.getName().getId(), addon.getAndroidVersion().getFeatureLevel());
        }
        // TODO: add other package types
        return false;
      }
    };
  }

  private static boolean doFilter(@NotNull FormFactor formFactor, int minSdkLevel, @Nullable String inputName, int targetSdkLevel) {
    if (!formFactor.myApiWhitelist.isEmpty()) {
      // If a whitelist is present, only allow things on the whitelist
      for (String filterItem : formFactor.myApiWhitelist) {
        if (matches(filterItem, inputName, targetSdkLevel)) {
          return true;
        }
      }
      return false;
    }

    // If we don't have a whitelist, let's check the blacklist
    for (String filterItem : formFactor.myApiBlacklist) {
      if (matches(filterItem, inputName, targetSdkLevel)) {
        return false;
      }
    }

    // Finally, we'll check that the minSDK is honored
    return targetSdkLevel >= minSdkLevel;
  }


  /**
   * @return true iff inputVersion is parsable as an int that matches filterItem, or if inputName contains filterItem.
   */
  private static boolean matches(@NotNull String filterItem, @Nullable String inputName, int inputVersion) {
    if (Integer.toString(inputVersion).equals(filterItem)) {
      return true;
    }
    if (inputName != null && inputName.contains(filterItem)) {
      return true;
    }
    return false;
  }

  /**
   * Create an image showing icons for each of the available form factors.
   * @param component Icon will be drawn in the context of the given {@code component}
   * @param requireEmulator If true, only include icons for form factors that have an emulator available.
   * @return The new Icon
   */
  @Nullable
  public static Icon getFormFactorsImage(JComponent component, boolean requireEmulator) {
    int width = 0;
    int height = 0;
    for (DeviceMenuAction.FormFactor formFactor : DeviceMenuAction.FormFactor.values()) {
      Icon icon = formFactor.getLargeIcon();
      height = icon.getIconHeight();
      if (!requireEmulator || formFactor.hasEmulator()) {
        width += formFactor.getLargeIcon().getIconWidth();
      }
    }
    //noinspection UndesirableClassUsage
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();
    int x = 0;
    for (DeviceMenuAction.FormFactor formFactor : DeviceMenuAction.FormFactor.values()) {
      if (requireEmulator && !formFactor.hasEmulator()) {
        continue;
      }
      Icon icon = formFactor.getLargeIcon();
      icon.paintIcon(component, graphics, x, 0);
      x += icon.getIconWidth();
    }
    if (graphics != null) {
      graphics.dispose();
      return new ImageIcon(image);
    }
    else {
      return null;
    }
  }
}

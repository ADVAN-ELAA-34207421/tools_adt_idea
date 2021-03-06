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
 */

package com.android.tools.idea.sdk;

import com.android.annotations.NonNull;
import com.android.sdklib.repository.descriptors.IPkgDesc;
import com.android.sdklib.repository.descriptors.PkgType;
import com.android.sdklib.repository.local.LocalPkgInfo;
import com.android.tools.idea.sdk.remote.RemotePkgInfo;
import com.android.tools.idea.sdk.remote.UpdatablePkgInfo;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

import java.util.Set;


/**
 * Store of current local and remote packages, in convenient forms.
 */
public final class SdkPackages {
  private final Set<UpdatablePkgInfo> myUpdatedPkgs = Sets.newTreeSet();
  private final Set<RemotePkgInfo> myNewPkgs = Sets.newTreeSet();
  private final long myTimestampMs;
  private Set<UpdatablePkgInfo> myConsolidatedPkgs = Sets.newTreeSet();
  private LocalPkgInfo[] myLocalPkgInfos = new LocalPkgInfo[0];
  private Multimap<PkgType, RemotePkgInfo> myRemotePkgInfos = TreeMultimap.create();

  SdkPackages() {
    myTimestampMs = System.currentTimeMillis();
  }

  public SdkPackages(LocalPkgInfo[] localPkgs, Multimap<PkgType, RemotePkgInfo> remotePkgs) {
    this();
    setLocalPkgInfos(localPkgs);
    setRemotePkgInfos(remotePkgs);
  }

  /**
   * Returns the timestamp (in {@link System#currentTimeMillis()} time) when this object was created.
   */
  public long getTimestampMs() {
    return myTimestampMs;
  }

  /**
   * Returns the set of packages that have local updates available.
   * Use {@link LocalPkgInfo#getUpdate()} to retrieve the computed updated candidate.
   *
   * @return A non-null, possibly empty Set of update candidates.
   */
  @NonNull
  public Set<UpdatablePkgInfo> getUpdatedPkgs() {
    return myUpdatedPkgs;
  }

  /**
   * Returns the set of new remote packages that are not locally present
   * and that the user could install.
   *
   * @return A non-null, possibly empty Set of new install candidates.
   */
  @NonNull
  public Set<RemotePkgInfo> getNewPkgs() {
    return myNewPkgs;
  }

  /**
   * Returns a set of {@link UpdatablePackageInfo}s representing all known local and remote packages. Remote packages corresponding
   * to local packages will be represented by a single item containing both the local and remote info..
   */
  @NonNull
  public Set<UpdatablePkgInfo> getConsolidatedPkgs() {
    return myConsolidatedPkgs;
  }

  @NonNull
  public LocalPkgInfo[] getLocalPkgInfos() {
    return myLocalPkgInfos;
  }

  public Multimap<PkgType, RemotePkgInfo> getRemotePkgInfos() {
    return myRemotePkgInfos;
  }

  void setLocalPkgInfos(LocalPkgInfo[] packages) {
    myLocalPkgInfos = packages;
    computeUpdates();
  }

  void setRemotePkgInfos(Multimap<PkgType, RemotePkgInfo> packages) {
    myRemotePkgInfos = packages;
    computeUpdates();
  }

  private void computeUpdates() {
    Set<UpdatablePkgInfo> newConsolidatedPkgs = Sets.newTreeSet();
    UpdatablePkgInfo[] updatablePkgInfos = new UpdatablePkgInfo[myLocalPkgInfos.length];
    for (int i = 0; i < myLocalPkgInfos.length; i++) {
      updatablePkgInfos[i] = new UpdatablePkgInfo(myLocalPkgInfos[i]);
    }
    Set<RemotePkgInfo> updates = Sets.newTreeSet();

    // Find updates to locally installed packages
    for (UpdatablePkgInfo info : updatablePkgInfos) {
      RemotePkgInfo update = findUpdate(info);
      if (update != null) {
        info.setRemote(update);
        myUpdatedPkgs.add(info);
        updates.add(update);
      }
      newConsolidatedPkgs.add(info);
    }

    // Find new packages not yet installed
    nextRemote: for (RemotePkgInfo remote : myRemotePkgInfos.values()) {
      if (updates.contains(remote)) {
        // if package is already a known update, it's not new.
        continue nextRemote;
      }
      IPkgDesc remoteDesc = remote.getPkgDesc();
      for (UpdatablePkgInfo info : updatablePkgInfos) {
        IPkgDesc localDesc = info.getLocalInfo().getDesc();
        if (remoteDesc.compareTo(localDesc) == 0 || remoteDesc.isUpdateFor(localDesc)) {
          // if package is same as an installed or is an update for an installed
          // one, then it's not new.
          continue nextRemote;
        }
      }

      myNewPkgs.add(remote);
      newConsolidatedPkgs.add(new UpdatablePkgInfo(remote));
    }
    myConsolidatedPkgs = newConsolidatedPkgs;
  }

  private RemotePkgInfo findUpdate(@NonNull UpdatablePkgInfo info) {
    RemotePkgInfo currUpdatePkg = null;
    IPkgDesc currUpdateDesc = null;
    IPkgDesc localDesc = info.getLocalInfo().getDesc();

    for (RemotePkgInfo remote: myRemotePkgInfos.get(localDesc.getType())) {
      IPkgDesc remoteDesc = remote.getPkgDesc();
      if ((currUpdateDesc == null && remoteDesc.isUpdateFor(localDesc)) ||
          (currUpdateDesc != null && remoteDesc.isUpdateFor(currUpdateDesc))) {
        currUpdatePkg = remote;
        currUpdateDesc = remoteDesc;
      }
    }

    return currUpdatePkg;
  }
}

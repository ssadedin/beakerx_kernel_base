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
package com.twosigma.beakerx.autocomplete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;

import static java.util.stream.Collectors.toList;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class AutocompleteResult {

  /**
   * A match and the type information associated with it
   * <p>
   * Note: in Jupyter the type info is not intended as data types 
   * but high level descriptors (function, attribute, etc). Here
   * however we use them as actual type descriptors to provide more
   * information back to users.
   */
  public static class MatchInfo {
    
    public MatchInfo(String match, String typeInfo) {
      this.match = match;
      this.typeInfo = typeInfo;
    }

    public MatchInfo(String match) {
      this.match = match;
      this.typeInfo = "";
    }

    public MatchInfo(String match, String typeInfo, String label) {
      this(match, typeInfo);
      this.label = label;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      return prime * result + Objects.hash(match, typeInfo, label);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;

      MatchInfo other = (MatchInfo) obj;
      return Objects.equals(match, other.match) 
          && Objects.equals(typeInfo, other.typeInfo)
          && Objects.equals(label, other.label);
    }

    public String match;
    public String typeInfo;
    public String label = "test label!!!";

    public String toString() {
      return reflectionToString(this);
    }
  }

  private List<MatchInfo> matchInfos;
  private int startIndex;
  
  public List<MatchInfo> getMatchInfos() {
    return Collections.unmodifiableList(this.matchInfos);
  }

   /**
   * Create an empty autocomplete result which can be populated using "append" fluent methods
   * 
   * @param startIndex
   */
  public AutocompleteResult(int startIndex) {
    this.matchInfos = new ArrayList<>();
    this.startIndex = startIndex;
  }

  public AutocompleteResult(List<String> matches, int startIndex, List<String> typeInfos) {
    this(startIndex);
    this.matchInfos = new ArrayList<>();
    for(int i=0; i<matches.size(); ++i) {
      this.matchInfos.add(new MatchInfo(matches.get(i), typeInfos.get(i)));
    }
  }

  public AutocompleteResult(List<String> matches, int startIndex) {
    this.matchInfos = matches.stream().map(MatchInfo::new).collect(toList());
    this.startIndex = startIndex;
  }

  public List<String> getMatches() {
    return matchInfos.stream().map(match -> match.match).collect(toList());
  }

  public int getStartIndex() {
    return startIndex;
  }
  
  public void setStartIndex(int startIndex) {
    this.startIndex = startIndex;
  }

  public List<String> getTypeInfos() {
    List<String> typeInfos = matchInfos.stream().map(match -> match.typeInfo).collect(toList());
    if(typeInfos.stream().allMatch(t -> t.isEmpty())) {
      return null;
    }
    return typeInfos;
  }

  @Override
  public boolean equals(Object o) {
    return reflectionEquals(this, o);
  }

  @Override
  public int hashCode() {
    return reflectionHashCode(this);
  }

  @Override
  public String toString() {
    return reflectionToString(this);
  }
  
  public AutocompleteResult add(String match) {
    return this.append(new AutocompleteResult(Collections.singletonList(match), startIndex));
  }

  public AutocompleteResult add(String match, String typeInfo) {
    return this.append(new AutocompleteResult(Collections.singletonList(match), startIndex, Collections.singletonList(typeInfo)));
  }

  public AutocompleteResult addAll(List<String> matches, String typeInfo) {
    String [] values = new String[matches.size()];
    Arrays.fill(values, typeInfo);
    List<String> typeInfos = Arrays.asList(values);
    return this.append(new AutocompleteResult(matches, startIndex, typeInfos));
  }

  public AutocompleteResult append(List<String> matches) {
    return this.append(new AutocompleteResult(matches, startIndex));
  }

  public AutocompleteResult append(List<String> matches, List<String> typeInfos) {
    return this.append(new AutocompleteResult(matches, startIndex, typeInfos));
  }

  public AutocompleteResult append(AutocompleteResult acr2) {
    this.matchInfos.addAll(acr2.matchInfos);
    return this;
  }

  public boolean isEmpty() {
    return this.matchInfos != null && this.matchInfos.isEmpty();
  }

  public void removeIf(Predicate<MatchInfo> p) {
    this.matchInfos.removeIf(p);
  }
}
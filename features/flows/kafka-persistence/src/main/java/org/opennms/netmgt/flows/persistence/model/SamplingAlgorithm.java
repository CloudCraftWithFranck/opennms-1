/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: flowdocument.proto

package org.opennms.netmgt.flows.persistence.model;

/**
 * Protobuf enum {@code SamplingAlgorithm}
 */
public enum SamplingAlgorithm
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>UNASSIGNED = 0;</code>
   */
  UNASSIGNED(0),
  /**
   * <code>SYSTEMATIC_COUNT_BASED_SAMPLING = 1;</code>
   */
  SYSTEMATIC_COUNT_BASED_SAMPLING(1),
  /**
   * <code>SYSTEMATIC_TIME_BASED_SAMPLING = 2;</code>
   */
  SYSTEMATIC_TIME_BASED_SAMPLING(2),
  /**
   * <code>RANDOM_N_OUT_OF_N_SAMPLING = 3;</code>
   */
  RANDOM_N_OUT_OF_N_SAMPLING(3),
  /**
   * <code>UNIFORM_PROBABILISTIC_SAMPLING = 4;</code>
   */
  UNIFORM_PROBABILISTIC_SAMPLING(4),
  /**
   * <code>PROPERTY_MATCH_FILTERING = 5;</code>
   */
  PROPERTY_MATCH_FILTERING(5),
  /**
   * <code>HASH_BASED_FILTERING = 6;</code>
   */
  HASH_BASED_FILTERING(6),
  /**
   * <code>FLOW_STATE_DEPENDENT_INTERMEDIATE_FLOW_SELECTION_PROCESS = 7;</code>
   */
  FLOW_STATE_DEPENDENT_INTERMEDIATE_FLOW_SELECTION_PROCESS(7),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>UNASSIGNED = 0;</code>
   */
  public static final int UNASSIGNED_VALUE = 0;
  /**
   * <code>SYSTEMATIC_COUNT_BASED_SAMPLING = 1;</code>
   */
  public static final int SYSTEMATIC_COUNT_BASED_SAMPLING_VALUE = 1;
  /**
   * <code>SYSTEMATIC_TIME_BASED_SAMPLING = 2;</code>
   */
  public static final int SYSTEMATIC_TIME_BASED_SAMPLING_VALUE = 2;
  /**
   * <code>RANDOM_N_OUT_OF_N_SAMPLING = 3;</code>
   */
  public static final int RANDOM_N_OUT_OF_N_SAMPLING_VALUE = 3;
  /**
   * <code>UNIFORM_PROBABILISTIC_SAMPLING = 4;</code>
   */
  public static final int UNIFORM_PROBABILISTIC_SAMPLING_VALUE = 4;
  /**
   * <code>PROPERTY_MATCH_FILTERING = 5;</code>
   */
  public static final int PROPERTY_MATCH_FILTERING_VALUE = 5;
  /**
   * <code>HASH_BASED_FILTERING = 6;</code>
   */
  public static final int HASH_BASED_FILTERING_VALUE = 6;
  /**
   * <code>FLOW_STATE_DEPENDENT_INTERMEDIATE_FLOW_SELECTION_PROCESS = 7;</code>
   */
  public static final int FLOW_STATE_DEPENDENT_INTERMEDIATE_FLOW_SELECTION_PROCESS_VALUE = 7;


  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @Deprecated
  public static SamplingAlgorithm valueOf(int value) {
    return forNumber(value);
  }

  public static SamplingAlgorithm forNumber(int value) {
    switch (value) {
      case 0: return UNASSIGNED;
      case 1: return SYSTEMATIC_COUNT_BASED_SAMPLING;
      case 2: return SYSTEMATIC_TIME_BASED_SAMPLING;
      case 3: return RANDOM_N_OUT_OF_N_SAMPLING;
      case 4: return UNIFORM_PROBABILISTIC_SAMPLING;
      case 5: return PROPERTY_MATCH_FILTERING;
      case 6: return HASH_BASED_FILTERING;
      case 7: return FLOW_STATE_DEPENDENT_INTERMEDIATE_FLOW_SELECTION_PROCESS;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<SamplingAlgorithm>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      SamplingAlgorithm> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<SamplingAlgorithm>() {
          public SamplingAlgorithm findValueByNumber(int number) {
            return SamplingAlgorithm.forNumber(number);
          }
        };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor
      getValueDescriptor() {
    return getDescriptor().getValues().get(ordinal());
  }
  public final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptorForType() {
    return getDescriptor();
  }
  public static final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptor() {
    return EnrichedFlowProtos.getDescriptor().getEnumTypes().get(1);
  }

  private static final SamplingAlgorithm[] VALUES = values();

  public static SamplingAlgorithm valueOf(
      com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new IllegalArgumentException(
        "EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private SamplingAlgorithm(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:SamplingAlgorithm)
}


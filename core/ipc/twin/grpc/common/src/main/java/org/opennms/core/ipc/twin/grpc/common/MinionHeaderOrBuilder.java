/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
// source: twin-grpc.proto

package org.opennms.core.ipc.twin.grpc.common;

public interface MinionHeaderOrBuilder extends
    // @@protoc_insertion_point(interface_extends:MinionHeader)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string system_id = 1;</code>
   */
  java.lang.String getSystemId();
  /**
   * <code>string system_id = 1;</code>
   */
  com.google.protobuf.ByteString
      getSystemIdBytes();

  /**
   * <code>string location = 2;</code>
   */
  java.lang.String getLocation();
  /**
   * <code>string location = 2;</code>
   */
  com.google.protobuf.ByteString
      getLocationBytes();
}
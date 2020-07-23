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

public interface NodeInfoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:NodeInfo)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string foreign_source = 1;</code>
   */
  java.lang.String getForeignSource();
  /**
   * <code>string foreign_source = 1;</code>
   */
  com.google.protobuf.ByteString
      getForeignSourceBytes();

  /**
   * <code>string foregin_id = 2;</code>
   */
  java.lang.String getForeginId();
  /**
   * <code>string foregin_id = 2;</code>
   */
  com.google.protobuf.ByteString
      getForeginIdBytes();

  /**
   * <code>uint32 node_id = 3;</code>
   */
  int getNodeId();

  /**
   * <code>repeated string categories = 4;</code>
   */
  java.util.List<java.lang.String>
      getCategoriesList();
  /**
   * <code>repeated string categories = 4;</code>
   */
  int getCategoriesCount();
  /**
   * <code>repeated string categories = 4;</code>
   */
  java.lang.String getCategories(int index);
  /**
   * <code>repeated string categories = 4;</code>
   */
  com.google.protobuf.ByteString
      getCategoriesBytes(int index);
}

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import DS from 'ember-data';

export default DS.Model.extend({
  rack: DS.attr('string'),
  state: DS.attr('string'),
  nodeHostName: DS.attr('string'),
  nodeHTTPAddress: DS.attr('string'),
  lastHealthUpdate: DS.attr('string'),
  healthReport: DS.attr('string'),
  numContainers: DS.attr('number'),
  usedMemoryMB: DS.attr('number'),
  availMemoryMB: DS.attr('number'),
  usedVirtualCores: DS.attr('number'),
  availableVirtualCores: DS.attr('number'),
  version: DS.attr('string'),
  nodeLabels: DS.attr('array'),

  nodeLabelsAsString: function() {
    var labels = this.get("nodeLabels");
    var labelToReturn = "";
    // Only one label per node supported.
    if (labels && labels.length > 0) {
      labelToReturn = labels[0];
    }
    return labelToReturn;
  }.property("nodeLabels"),

  /**
   * Indicates no rows were retrieved from backend
   */
  isDummyNode: function() {
    return this.get('id') === "dummy";
  }.property("id"),

  nodeStateStyle: function() {
    var style = "default";
    var nodeState = this.get("state");
    if (nodeState === "REBOOTED") {
      style = "warning";
    } else if (nodeState === "UNHEALTHY" || nodeState === "DECOMMISSIONED" ||
          nodeState === "LOST" || nodeState === "SHUTDOWN") {
      style = "danger";
    } else if (nodeState === "RUNNING") {
      style = "success";
    }
    return "label label-" + style;
  }.property("state"),

  getMemoryDataForDonutChart: function() {
    var arr = [];
    arr.push({
      label: "Used",
      value: this.get("usedMemoryMB")
    });
    arr.push({
      label: "Available",
      value: this.get("availMemoryMB")
    });
    return arr;
  }.property("availMemoryMB", "usedMemoryMB"),

  getVCoreDataForDonutChart: function() {
    var arr = [];
    arr.push({
      label: "Used",
      value: this.get("usedVirtualCores")
    });
    arr.push({
      label: "Available",
      value: this.get("availableVirtualCores")
    });
    return arr;
  }.property("availableVirtualCores", "usedVirtualCores"),

  toolTipText: function() {
    return "<p>Rack: " + this.get("rack") + '</p>' +
           "<p>Host: " + this.get("nodeHostName") + '</p>' +
           "<p>Used Memory: " + Math.round(this.get("usedMemoryMB")) + ' MB</p>' +
           "<p>Available Memory: " + Math.round(this.get("availMemoryMB")) + ' MB</p>';
  }.property(),

  usedMemoryBytes: function() {
    return this.get("usedMemoryMB") * 1024 * 1024;
  }.property("usedMemoryMB"),

  availMemoryBytes: function() {
    return this.get("availMemoryMB") * 1024 * 1024;
  }.property("availMemoryMB"),
});

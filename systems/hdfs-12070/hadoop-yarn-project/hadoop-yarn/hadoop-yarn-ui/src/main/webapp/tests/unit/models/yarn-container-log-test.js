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

import { moduleForModel, test } from 'ember-qunit';
import Ember from 'ember';

moduleForModel('yarn-container-log', 'Unit | Model | ContainerLog', {
  // Specify the other units that are required for this test.
  needs: []
});

test('Basic creation test', function(assert) {
  let model = this.subject();
  assert.ok(model);
  assert.ok(model._notifyProperties);
  assert.ok(model.didLoad);
  assert.ok(model.logs);
  assert.ok(model.containerID);
  assert.ok(model.logFileName);
});

test('test fields', function(assert) {
  let model = this.subject();

  Ember.run(function () {
    model.set("logs", "This is syslog");
    model.set("containerID", "container_e32_1456000363780_0002_01_000001");
    model.set("logFileName", "syslog");
    assert.equal(model.get("logs"), "This is syslog");
    assert.equal(model.get("containerID"), "container_e32_1456000363780_0002_01_000001");
    assert.equal(model.get("logFileName"), "syslog");
  });
});


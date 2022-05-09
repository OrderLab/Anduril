<!---
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. See accompanying LICENSE file.
-->

Placement Constraints
=====================

<!-- MACRO{toc|fromDepth=0|toDepth=3} -->


Overview
--------

YARN allows applications to specify placement constraints in the form of data locality (preference to specific nodes or racks) or (non-overlapping) node labels. This document focuses on more expressive placement constraints in YARN. Such constraints can be crucial for the performance and resilience of applications, especially those that include long-running containers, such as services, machine-learning and streaming workloads.

For example, it may be beneficial to co-locate the allocations of a job on the same rack (*affinity* constraints) to reduce network costs, spread allocations across machines (*anti-affinity* constraints) to minimize resource interference, or allow up to a specific number of allocations in a node group (*cardinality* constraints) to strike a balance between the two. Placement decisions also affect resilience. For example, allocations placed within the same cluster upgrade domain would go offline simultaneously.

The applications can specify constraints without requiring knowledge of the underlying topology of the cluster (e.g., one does not need to specify the specific node or rack where their containers should be placed with constraints) or the other applications deployed. Currently **intra-application** constraints are supported, but the design that is followed is generic and support for constraints across applications will soon be added. Moreover, all constraints at the moment are **hard**, that is, if the constraints for a container cannot be satisfied due to the current cluster condition or conflicting constraints, the container request will remain pending or get will get rejected.

Note that in this document we use the notion of “allocation” to refer to a unit of resources (e.g., CPU and memory) that gets allocated in a node. In the current implementation of YARN, an allocation corresponds to a single container. However, in case an application uses an allocation to spawn more than one containers, an allocation could correspond to multiple containers.


Quick Guide
-----------

We first describe how to enable scheduling with placement constraints and then provide examples of how to experiment with this feature using the distributed shell, an application that allows to run a given shell command on a set of containers.

### Enabling placement constraints

To enable placement constraints, the following property has to be set to `placement-processor` or `scheduler` in **conf/yarn-site.xml**:

| Property | Description | Default value |
|:-------- |:----------- |:------------- |
| `yarn.resourcemanager.placement-constraints.handler` | Specify which handler will be used to process PlacementConstraints. Acceptable values are: `placement-processor`, `scheduler`, and `disabled`. | `disabled` |

We now give more details about each of the three placement constraint handlers:

* `placement-processor`: Using this handler, the placement of containers with constraints is determined as a pre-processing step before the capacity or the fair scheduler is called. Once the placement is decided, the capacity/fair scheduler is invoked to perform the actual allocation. The advantage of this handler is that it supports all constraint types (affinity, anti-affinity, cardinality). Moreover, it considers multiple containers at a time, which allows to satisfy more constraints than a container-at-a-time approach can achieve. As it sits outside the main scheduler, it can be used by both the capacity and fair schedulers. Note that at the moment it does not account for task priorities within an application, given that such priorities might be conflicting with the placement constraints.
* `scheduler`: Using this handler, containers with constraints will be placed by the main scheduler (as of now, only the capacity scheduler supports SchedulingRequests). It currently supports anti-affinity constraints (no affinity or cardinality). The advantage of this handler, when compared to the `placement-processor`, is that it follows the same ordering rules for queues (sorted by utilization, priority), apps (sorted by FIFO/fairness/priority) and tasks within the same app (priority) that are enforced by the existing main scheduler.
* `disabled`: Using this handler, if a SchedulingRequest is asked by an application, the corresponding allocate call will be rejected.

The `placement-processor` handler supports a wider range of constraints and can allow more containers to be placed, especially when applications have demanding constraints or the cluster is highly-utilized (due to considering multiple containers at a time). However, if respecting task priority within an application is important for the user and the capacity scheduler is used, then the `scheduler` handler should be used instead.

### Experimenting with placement constraints using distributed shell

Users can experiment with placement constraints by using the distributed shell application through the following command:

```
$ yarn org.apache.hadoop.yarn.applications.distributedshell.Client -jar share/hadoop/yarn/hadoop-yarn-applications-distributedshell-${project.version}.jar -shell_command sleep -shell_args 10 -placement_spec PlacementSpec
```

where **PlacementSpec** is of the form:

```
PlacementSpec => "" | KeyVal;PlacementSpec
KeyVal        => SourceTag=Constraint
SourceTag     => String
Constraint    => NumContainers | NumContainers,"IN",Scope,TargetTag | NumContainers,"NOTIN",Scope,TargetTag | NumContainers,"CARDINALITY",Scope,TargetTag,MinCard,MaxCard
NumContainers => int
Scope         => "NODE" | "RACK"
TargetTag     => String
MinCard       => int
MaxCard       => int
```

Note that when the `-placement_spec` argument is specified in the distributed shell command, the `-num-containers` argument should not be used. In case `-num-containers` argument is used in conjunction with `-placement-spec`, the former is ignored. This is because in PlacementSpec, we determine the number of containers per tag, making the `-num-containers` redundant and possibly conflicting. Moreover, if `-placement_spec` is used, all containers will be requested with GUARANTEED execution type.

An example of PlacementSpec is the following:
```
zk=3,NOTIN,NODE,zk:hbase=5,IN,RACK,zk:spark=7,CARDINALITY,NODE,hbase,1,3
```
The above encodes two constraints:
* place 3 containers with tag "zk" (standing for ZooKeeper) with node anti-affinity to each other, i.e., do not place more than one container per node (notice that in this first constraint, the SourceTag and the TargetTag of the constraint coincide);
* place 5 containers with tag "hbase" with affinity to a rack on which containers with tag "zk" are running (i.e., an "hbase" container should not be placed at a rack where an "zk" container is running, given that "zk" is the TargetTag of the second constraint);
* place 7 container with tag "spark" in nodes that have at least one, but no more than three, containers, with tag "hbase".



Defining Placement Constraints
------------------------------

### Allocation tags

Allocation tags are string tags that an application can associate with (groups of) its containers. Tags are used to identify components of applications. For example, an HBase Master allocation can be tagged with "hbase-m", and Region Servers with "hbase-rs". Other examples are "latency-critical" to refer to the more general demands of the allocation, or "app_0041" to denote the job ID. Allocation tags play a key role in constraints, as they allow to refer to multiple allocations that share a common tag.

Note that instead of using the `ResourceRequest` object to define allocation tags, we use the new `SchedulingRequest` object. This has many similarities with the `ResourceRequest`, but better separates the sizing of the requested allocations (number and size of allocations, priority, execution type, etc.), and the constraints dictating how these allocations should be placed (resource name, relaxed locality). Applications can still use `ResourceRequest` objects, but in order to define allocation tags and constraints, they need to use the `SchedulingRequest` object. Within a single `AllocateRequest`, an application should use either the `ResourceRequest` or the `SchedulingRequest` objects, but not both of them.

#### Differences between node labels, node attributes and allocation tags

The difference between allocation tags and node labels or node attributes (YARN-3409), is that allocation tags are attached to allocations and not to nodes. When an allocation gets allocated to a node by the scheduler, the set of tags of that allocation are automatically added to the node for the duration of the allocation. Hence, a node inherits the tags of the allocations that are currently allocated to the node. Likewise, a rack inherits the tags of its nodes. Moreover, similar to node labels and unlike node attributes, allocation tags have no value attached to them. As we show below, our constraints can refer to allocation tags, as well as node labels and node attributes.


### Placement constraints API

Applications can use the public API in the `PlacementConstraints` to construct placement constraint. Before describing the methods for building constraints, we describe the methods of the `PlacementTargets` class that are used to construct the target expressions that will then be used in constraints:

| Method | Description |
|:------ |:----------- |
| `allocationTag(String... allocationTags)` | Constructs a target expression on an allocation tag. It is satisfied if there are allocations with one of the given tags. |
| `allocationTagToIntraApp(String... allocationTags)` | similar to `allocationTag(String...)`, but targeting only the containers of the application that will use this target (intra-application constraints). |
| `nodePartition(String... nodePartitions)` | Constructs a target expression on a node partition. It is satisfied for nodes that belong to one of the `nodePartitions`. |
| `nodeAttribute(String attributeKey, String... attributeValues)` | Constructs a target expression on a node attribute. It is satisfied if the specified node attribute has one of the specified values. |

Note that the `nodeAttribute` method above is not yet functional, as it requires the ongoing node attributes feature.

The methods of the `PlacementConstraints` class for building constraints are the following:

| Method | Description |
|:------ |:----------- |
| `targetIn(String scope, TargetExpression... targetExpressions)` | Creates a constraint that requires allocations to be placed on nodes that satisfy all target expressions within the given scope (e.g., node or rack). For example, `targetIn(RACK, allocationTag("hbase-m"))`, allows allocations on nodes that belong to a rack that has at least one allocation with tag "hbase-m". |
| `targetNotIn(String scope, TargetExpression... targetExpressions)` | Creates a constraint that requires allocations to be placed on nodes that belong to a scope (e.g., node or rack) that does not satisfy any of the target expressions. |
| `cardinality(String scope, int minCardinality, int maxCardinality, String... allocationTags)` | Creates a constraint that restricts the number of allocations within a given scope (e.g., node or rack). For example, {@code cardinality(NODE, 3, 10, "zk")} is satisfied on nodes where there are no less than 3 allocations with tag "zk" and no more than 10. |
| `minCardinality(String scope, int minCardinality, String... allocationTags)` | Similar to `cardinality(String, int, int, String...)`, but determines only the minimum cardinality (the maximum cardinality is unbound). |
| `maxCardinality(String scope, int maxCardinality, String... allocationTags)` | Similar to `cardinality(String, int, int, String...)`, but determines only the maximum cardinality (the minimum cardinality is 0). |
| `targetCardinality(String scope, int minCardinality, int maxCardinality, String... allocationTags)` | This constraint generalizes the cardinality and target constraints. Consider a set of nodes N that belongs to the scope specified in the constraint. If the target expressions are satisfied at least minCardinality times and at most maxCardinality times in the node set N, then the constraint is satisfied. For example, `targetCardinality(RACK, 2, 10, allocationTag("zk"))`, requires an allocation to be placed within a rack that has at least 2 and at most 10 other allocations with tag "zk". |

The `PlacementConstraints` class also includes method for building compound constraints (AND/OR expressions with multiple constraints). Adding support for compound constraints is work in progress.


### Specifying constraints in applications

Applications have to specify the containers for which each constraint will be enabled. To this end, applications can provide a mapping from a set of allocation tags (source tags) to a placement constraint. For example, an entry of this mapping could be "hbase"->constraint1, which means that constraint1 will be applied when scheduling each allocation with tag "hbase".

When using the `placement-processor` handler (see [Enabling placement constraints](#Enabling_placement_constraints)), this constraint mapping is specified within the `RegisterApplicationMasterRequest`.

When using the `scheduler` handler, the constraints can also be added at each `SchedulingRequest` object. Each such constraint is valid for the tag of that scheduling request. In case constraints are specified both at the `RegisterApplicationMasterRequest` and the scheduling requests, the latter override the former.

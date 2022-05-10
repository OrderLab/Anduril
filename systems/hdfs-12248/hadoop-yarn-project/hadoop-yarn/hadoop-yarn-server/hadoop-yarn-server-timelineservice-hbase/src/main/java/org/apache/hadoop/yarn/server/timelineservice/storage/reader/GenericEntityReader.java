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
package org.apache.hadoop.yarn.server.timelineservice.storage.reader;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FamilyFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.yarn.api.records.timelineservice.TimelineEntity;
import org.apache.hadoop.yarn.server.timelineservice.reader.TimelineDataToRetrieve;
import org.apache.hadoop.yarn.server.timelineservice.reader.TimelineEntityFilters;
import org.apache.hadoop.yarn.server.timelineservice.reader.TimelineReaderContext;
import org.apache.hadoop.yarn.server.timelineservice.reader.filter.TimelineFilterList;
import org.apache.hadoop.yarn.server.timelineservice.reader.filter.TimelineFilterUtils;
import org.apache.hadoop.yarn.server.timelineservice.storage.TimelineReader.Field;
import org.apache.hadoop.yarn.server.timelineservice.storage.apptoflow.AppToFlowColumn;
import org.apache.hadoop.yarn.server.timelineservice.storage.apptoflow.AppToFlowRowKey;
import org.apache.hadoop.yarn.server.timelineservice.storage.apptoflow.AppToFlowTable;
import org.apache.hadoop.yarn.server.timelineservice.storage.common.BaseTable;
import org.apache.hadoop.yarn.server.timelineservice.storage.common.ColumnPrefix;
import org.apache.hadoop.yarn.server.timelineservice.storage.common.KeyConverter;
import org.apache.hadoop.yarn.server.timelineservice.storage.common.RowKeyPrefix;
import org.apache.hadoop.yarn.server.timelineservice.storage.common.StringKeyConverter;
import org.apache.hadoop.yarn.server.timelineservice.storage.common.TimelineStorageUtils;
import org.apache.hadoop.yarn.server.timelineservice.storage.entity.EntityColumn;
import org.apache.hadoop.yarn.server.timelineservice.storage.entity.EntityColumnFamily;
import org.apache.hadoop.yarn.server.timelineservice.storage.entity.EntityColumnPrefix;
import org.apache.hadoop.yarn.server.timelineservice.storage.entity.EntityRowKey;
import org.apache.hadoop.yarn.server.timelineservice.storage.entity.EntityRowKeyPrefix;
import org.apache.hadoop.yarn.server.timelineservice.storage.entity.EntityTable;
import org.apache.hadoop.yarn.webapp.NotFoundException;

import com.google.common.base.Preconditions;

/**
 * Timeline entity reader for generic entities that are stored in the entity
 * table.
 */
class GenericEntityReader extends TimelineEntityReader {
  private static final EntityTable ENTITY_TABLE = new EntityTable();

  /**
   * Used to look up the flow context.
   */
  private final AppToFlowTable appToFlowTable = new AppToFlowTable();

  /**
   * Used to convert strings key components to and from storage format.
   */
  private final KeyConverter<String> stringKeyConverter =
      new StringKeyConverter();

  public GenericEntityReader(TimelineReaderContext ctxt,
      TimelineEntityFilters entityFilters, TimelineDataToRetrieve toRetrieve,
      boolean sortedKeys) {
    super(ctxt, entityFilters, toRetrieve, sortedKeys);
  }

  public GenericEntityReader(TimelineReaderContext ctxt,
      TimelineDataToRetrieve toRetrieve) {
    super(ctxt, toRetrieve);
  }

  /**
   * Uses the {@link EntityTable}.
   */
  protected BaseTable<?> getTable() {
    return ENTITY_TABLE;
  }

  @Override
  protected FilterList constructFilterListBasedOnFilters() throws IOException {
    // Filters here cannot be null for multiple entity reads as they are set in
    // augmentParams if null.
    FilterList listBasedOnFilters = new FilterList();
    TimelineEntityFilters filters = getFilters();
    // Create filter list based on created time range and add it to
    // listBasedOnFilters.
    long createdTimeBegin = filters.getCreatedTimeBegin();
    long createdTimeEnd = filters.getCreatedTimeEnd();
    if (createdTimeBegin != 0 || createdTimeEnd != Long.MAX_VALUE) {
      listBasedOnFilters.addFilter(TimelineFilterUtils
          .createSingleColValueFiltersByRange(EntityColumn.CREATED_TIME,
              createdTimeBegin, createdTimeEnd));
    }
    // Create filter list based on metric filters and add it to
    // listBasedOnFilters.
    TimelineFilterList metricFilters = filters.getMetricFilters();
    if (metricFilters != null && !metricFilters.getFilterList().isEmpty()) {
      listBasedOnFilters.addFilter(TimelineFilterUtils.createHBaseFilterList(
          EntityColumnPrefix.METRIC, metricFilters));
    }
    // Create filter list based on config filters and add it to
    // listBasedOnFilters.
    TimelineFilterList configFilters = filters.getConfigFilters();
    if (configFilters != null && !configFilters.getFilterList().isEmpty()) {
      listBasedOnFilters.addFilter(TimelineFilterUtils.createHBaseFilterList(
          EntityColumnPrefix.CONFIG, configFilters));
    }
    // Create filter list based on info filters and add it to listBasedOnFilters
    TimelineFilterList infoFilters = filters.getInfoFilters();
    if (infoFilters != null && !infoFilters.getFilterList().isEmpty()) {
      listBasedOnFilters.addFilter(TimelineFilterUtils.createHBaseFilterList(
          EntityColumnPrefix.INFO, infoFilters));
    }
    return listBasedOnFilters;
  }

  /**
   * Check if we need to fetch only some of the event columns.
   *
   * @return true if we need to fetch some of the columns, false otherwise.
   */
  private boolean fetchPartialEventCols(TimelineFilterList eventFilters,
      EnumSet<Field> fieldsToRetrieve) {
    return (eventFilters != null && !eventFilters.getFilterList().isEmpty() &&
        !hasField(fieldsToRetrieve, Field.EVENTS));
  }

  /**
   * Check if we need to fetch only some of the relates_to columns.
   *
   * @return true if we need to fetch some of the columns, false otherwise.
   */
  private boolean fetchPartialRelatesToCols(TimelineFilterList relatesTo,
      EnumSet<Field> fieldsToRetrieve) {
    return (relatesTo != null && !relatesTo.getFilterList().isEmpty() &&
        !hasField(fieldsToRetrieve, Field.RELATES_TO));
  }

  /**
   * Check if we need to fetch only some of the is_related_to columns.
   *
   * @return true if we need to fetch some of the columns, false otherwise.
   */
  private boolean fetchPartialIsRelatedToCols(TimelineFilterList isRelatedTo,
      EnumSet<Field> fieldsToRetrieve) {
    return (isRelatedTo != null && !isRelatedTo.getFilterList().isEmpty() &&
        !hasField(fieldsToRetrieve, Field.IS_RELATED_TO));
  }

  /**
   * Check if we need to fetch only some of the columns based on event filters,
   * relatesto and isrelatedto from info family.
   *
   * @return true, if we need to fetch only some of the columns, false if we
   *         need to fetch all the columns under info column family.
   */
  protected boolean fetchPartialColsFromInfoFamily() {
    EnumSet<Field> fieldsToRetrieve = getDataToRetrieve().getFieldsToRetrieve();
    TimelineEntityFilters filters = getFilters();
    return fetchPartialEventCols(filters.getEventFilters(), fieldsToRetrieve)
        || fetchPartialRelatesToCols(filters.getRelatesTo(), fieldsToRetrieve)
        || fetchPartialIsRelatedToCols(filters.getIsRelatedTo(),
            fieldsToRetrieve);
  }

  /**
   * Check if we need to create filter list based on fields. We need to create a
   * filter list iff all fields need not be retrieved or we have some specific
   * fields or metrics to retrieve. We also need to create a filter list if we
   * have relationships(relatesTo/isRelatedTo) and event filters specified for
   * the query.
   *
   * @return true if we need to create the filter list, false otherwise.
   */
  protected boolean needCreateFilterListBasedOnFields() {
    TimelineDataToRetrieve dataToRetrieve = getDataToRetrieve();
    // Check if all fields are to be retrieved or not. If all fields have to
    // be retrieved, also check if we have some metrics or configs to
    // retrieve specified for the query because then a filter list will have
    // to be created.
    boolean flag =
        !dataToRetrieve.getFieldsToRetrieve().contains(Field.ALL)
            || (dataToRetrieve.getConfsToRetrieve() != null && !dataToRetrieve
                .getConfsToRetrieve().getFilterList().isEmpty())
            || (dataToRetrieve.getMetricsToRetrieve() != null && !dataToRetrieve
                .getMetricsToRetrieve().getFilterList().isEmpty());
    // Filters need to be checked only if we are reading multiple entities. If
    // condition above is false, we check if there are relationships(relatesTo/
    // isRelatedTo) and event filters specified for the query.
    if (!flag && !isSingleEntityRead()) {
      TimelineEntityFilters filters = getFilters();
      flag =
          (filters.getEventFilters() != null && !filters.getEventFilters()
              .getFilterList().isEmpty())
              || (filters.getIsRelatedTo() != null && !filters.getIsRelatedTo()
                  .getFilterList().isEmpty())
              || (filters.getRelatesTo() != null && !filters.getRelatesTo()
                  .getFilterList().isEmpty());
    }
    return flag;
  }

  /**
   * Add {@link QualifierFilter} filters to filter list for each column of
   * entity table.
   *
   * @param list filter list to which qualifier filters have to be added.
   */
  protected void updateFixedColumns(FilterList list) {
    for (EntityColumn column : EntityColumn.values()) {
      list.addFilter(new QualifierFilter(CompareOp.EQUAL, new BinaryComparator(
          column.getColumnQualifierBytes())));
    }
  }

  /**
   * Creates a filter list which indicates that only some of the column
   * qualifiers in the info column family will be returned in result.
   *
   * @param isApplication If true, it means operations are to be performed for
   *          application table, otherwise for entity table.
   * @return filter list.
   * @throws IOException if any problem occurs while creating filter list.
   */
  private FilterList createFilterListForColsOfInfoFamily() throws IOException {
    FilterList infoFamilyColsFilter = new FilterList(Operator.MUST_PASS_ONE);
    // Add filters for each column in entity table.
    updateFixedColumns(infoFamilyColsFilter);
    EnumSet<Field> fieldsToRetrieve = getDataToRetrieve().getFieldsToRetrieve();
    // If INFO field has to be retrieved, add a filter for fetching columns
    // with INFO column prefix.
    if (hasField(fieldsToRetrieve, Field.INFO)) {
      infoFamilyColsFilter
          .addFilter(TimelineFilterUtils.createHBaseQualifierFilter(
              CompareOp.EQUAL, EntityColumnPrefix.INFO));
    }
    TimelineFilterList relatesTo = getFilters().getRelatesTo();
    if (hasField(fieldsToRetrieve, Field.RELATES_TO)) {
      // If RELATES_TO field has to be retrieved, add a filter for fetching
      // columns with RELATES_TO column prefix.
      infoFamilyColsFilter.addFilter(TimelineFilterUtils
          .createHBaseQualifierFilter(CompareOp.EQUAL,
              EntityColumnPrefix.RELATES_TO));
    } else if (relatesTo != null && !relatesTo.getFilterList().isEmpty()) {
      // Even if fields to retrieve does not contain RELATES_TO, we still
      // need to have a filter to fetch some of the column qualifiers if
      // relatesTo filters are specified. relatesTo filters will then be
      // matched after fetching rows from HBase.
      Set<String> relatesToCols =
          TimelineFilterUtils.fetchColumnsFromFilterList(relatesTo);
      infoFamilyColsFilter.addFilter(createFiltersFromColumnQualifiers(
          EntityColumnPrefix.RELATES_TO, relatesToCols));
    }
    TimelineFilterList isRelatedTo = getFilters().getIsRelatedTo();
    if (hasField(fieldsToRetrieve, Field.IS_RELATED_TO)) {
      // If IS_RELATED_TO field has to be retrieved, add a filter for fetching
      // columns with IS_RELATED_TO column prefix.
      infoFamilyColsFilter.addFilter(TimelineFilterUtils
          .createHBaseQualifierFilter(CompareOp.EQUAL,
              EntityColumnPrefix.IS_RELATED_TO));
    } else if (isRelatedTo != null && !isRelatedTo.getFilterList().isEmpty()) {
      // Even if fields to retrieve does not contain IS_RELATED_TO, we still
      // need to have a filter to fetch some of the column qualifiers if
      // isRelatedTo filters are specified. isRelatedTo filters will then be
      // matched after fetching rows from HBase.
      Set<String> isRelatedToCols =
          TimelineFilterUtils.fetchColumnsFromFilterList(isRelatedTo);
      infoFamilyColsFilter.addFilter(createFiltersFromColumnQualifiers(
          EntityColumnPrefix.IS_RELATED_TO, isRelatedToCols));
    }
    TimelineFilterList eventFilters = getFilters().getEventFilters();
    if (hasField(fieldsToRetrieve, Field.EVENTS)) {
      // If EVENTS field has to be retrieved, add a filter for fetching columns
      // with EVENT column prefix.
      infoFamilyColsFilter
          .addFilter(TimelineFilterUtils.createHBaseQualifierFilter(
              CompareOp.EQUAL, EntityColumnPrefix.EVENT));
    } else if (eventFilters != null &&
        !eventFilters.getFilterList().isEmpty()) {
      // Even if fields to retrieve does not contain EVENTS, we still need to
      // have a filter to fetch some of the column qualifiers on the basis of
      // event filters specified. Event filters will then be matched after
      // fetching rows from HBase.
      Set<String> eventCols =
          TimelineFilterUtils.fetchColumnsFromFilterList(eventFilters);
      infoFamilyColsFilter.addFilter(createFiltersFromColumnQualifiers(
          EntityColumnPrefix.EVENT, eventCols));
    }
    return infoFamilyColsFilter;
  }

  /**
   * Exclude column prefixes via filters which are not required(based on fields
   * to retrieve) from info column family. These filters are added to filter
   * list which contains a filter for getting info column family.
   *
   * @param infoColFamilyList filter list for info column family.
   */
  private void excludeFieldsFromInfoColFamily(FilterList infoColFamilyList) {
    EnumSet<Field> fieldsToRetrieve = getDataToRetrieve().getFieldsToRetrieve();
    // Events not required.
    if (!hasField(fieldsToRetrieve, Field.EVENTS)) {
      infoColFamilyList.addFilter(TimelineFilterUtils
          .createHBaseQualifierFilter(CompareOp.NOT_EQUAL,
              EntityColumnPrefix.EVENT));
    }
    // info not required.
    if (!hasField(fieldsToRetrieve, Field.INFO)) {
      infoColFamilyList.addFilter(TimelineFilterUtils
          .createHBaseQualifierFilter(CompareOp.NOT_EQUAL,
              EntityColumnPrefix.INFO));
    }
    // is related to not required.
    if (!hasField(fieldsToRetrieve, Field.IS_RELATED_TO)) {
      infoColFamilyList.addFilter(TimelineFilterUtils
          .createHBaseQualifierFilter(CompareOp.NOT_EQUAL,
              EntityColumnPrefix.IS_RELATED_TO));
    }
    // relates to not required.
    if (!hasField(fieldsToRetrieve, Field.RELATES_TO)) {
      infoColFamilyList.addFilter(TimelineFilterUtils
          .createHBaseQualifierFilter(CompareOp.NOT_EQUAL,
              EntityColumnPrefix.RELATES_TO));
    }
  }

  /**
   * Updates filter list based on fields for confs and metrics to retrieve.
   *
   * @param listBasedOnFields filter list based on fields.
   * @throws IOException if any problem occurs while updating filter list.
   */
  private void updateFilterForConfsAndMetricsToRetrieve(
      FilterList listBasedOnFields) throws IOException {
    TimelineDataToRetrieve dataToRetrieve = getDataToRetrieve();
    // Please note that if confsToRetrieve is specified, we would have added
    // CONFS to fields to retrieve in augmentParams() even if not specified.
    if (dataToRetrieve.getFieldsToRetrieve().contains(Field.CONFIGS)) {
      // Create a filter list for configs.
      listBasedOnFields.addFilter(TimelineFilterUtils
          .createFilterForConfsOrMetricsToRetrieve(
              dataToRetrieve.getConfsToRetrieve(), EntityColumnFamily.CONFIGS,
              EntityColumnPrefix.CONFIG));
    }

    // Please note that if metricsToRetrieve is specified, we would have added
    // METRICS to fields to retrieve in augmentParams() even if not specified.
    if (dataToRetrieve.getFieldsToRetrieve().contains(Field.METRICS)) {
      // Create a filter list for metrics.
      listBasedOnFields.addFilter(TimelineFilterUtils
          .createFilterForConfsOrMetricsToRetrieve(
              dataToRetrieve.getMetricsToRetrieve(),
              EntityColumnFamily.METRICS, EntityColumnPrefix.METRIC));
    }
  }

  @Override
  protected FilterList constructFilterListBasedOnFields() throws IOException {
    if (!needCreateFilterListBasedOnFields()) {
      // Fetch all the columns. No need of a filter.
      return null;
    }
    FilterList listBasedOnFields = new FilterList(Operator.MUST_PASS_ONE);
    FilterList infoColFamilyList = new FilterList();
    // By default fetch everything in INFO column family.
    FamilyFilter infoColumnFamily =
        new FamilyFilter(CompareOp.EQUAL, new BinaryComparator(
            EntityColumnFamily.INFO.getBytes()));
    infoColFamilyList.addFilter(infoColumnFamily);
    if (!isSingleEntityRead() && fetchPartialColsFromInfoFamily()) {
      // We can fetch only some of the columns from info family.
      infoColFamilyList.addFilter(createFilterListForColsOfInfoFamily());
    } else {
      // Exclude column prefixes in info column family which are not required
      // based on fields to retrieve.
      excludeFieldsFromInfoColFamily(infoColFamilyList);
    }
    listBasedOnFields.addFilter(infoColFamilyList);
    updateFilterForConfsAndMetricsToRetrieve(listBasedOnFields);
    return listBasedOnFields;
  }

  /**
   * Looks up flow context from AppToFlow table.
   *
   * @param appToFlowRowKey to identify Cluster and App Ids.
   * @param hbaseConf HBase configuration.
   * @param conn HBase Connection.
   * @return flow context information.
   * @throws IOException if any problem occurs while fetching flow information.
   */
  protected FlowContext lookupFlowContext(AppToFlowRowKey appToFlowRowKey,
      Configuration hbaseConf, Connection conn) throws IOException {
    byte[] rowKey = appToFlowRowKey.getRowKey();
    Get get = new Get(rowKey);
    Result result = appToFlowTable.getResult(hbaseConf, conn, get);
    if (result != null && !result.isEmpty()) {
      return new FlowContext(AppToFlowColumn.USER_ID.readResult(result)
          .toString(), AppToFlowColumn.FLOW_ID.readResult(result).toString(),
          ((Number) AppToFlowColumn.FLOW_RUN_ID.readResult(result))
          .longValue());
    } else {
      throw new NotFoundException(
          "Unable to find the context flow ID and flow run ID for clusterId="
              + appToFlowRowKey.getClusterId() + ", appId="
              + appToFlowRowKey.getAppId());
    }
  }

  /**
   * Encapsulates flow context information.
   */
  protected static class FlowContext {
    private final String userId;
    private final String flowName;
    private final Long flowRunId;

    public FlowContext(String user, String flowName, Long flowRunId) {
      this.userId = user;
      this.flowName = flowName;
      this.flowRunId = flowRunId;
    }

    protected String getUserId() {
      return userId;
    }

    protected String getFlowName() {
      return flowName;
    }

    protected Long getFlowRunId() {
      return flowRunId;
    }
  }

  @Override
  protected void validateParams() {
    Preconditions.checkNotNull(getContext(), "context shouldn't be null");
    Preconditions.checkNotNull(getDataToRetrieve(),
        "data to retrieve shouldn't be null");
    Preconditions.checkNotNull(getContext().getClusterId(),
        "clusterId shouldn't be null");
    Preconditions.checkNotNull(getContext().getAppId(),
        "appId shouldn't be null");
    Preconditions.checkNotNull(getContext().getEntityType(),
        "entityType shouldn't be null");
    if (isSingleEntityRead()) {
      Preconditions.checkNotNull(getContext().getEntityId(),
          "entityId shouldn't be null");
    }
  }

  @Override
  protected void augmentParams(Configuration hbaseConf, Connection conn)
      throws IOException {
    TimelineReaderContext context = getContext();
    // In reality all three should be null or neither should be null
    if (context.getFlowName() == null || context.getFlowRunId() == null
        || context.getUserId() == null) {
      // Get flow context information from AppToFlow table.
      AppToFlowRowKey appToFlowRowKey =
          new AppToFlowRowKey(context.getClusterId(), context.getAppId());
      FlowContext flowContext =
          lookupFlowContext(appToFlowRowKey, hbaseConf, conn);
      context.setFlowName(flowContext.flowName);
      context.setFlowRunId(flowContext.flowRunId);
      context.setUserId(flowContext.userId);
    }
    // Add configs/metrics to fields to retrieve if confsToRetrieve and/or
    // metricsToRetrieve are specified.
    getDataToRetrieve().addFieldsBasedOnConfsAndMetricsToRetrieve();
    if (!isSingleEntityRead()) {
      createFiltersIfNull();
    }
  }

  @Override
  protected Result getResult(Configuration hbaseConf, Connection conn,
      FilterList filterList) throws IOException {
    TimelineReaderContext context = getContext();
    byte[] rowKey =
        new EntityRowKey(context.getClusterId(), context.getUserId(),
            context.getFlowName(), context.getFlowRunId(), context.getAppId(),
            context.getEntityType(), context.getEntityId()).getRowKey();
    Get get = new Get(rowKey);
    get.setMaxVersions(getDataToRetrieve().getMetricsLimit());
    if (filterList != null && !filterList.getFilters().isEmpty()) {
      get.setFilter(filterList);
    }
    return getTable().getResult(hbaseConf, conn, get);
  }

  @Override
  protected ResultScanner getResults(Configuration hbaseConf, Connection conn,
      FilterList filterList) throws IOException {
    // Scan through part of the table to find the entities belong to one app
    // and one type
    Scan scan = new Scan();
    TimelineReaderContext context = getContext();
    RowKeyPrefix<EntityRowKey> entityRowKeyPrefix =
        new EntityRowKeyPrefix(context.getClusterId(), context.getUserId(),
            context.getFlowName(), context.getFlowRunId(), context.getAppId(),
            context.getEntityType());
    scan.setRowPrefixFilter(entityRowKeyPrefix.getRowKeyPrefix());
    scan.setMaxVersions(getDataToRetrieve().getMetricsLimit());
    if (filterList != null && !filterList.getFilters().isEmpty()) {
      scan.setFilter(filterList);
    }
    return getTable().getResultScanner(hbaseConf, conn, scan);
  }

  @Override
  protected TimelineEntity parseEntity(Result result) throws IOException {
    if (result == null || result.isEmpty()) {
      return null;
    }
    TimelineEntity entity = new TimelineEntity();
    String entityType = EntityColumn.TYPE.readResult(result).toString();
    entity.setType(entityType);
    String entityId = EntityColumn.ID.readResult(result).toString();
    entity.setId(entityId);

    TimelineEntityFilters filters = getFilters();
    // fetch created time
    Long createdTime = (Long) EntityColumn.CREATED_TIME.readResult(result);
    entity.setCreatedTime(createdTime);

    EnumSet<Field> fieldsToRetrieve = getDataToRetrieve().getFieldsToRetrieve();
    // fetch is related to entities and match isRelatedTo filter. If isRelatedTo
    // filters do not match, entity would be dropped. We have to match filters
    // locally as relevant HBase filters to filter out rows on the basis of
    // isRelatedTo are not set in HBase scan.
    boolean checkIsRelatedTo =
        !isSingleEntityRead() && filters.getIsRelatedTo() != null
            && filters.getIsRelatedTo().getFilterList().size() > 0;
    if (hasField(fieldsToRetrieve, Field.IS_RELATED_TO) || checkIsRelatedTo) {
      readRelationship(entity, result, EntityColumnPrefix.IS_RELATED_TO, true);
      if (checkIsRelatedTo
          && !TimelineStorageUtils.matchIsRelatedTo(entity,
              filters.getIsRelatedTo())) {
        return null;
      }
      if (!hasField(fieldsToRetrieve, Field.IS_RELATED_TO)) {
        entity.getIsRelatedToEntities().clear();
      }
    }

    // fetch relates to entities and match relatesTo filter. If relatesTo
    // filters do not match, entity would be dropped. We have to match filters
    // locally as relevant HBase filters to filter out rows on the basis of
    // relatesTo are not set in HBase scan.
    boolean checkRelatesTo =
        !isSingleEntityRead() && filters.getRelatesTo() != null
            && filters.getRelatesTo().getFilterList().size() > 0;
    if (hasField(fieldsToRetrieve, Field.RELATES_TO)
        || checkRelatesTo) {
      readRelationship(entity, result, EntityColumnPrefix.RELATES_TO, false);
      if (checkRelatesTo
          && !TimelineStorageUtils.matchRelatesTo(entity,
              filters.getRelatesTo())) {
        return null;
      }
      if (!hasField(fieldsToRetrieve, Field.RELATES_TO)) {
        entity.getRelatesToEntities().clear();
      }
    }

    // fetch info if fieldsToRetrieve contains INFO or ALL.
    if (hasField(fieldsToRetrieve, Field.INFO)) {
      readKeyValuePairs(entity, result, EntityColumnPrefix.INFO, false);
    }

    // fetch configs if fieldsToRetrieve contains CONFIGS or ALL.
    if (hasField(fieldsToRetrieve, Field.CONFIGS)) {
      readKeyValuePairs(entity, result, EntityColumnPrefix.CONFIG, true);
    }

    // fetch events and match event filters if they exist. If event filters do
    // not match, entity would be dropped. We have to match filters locally
    // as relevant HBase filters to filter out rows on the basis of events
    // are not set in HBase scan.
    boolean checkEvents =
        !isSingleEntityRead() && filters.getEventFilters() != null
            && filters.getEventFilters().getFilterList().size() > 0;
    if (hasField(fieldsToRetrieve, Field.EVENTS) || checkEvents) {
      readEvents(entity, result, EntityColumnPrefix.EVENT);
      if (checkEvents
          && !TimelineStorageUtils.matchEventFilters(entity,
              filters.getEventFilters())) {
        return null;
      }
      if (!hasField(fieldsToRetrieve, Field.EVENTS)) {
        entity.getEvents().clear();
      }
    }

    // fetch metrics if fieldsToRetrieve contains METRICS or ALL.
    if (hasField(fieldsToRetrieve, Field.METRICS)) {
      readMetrics(entity, result, EntityColumnPrefix.METRIC);
    }
    return entity;
  }

  /**
   * Helper method for reading key-value pairs for either info or config.
   *
   * @param <T> Describes the type of column prefix.
   * @param entity entity to fill.
   * @param result result from HBase.
   * @param prefix column prefix.
   * @param isConfig if true, means we are reading configs, otherwise info.
   * @throws IOException if any problem is encountered while reading result.
   */
  protected <T> void readKeyValuePairs(TimelineEntity entity, Result result,
      ColumnPrefix<T> prefix, boolean isConfig) throws IOException {
    // info and configuration are of type Map<String, Object or String>
    Map<String, Object> columns =
        prefix.readResults(result, stringKeyConverter);
    if (isConfig) {
      for (Map.Entry<String, Object> column : columns.entrySet()) {
        entity.addConfig(column.getKey(), column.getValue().toString());
      }
    } else {
      entity.addInfo(columns);
    }
  }
}

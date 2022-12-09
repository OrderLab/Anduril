package analyzer.crashtuner;

import analyzer.analysis.AnalysisInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.*;
import soot.jimple.FieldRef;

import java.util.*;

public final class CrashTunerAnalyzer {
    private static final Logger LOG = LoggerFactory.getLogger(CrashTunerAnalyzer.class);

    public final static class SourceMetaInfoAccess {
        public final Unit unit;
        public final String methodSig;
        public final String variableName;
        public final String typeName;
        public final long accessId;

        public SourceMetaInfoAccess(Unit unit, String methodSig, String varName,
                                    String typeName, long accessId) {
            this.unit = unit;
            this.methodSig = methodSig;
            this.variableName = varName;
            this.typeName = typeName;
            this.accessId = accessId;
        }
    }

    static public Set<Location> analyze(final Set<SootClass> classes) {
        final Set<Location> result = new HashSet<>();
        for (final Map.Entry<SootMethod, List<SourceMetaInfoAccess>> entry :
                identifyMetaInfoAccesses(classes).entrySet()) {
            for (final SourceMetaInfoAccess access : entry.getValue()) {
                result.add(new Location(entry.getKey(), access.unit));
            }
        }
        LOG.info("meta info access = {}", result.size());
        return result;
    }

    static public Map<SootMethod, List<SourceMetaInfoAccess>> identifyMetaInfoAccesses(
            Collection<SootClass> sootClasses) {
        final Set<SootField> metaInfoFields = new HashSet<>();
        final Set<Local> metaInfoLocals = new HashSet<>();
        final Set<String> metaInfoTypes = predefinedMetaInfoTypes();
        for (final SootClass sootClass : sootClasses) {
            for (final SootField field : sootClass.getFields()) {
                final String typeStr = field.getType().toString();
                if (metaInfoTypes.contains(typeStr)) {
                    LOG.debug("Field '" + field.getName() + "' in " + sootClass.getName() +
                            " has type '" + typeStr + "'");
                    metaInfoFields.add(field);
                }
            }
        }
        long accessId = 1;
        Map<SootMethod, List<SourceMetaInfoAccess>> accessPointsMap = new HashMap<>();
        for (final SootClass sootClass : sootClasses) {
            for (final SootMethod sootMethod : sootClass.getMethods()) {
                if (sootMethod.isPhantom() || !sootMethod.hasActiveBody())
                    continue;
                final Body body = sootMethod.retrieveActiveBody();
                for (final Local local : body.getLocals()) {
                    final String typeStr = local.getType().toString();
                    if (metaInfoTypes.contains(typeStr)) {
                        LOG.debug("Local '" + local.getName() + "' in " + sootClass.getName()
                                + ":" + sootMethod.getName() + " has type '" + typeStr + "'");
                        metaInfoLocals.add(local);
                    }
                }
                List<SourceMetaInfoAccess> accessPoints = new LinkedList<>();
                for (final Unit unit : body.getUnits()) {
                    boolean useMetaInfo = false;
                    String metaInfoVarName = null, metaInfoTypeStr = null;
                    for (final ValueBox box : unit.getUseBoxes()) {
                        final Value value = box.getValue();
                        if (value instanceof FieldRef) {
                            final SootField sf = ((FieldRef) value).getField();
                            if (metaInfoFields.contains(sf)) {
                                metaInfoVarName = sf.getName();
                                metaInfoTypeStr = sf.getType().toString();
                                LOG.debug("Use of meta-info field " + metaInfoVarName + " in " +
                                        sootClass.getName() + ":" + sootMethod.getName() + "@" +
                                        unit.getJavaSourceStartLineNumber());
                                useMetaInfo = true;
                            }
                        } else if (value instanceof Local) {
                            final Local local = (Local) value;
                            if (metaInfoLocals.contains(local)) {
                                metaInfoVarName = local.getName();
                                metaInfoTypeStr = local.getType().toString();
                                LOG.debug("Use of meta-info local " + local.getName() + " in " +
                                        sootClass.getName() + ":" + sootMethod.getName() + "@" +
                                        unit.getJavaSourceStartLineNumber());
                                useMetaInfo = true;
                            }
                        }
                        if (useMetaInfo)
                            break;
                    }
                    if (useMetaInfo) {
                        final SourceMetaInfoAccess access = new SourceMetaInfoAccess(unit,
                                sootMethod.getSignature(), metaInfoVarName, metaInfoTypeStr, accessId);
                        accessId++;
                        accessPoints.add(access);
                    }
                }
                if (!accessPoints.isEmpty()) {
                    accessPointsMap.put(sootMethod, accessPoints);
                }
            }
        }
        return accessPointsMap;
    }

    static private Set<String> predefinedMetaInfoTypes() {
        final Set<String> typeStrings = new HashSet<>();
        typeStrings.add("java.net.SocketAddress");
        final String prefix = AnalysisInput.prefix;
        if (prefix.startsWith("org.apache.zookeeper")) {
            typeStrings.add("org.apache.zookeeper.server.quorum.QuorumPeer");
            typeStrings.add("org.apache.zookeeper.server.quorum.Leader");
            typeStrings.add("org.apache.zookeeper.server.quorum.Follower");
            typeStrings.add("org.apache.zookeeper.server.quorum.Observer");
        } else if (prefix.startsWith("org.apache.hadoop")) {
            typeStrings.add("org.apache.hadoop.yarn.api.records.ApplicationAttemptId");
            typeStrings.add("org.apache.hadoop.yarn.api.records.ApplicationId");
            typeStrings.add("org.apache.hadoop.yarn.api.records.ContainerId");
            typeStrings.add("org.apache.hadoop.fs.FSDataOutputStream");
            typeStrings.add("org.apache.hadoop.fs.FSDataInputStream");
            typeStrings.add("org.apache.hadoop.hdfs.protocol.DatanodeInfo");
            typeStrings.add("org.apache.hadoop.hdfs.server.namenode.NameNode");
            typeStrings.add("org.apache.hadoop.hdfs.server.datanode.DataNode");
            typeStrings.add("org.apache.hadoop.hdfs.server.datanode.BPOfferService");
        } else if (prefix.contains("kafka")) {
            typeStrings.add("kafka.controller.ControllerState");
            typeStrings.add("kafka.controller.ReplicaStateMachine");
            typeStrings.add("kafka.server.BrokerServer");
        } else if (prefix.startsWith("org.apache.hbase")) {
            typeStrings.add("org.apache.hadoop.hbase.HRegionInfo");
            typeStrings.add("org.apache.hadoop.hbase.regionserver.HRegionServer");
            typeStrings.add("org.apache.hadoop.hbase.regionserver.HRegion");
            typeStrings.add("org.apache.hadoop.hbase.master.assignment.RegionStateNode");
            typeStrings.add("org.apache.hadoop.hbase.master.assignment.TransitRegionStateProcedure");
        } else if (prefix.startsWith("org.apache.cassandra")) {
            typeStrings.add("org.apache.cassandra.gms.Gossiper");
            typeStrings.add("org.apache.cassandra.net.MessageIn");
            typeStrings.add("org.apache.cassandra.net.MessageOut");
        }
        return typeStrings;
    }

    public static final class Location {
        public final SootMethod method;
        public final Unit unit;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Location)) return false;
            Location location = (Location) o;
            return method.equals(location.method) && unit.equals(location.unit);
        }

        @Override
        public int hashCode() {
            return Objects.hash(method, unit);
        }

        public Location(final SootMethod method, final Unit unit) {
            this.method = method;
            this.unit = unit;
        }
    }
}

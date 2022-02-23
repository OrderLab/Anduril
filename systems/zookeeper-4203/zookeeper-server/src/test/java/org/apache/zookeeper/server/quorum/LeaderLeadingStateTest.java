package org.apache.zookeeper.server.quorum;

import org.apache.zookeeper.audit.ZKAuditProvider;
import org.apache.zookeeper.metrics.MetricsProvider;
import org.apache.zookeeper.metrics.MetricsProviderLifeCycleException;
import org.apache.zookeeper.metrics.impl.MetricsProviderBootstrap;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ServerMetrics;
import org.apache.zookeeper.server.ZKDatabase;
import org.apache.zookeeper.server.admin.AdminServer;
import org.apache.zookeeper.server.auth.ProviderRegistry;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;
import org.apache.zookeeper.server.util.JvmPauseMonitor;
import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitConfig;
import org.jboss.byteman.rule.Rule;
import org.jboss.byteman.rule.helper.Helper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.sasl.SaslException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author lan
 * @date 2021/3/12
 */
@RunWith(org.jboss.byteman.contrib.bmunit.BMUnitRunner.class)
@BMUnitConfig(loadDirectory = "target/test-classes")
@BMScript(value = "test-leader-leading-state.btm")
public class LeaderLeadingStateTest {
    private static final Logger LOG = LoggerFactory.getLogger(LeaderLeadingStateTest.class);

    static CountDownLatch latch = new CountDownLatch(1);

    static volatile boolean flag = true;
    static volatile int injectNum = 0;
    static final Object sync = new Object();

    public static class StateMonitorHelper extends Helper {
        volatile boolean injected = false;
        protected StateMonitorHelper(Rule rule) {
            super(rule);
        }

        @Override
        public void killThread() {
            flag = false;
            latch.countDown();
        }

        public boolean inject(){
            synchronized (sync) {
                injectNum++;
                if (injectNum == 2) {
                    LOG.info("asdf inject");
                    return true;
                }
            }
            return false;
        }
    }

    static {
        System.setProperty("zookeeper.admin.enableServer", "false");
    }


    @Test
    public void leadingStateTest() throws IOException, QuorumPeerConfig.ConfigException, AdminServer.AdminServerException, InterruptedException {
        MockQuorumPeerMain peerMain3 = buildQuorumPeerMain(3);
        MockQuorumPeerMain peerMain1 = buildQuorumPeerMain(1);
        MockQuorumPeerMain peerMain2 = buildQuorumPeerMain(2);

        latch.await(10, TimeUnit.SECONDS);

        shutdown(peerMain1);
        shutdown(peerMain2);
        shutdown(peerMain3);

        if (!QuorumZooKeeperServer.leaderStateTestFlag) {
            throw new IllegalStateException("State error");
        }
    }

    private void shutdown(MockQuorumPeerMain peerMain) {
        try {
            peerMain.getQuorumPeer().shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MockQuorumPeerMain buildQuorumPeerMain(int id) throws IOException, QuorumPeerConfig.ConfigException, AdminServer.AdminServerException {
        MockQuorumPeerMain peerMain = new MockQuorumPeerMain();

        QuorumPeerConfigTest.MockQuorumPeerConfig peerConfig = new QuorumPeerConfigTest.MockQuorumPeerConfig(id);
        peerConfig.parseProperties(buildConfigProperties(id));
        peerMain.runFromConfig(peerConfig);
        return peerMain;
    }


    private Properties buildConfigProperties(int id) {
        Properties properties = new Properties();
        properties.setProperty("tickTime", "2000");
        properties.setProperty("initLimit", "10");
        properties.setProperty("syncLimit", "2000");
        properties.setProperty("dataDir", "/tmp/zookeeper" + id);
        properties.setProperty("clientPort", "218" + id);
        properties.setProperty("server.1", "127.0.0.1:2887:3887");
        properties.setProperty("server.2", "127.0.0.1:2888:3888");
        properties.setProperty("server.3", "127.0.0.1:2889:3889");
        return properties;
    }

    static class MockQuorumPeerMain extends QuorumPeerMain {

        private static final Logger LOG = LoggerFactory.getLogger(MockQuorumPeerMain.class);

        private volatile QuorumPeer quorumPeer;

        @Override
        public void runFromConfig(QuorumPeerConfig config) throws IOException, AdminServer.AdminServerException {
//        try {
//            ManagedUtil.registerLog4jMBeans();
//        } catch (JMException e) {
//            LOG.warn("Unable to register log4j JMX control", e);
//        }

            LOG.info("Starting quorum peer, myid=" + config.getServerId());
            final MetricsProvider metricsProvider;
            try {
                metricsProvider = MetricsProviderBootstrap.startMetricsProvider(
                        config.getMetricsProviderClassName(),
                        config.getMetricsProviderConfiguration());
            } catch (MetricsProviderLifeCycleException error) {
                throw new IOException("Cannot boot MetricsProvider " + config.getMetricsProviderClassName(), error);
            }
            try {
                ServerMetrics.metricsProviderInitialized(metricsProvider);
                ProviderRegistry.initialize();
                ServerCnxnFactory cnxnFactory = null;
                ServerCnxnFactory secureCnxnFactory = null;

                if (config.getClientPortAddress() != null) {
                    cnxnFactory = ServerCnxnFactory.createFactory();
                    cnxnFactory.configure(config.getClientPortAddress(), config.getMaxClientCnxns(), config.getClientPortListenBacklog(), false);
                }

                if (config.getSecureClientPortAddress() != null) {
                    secureCnxnFactory = ServerCnxnFactory.createFactory();
                    secureCnxnFactory.configure(config.getSecureClientPortAddress(), config.getMaxClientCnxns(), config.getClientPortListenBacklog(), true);
                }

                quorumPeer = getQuorumPeer();
                quorumPeer.setTxnFactory(new FileTxnSnapLog(config.getDataLogDir(), config.getDataDir()));
                quorumPeer.enableLocalSessions(config.areLocalSessionsEnabled());
                quorumPeer.enableLocalSessionsUpgrading(config.isLocalSessionsUpgradingEnabled());
                //quorumPeer.setQuorumPeers(config.getAllMembers());
                quorumPeer.setElectionType(config.getElectionAlg());
                quorumPeer.setMyid(config.getServerId());
                quorumPeer.setTickTime(config.getTickTime());
                quorumPeer.setMinSessionTimeout(config.getMinSessionTimeout());
                quorumPeer.setMaxSessionTimeout(config.getMaxSessionTimeout());
                quorumPeer.setInitLimit(config.getInitLimit());
                quorumPeer.setSyncLimit(config.getSyncLimit());
                quorumPeer.setConnectToLearnerMasterLimit(config.getConnectToLearnerMasterLimit());
                quorumPeer.setObserverMasterPort(config.getObserverMasterPort());
                quorumPeer.setConfigFileName(config.getConfigFilename());
                quorumPeer.setClientPortListenBacklog(config.getClientPortListenBacklog());
                quorumPeer.setZKDatabase(new ZKDatabase(quorumPeer.getTxnFactory()));
                quorumPeer.setQuorumVerifier(config.getQuorumVerifier(), false);
                if (config.getLastSeenQuorumVerifier() != null) {
                    quorumPeer.setLastSeenQuorumVerifier(config.getLastSeenQuorumVerifier(), false);
                }
                quorumPeer.initConfigInZKDatabase();
                quorumPeer.setCnxnFactory(cnxnFactory);
                quorumPeer.setSecureCnxnFactory(secureCnxnFactory);
                quorumPeer.setSslQuorum(config.isSslQuorum());
                quorumPeer.setUsePortUnification(config.shouldUsePortUnification());
                quorumPeer.setLearnerType(config.getPeerType());
                quorumPeer.setSyncEnabled(config.getSyncEnabled());
                quorumPeer.setQuorumListenOnAllIPs(config.getQuorumListenOnAllIPs());
                if (config.sslQuorumReloadCertFiles) {
                    quorumPeer.getX509Util().enableCertFileReloading();
                }
                quorumPeer.setMultiAddressEnabled(config.isMultiAddressEnabled());
                quorumPeer.setMultiAddressReachabilityCheckEnabled(config.isMultiAddressReachabilityCheckEnabled());
                quorumPeer.setMultiAddressReachabilityCheckTimeoutMs(config.getMultiAddressReachabilityCheckTimeoutMs());

                // sets quorum sasl authentication configurations
                quorumPeer.setQuorumSaslEnabled(config.quorumEnableSasl);
                if (quorumPeer.isQuorumSaslAuthEnabled()) {
                    quorumPeer.setQuorumServerSaslRequired(config.quorumServerRequireSasl);
                    quorumPeer.setQuorumLearnerSaslRequired(config.quorumLearnerRequireSasl);
                    quorumPeer.setQuorumServicePrincipal(config.quorumServicePrincipal);
                    quorumPeer.setQuorumServerLoginContext(config.quorumServerLoginContext);
                    quorumPeer.setQuorumLearnerLoginContext(config.quorumLearnerLoginContext);
                }
                quorumPeer.setQuorumCnxnThreadsSize(config.quorumCnxnThreadsSize);
                quorumPeer.initialize();

                if (config.jvmPauseMonitorToRun) {
                    quorumPeer.setJvmPauseMonitor(new JvmPauseMonitor(config));
                }

                quorumPeer.start();
                ZKAuditProvider.addZKStartStopAuditLog();
//                quorumPeer.join();
            } finally {
                try {
                    metricsProvider.stop();
                } catch (Throwable error) {
                    LOG.warn("Error while stopping metrics", error);
                }
            }
        }

        @Override
        protected QuorumPeer getQuorumPeer() throws SaslException {
            return quorumPeer == null ? new QuorumPeer() : quorumPeer;
        }


    }

}

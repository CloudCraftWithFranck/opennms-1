package org.opennms.smoketest.dcb;

import static org.junit.Assert.fail;

import java.net.InetSocketAddress;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import com.github.dockerjava.api.command.CreateContainerCmd;

public class DcbEndToEndIT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinion()
            .build());

    public static SshTftpContainer sshTftpContainer = new SshTftpContainer();

    static class SshTftpContainer extends GenericContainer {
        public static final String USERNAME = "dcbuser";
        public static final String PASSWORD = "dcbpass";

        public SshTftpContainer() {
            super("linuxserver/openssh-server");
            withEnv("PASSWORD_ACCESS", "true")
            .withEnv("SUDO_ACCESS", "true")
            .withEnv("USER_NAME", USERNAME)
            .withEnv("USER_PASSWORD", PASSWORD)
            .withExposedPorts(2222)
            .withNetwork(Network.SHARED)
            .withNetworkAliases(OpenNMSContainer.ELASTIC_ALIAS)
            .withCreateContainerCmdModifier(cmd -> {
                final CreateContainerCmd createCmd = (CreateContainerCmd)cmd;
                TestContainerUtils.setGlobalMemAndCpuLimits(createCmd);
            });
        }

        public InetSocketAddress getAddress() {
            return InetSocketAddress.createUnresolved(getContainerIpAddress(), getMappedPort(2222));
        }
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        sshTftpContainer.execInContainer("apk", "update");
        sshTftpContainer.execInContainer("apk", "add", "tftp-hpa");
    }

    @Test
    public void testSomething() {
        System.err.println(sshTftpContainer.getAddress());
        System.err.println(stack.opennms().getWebAddress());
        System.err.println(stack.minion().getSshAddress());
        fail();
    }
}

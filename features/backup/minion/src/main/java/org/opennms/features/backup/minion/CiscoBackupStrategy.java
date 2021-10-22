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

package org.opennms.features.backup.minion;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.opennms.features.backup.api.BackupStrategy;
import org.opennms.features.backup.api.Config;
import org.opennms.features.backup.api.ConfigType;
import org.opennms.features.backup.api.Const;

public class CiscoBackupStrategy implements BackupStrategy {

    @Override
    public Config getConfig(String ipAddress, int port, Map<String, String> params) {
        Config config = new Config();
        SshClient sshClient = null;
        try {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(ipAddress, port);
            sshClient = new SshClient(inetSocketAddress, params.get(Const.DEVICE_USER), params.get(Const.DEVICE_KEY));
            final PrintStream pipe = sshClient.openShell();

            pipe.println("terminal length 0");
            pipe.println("show run");
            pipe.println("logout");
            long start = System.currentTimeMillis();

            boolean didClose = false;
            while (System.currentTimeMillis() < (start + 5000)) {
                if (sshClient.isShellClosed()) {
                    didClose = true;
                    break;
                }
                Thread.sleep(500);
            }
            if (!didClose) {
                throw new RuntimeException("Shell not closed in time.");
            }

            // Read stdout
            String shellOutput = sshClient.getStdout();
            //remove command lines from the result
            String regexTarget = "(cisco#)[a-z0-9 ]*";
            String configData = shellOutput.replaceAll(regexTarget, "");

            config.setData(configData.getBytes());
            config.setType(ConfigType.TEXT);
            config.setRetrievedAt(new Date());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            config.setMessage(e.getMessage());
        }
        return config;
    }

    public static void main(String[] args) {
        CiscoBackupStrategy ciscoBackupStrategy = new CiscoBackupStrategy();
        Map params = new HashMap();
        params.put(Const.DEVICE_USER, "???");
        Path fileName = Path.of("???");
        String actual = null;
        try {
            actual = Files.readString(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        params.put(Const.DEVICE_KEY, actual);
        Config config = ciscoBackupStrategy.getConfig("???", 22, params);
        System.out.println(config);
    }
}

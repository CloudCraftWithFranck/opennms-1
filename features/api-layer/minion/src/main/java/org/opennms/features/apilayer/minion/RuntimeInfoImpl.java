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

package org.opennms.features.apilayer.minion;

import java.util.Objects;

import org.opennms.distributed.core.api.Identity;
import org.opennms.features.apilayer.common.VersionBean;
import org.opennms.integration.api.v1.runtime.Container;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import org.opennms.integration.api.v1.runtime.Version;
import org.osgi.framework.FrameworkUtil;

public class RuntimeInfoImpl implements RuntimeInfo {

    private final Version version;
    private final Identity identity;

    public RuntimeInfoImpl(Identity identity) {
        version = new VersionBean(FrameworkUtil.getBundle(getClass()).getVersion().toString());
        this.identity = Objects.requireNonNull(identity);
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public boolean isMeridian() {
        return version.getMajor() >= 2015;
    }

    @Override
    public Container getContainer() {
        return Container.MINION;
    }

    @Override
    public String getSystemId() {
        return identity.getId();
    }

    @Override
    public String getSystemLocation() {
        return identity.getLocation();
    }
}
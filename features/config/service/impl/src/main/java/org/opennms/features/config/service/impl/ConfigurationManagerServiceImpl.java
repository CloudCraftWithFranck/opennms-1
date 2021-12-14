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

package org.opennms.features.config.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.opennms.features.config.dao.api.ConfigData;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.api.ConfigStoreDao;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.opennms.features.config.service.util.OpenAPIConfigHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
public class ConfigurationManagerServiceImpl implements ConfigurationManagerService {
    private final static Logger LOG = LoggerFactory.getLogger(ConfigurationManagerServiceImpl.class);
    private final ConfigStoreDao<JSONObject> configStoreDao;
    // This map contains key: ConfigUpdateInfo value: list of Consumer
    private final ConcurrentHashMap<ConfigUpdateInfo, Collection<Consumer<ConfigUpdateInfo>>> onloadNotifyMap = new ConcurrentHashMap<>();

    public ConfigurationManagerServiceImpl(final ConfigStoreDao<JSONObject> configStoreDao) {
        this.configStoreDao = configStoreDao;
    }

    @Override
    public void registerConfigDefinition(String configName, ConfigDefinition configDefinition) throws JsonProcessingException {
        Objects.requireNonNull(configName);
        Objects.requireNonNull(configDefinition);

        if (this.getRegisteredConfigDefinition(configName).isPresent()) {
            throw new IllegalArgumentException(String.format("Schema with id=%s is already registered.", configName));
        }
        try {
            configStoreDao.register(configDefinition);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void changeConfigDefinition(String configName, ConfigDefinition configDefinition) throws IOException {
        Objects.requireNonNull(configName);
        Objects.requireNonNull(configDefinition);
        if (this.getRegisteredConfigDefinition(configName).isEmpty()) {
            throw new IllegalArgumentException(String.format("Schema with id=%s is not present. Use registerSchema instead.", configName));
        }
        configStoreDao.updateConfigDefinition(configDefinition);
    }

    @Override
    public Map<String, ConfigDefinition> getAllConfigDefinition() {
        return configStoreDao.getAllConfigDefinition();
    }

    @Override
    public Optional<ConfigDefinition> getRegisteredConfigDefinition(String configName) throws JsonProcessingException {
        Objects.requireNonNull(configName);
        return configStoreDao.getConfigDefinition(configName);
    }

    @Override
    public void registerReloadConsumer(ConfigUpdateInfo info, Consumer<ConfigUpdateInfo> consumer) {
        onloadNotifyMap.compute(info, (k, v) -> {
            if (v == null) {
                ArrayList<Consumer<ConfigUpdateInfo>> consumers = new ArrayList<>();
                consumers.add(consumer);
                return consumers;
            } else {
                v.add(consumer);
                return v;
            }
        });
    }

    /**
     * It will be trigger when a config is updated.
     *
     * @param configUpdateInfo
     */
    private void triggerReloadConsumer(ConfigUpdateInfo configUpdateInfo) {
        LOG.debug("Calling onReloaded callbacks");
        onloadNotifyMap.computeIfPresent(configUpdateInfo, (k, v) -> {
            v.forEach(c -> {
                try {
                    c.accept(configUpdateInfo);
                } catch (Exception e) {
                    LOG.warn("Fail to notify configName: {}, callback: {}, error: {}",
                            configUpdateInfo.getConfigName(), v, e.getMessage());
                }
            });
            return v;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerConfiguration(final String configName, final String configId, JsonAsString configObject)
            throws IOException {
        Objects.requireNonNull(configId);
        Objects.requireNonNull(configName);
        Objects.requireNonNull(configObject);
        Optional<ConfigDefinition> configDefinition = this.getRegisteredConfigDefinition(configName);
        if (configDefinition.isEmpty()) {
            throw new IllegalArgumentException(String.format("Unknown service with id=%s.", configName));
        }
        if (this.getJSONConfiguration(configName, configId).isPresent()) {
            throw new IllegalArgumentException(String.format(
                    "Configuration with service=%s, id=%s is already registered, update instead.", configName, configId));
        }


        configStoreDao.addConfig(configName, configId, new JSONObject(configObject.toString()));
        LOG.info("ConfigurationManager.registeredConfiguration(service={}, id={}, config={});", configName, configId, configObject);
    }

    @Override
    public void unregisterConfiguration(final String configName, final String configId) throws IOException {
        this.configStoreDao.deleteConfig(configName, configId);
    }

    @Override
    public void updateConfiguration(String configName, String configId, JsonAsString config) throws IOException {
        configStoreDao.updateConfig(configName, configId, new JSONObject(config.toString()));
        ConfigUpdateInfo updateInfo = new ConfigUpdateInfo(configName, configId);
        this.triggerReloadConsumer(updateInfo);
    }

    @Override
    public Optional<JSONObject> getJSONConfiguration(final String configName, final String configId) throws IOException {
        Optional<JSONObject> configObj = configStoreDao.getConfig(configName, configId);
        if (configObj.isEmpty()) {
            return configObj;
        }
        // try to fill default value or empty signature
        Optional<ConfigDefinition> def = configStoreDao.getConfigDefinition(configName);
        if (def.isPresent()) {
            String schemaName = (String) def.get().getMetaValue(ConfigDefinition.TOP_LEVEL_ELEMENT_NAME_TAG);
            if (schemaName == null) { // assume if top element name is null, the top schema name is configName
                schemaName = configName;
            }
            OpenAPIConfigHelper.fillWithDefaultValue(def.get().getSchema(), schemaName, configObj.get());
        }
        return configObj;
    }

    @Override
    public Optional<String> getJSONStrConfiguration(String configName, String configId) throws IOException, IllegalArgumentException {
        Optional<JSONObject> config = this.getJSONConfiguration(configName, configId);
        if (config.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(config.get().toString());
    }

    @Override
    public Set<String> getConfigNames() {
        return configStoreDao.getConfigNames().get();
    }

    @Override
    public void unregisterSchema(String configName) throws IOException {
        configStoreDao.unregister(configName);
    }

    @Override
    public Set<String> getConfigIds(String configName) throws IOException {
        Optional<ConfigData<JSONObject>> configData = configStoreDao.getConfigData(configName);
        if (configData.isEmpty()) {
            return new HashSet<>();
        }
        return configData.get().getConfigs().keySet();
    }

    @Override
    public Optional<ConfigData<JSONObject>> getConfigData(String configName) throws IOException {
        return configStoreDao.getConfigData(configName);
    }


//    //TODO: CHECK WHAT IS THAT FOR
//    @Override
//    public Optional<ConfigData<JSONObject>> getConfigurationMetaData(String configName) {
//        Objects.requireNonNull(serviceId);
//        return configStoreDao.getConfigData(configName).get();
//    }
/*
    @Override
    public ConfigData getSchemaForConfiguration(String configName) {
        return configStoreDao.getConfigSchema()
    }
*/
//    //TODO: CHECK later
//    @Override
//    public ConfigData<JSONObject> getSchemaForConfiguration(String configName) {
//        return null;
//    }


//    @Override
//    public void putConfiguration(final String configId, final JSONObject configContent) {
//        Objects.requireNonNull(configId);
//        Objects.requireNonNull(configContent);
//        if(!this.isConfigurationRegistered(configId)) {
//            throw new IllegalArgumentException(String.format("Unknown configuration with configId=%s. Please register first.", configId));
//        }
//        store.put(configId, configContent.toString(), STORE_CONTEXT_CONFIG);
//    }
//
//    private String getNamespaceForConfigId(final String configId) {
//        return getSchemaForConfiguration(configId)
//                .getXmlSchema()
//                .getNamespace();
//    }
//
//    @Override
//    public void putConfiguration(String configId, JSONObject object) {
//
//    }
//
//    @Override
//    public void removeConfiguration(String configId, String path) {
//
//    }


//    private void putConfigurationWithException(final String configId, final String path, final String content) throws DocumentException, IOException, SAXException {
//        Objects.requireNonNull(configId);
//        Objects.requireNonNull(path);
//        Document config = getConfigurationAsDocument(configId);
//        int index;
//        String namespace = getNamespaceForConfigId(configId);
//        final Element element = (Element) PathUtil.selectSingleNode(config, path, namespace);
//        Branch parent;
//        if (element == null) {
//            parent = (Branch) config.selectSingleNode(PathUtil.getParentPath(path));
//            index = Optional.ofNullable(parent).map(Branch::content).map(List::size).orElse(0);
//        } else {
//            // node exists, we need to replace it. Lets remove the old one
//            parent = element.getParent();
//            index = Optional.ofNullable(parent).map(Branch::content).map(list -> list.indexOf(element)).orElse(0);
//            if (parent == null) {
//                config.remove(element);
//            } else {
//                parent.remove(element);
//            }
//        }
//
//        String elementName = PathUtil.getElementName(PathUtil.getLastElement(path));
//        Element newElement = DocumentHelper.createElement(elementName);
//        setContentOfElement(newElement, content);
//        if (parent == null) {
//            config.add(newElement);
//        } else {
//            parent.content().add(index, newElement); // changes to list is reflected in parent
//        }
//        writeConfiguration(configId, config);
//    }

//    private void writeConfiguration(final String configId, final Document configDoc) throws IOException, SAXException, DocumentException {
//        Configuration config = this.getConfigurationMetaData(configId)
//                .orElseThrow(() -> new NullPointerException(String.format("Config with id=%s does not exist. Register first.", configId)));
//        XMLSchema schema = this.getSchemaForConfiguration(config.getSchemaId()).getXmlSchema();
//        String xml = configDoc.asXML();
//
//        // validate
//        XMLValidator.validate(xml, schema);
//
//        // write
//        String json = xmlMapper.xmlToJson(configId, xml);
//        putConfiguration(configId, new JSONObject(json));
//    }
//
//    private Document getConfigurationAsDocument(final String configId) throws DocumentException {
//        Objects.requireNonNull(configId);
//        Document config;
//        Optional<String> configAsString = this.getConfiguration(configId)
//                .map(json -> xmlMapper.jsonToXml(configId, json.toString()));
//        if (configAsString.isPresent()) {
//            SAXReader reader = new SAXReader();
//            config = reader.read(new StringReader(configAsString.get()));
//        } else {
//            config = DocumentHelper.createDocument();
//        }
//        return config;
//    }
//    private String getNamespaceForConfigId(final String configId) {
//        return getSchemaForConfiguration(configId)
//                .getXmlSchema()
//                .getNamespace();
//    }

//    private void removeConfigurationWithException(final String configId, final String path) throws DocumentException, IOException, SAXException {
//        Objects.requireNonNull(configId);
//        Objects.requireNonNull(path);
//        Document config = getConfigurationAsDocument(configId);
//
//        for (Object nodeObj : PathUtil.selectNodes(config, path, getNamespaceForConfigId(configId))) {
//            Node node = (Node) nodeObj;
//            node.getParent().remove(node);
//        }
//
//        writeConfiguration(configId, config);
//    }
}
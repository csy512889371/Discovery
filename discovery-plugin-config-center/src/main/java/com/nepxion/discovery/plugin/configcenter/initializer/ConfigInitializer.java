package com.nepxion.discovery.plugin.configcenter.initializer;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nepxion.discovery.common.entity.RuleEntity;
import com.nepxion.discovery.plugin.configcenter.loader.LocalConfigLoader;
import com.nepxion.discovery.plugin.configcenter.loader.RemoteConfigLoader;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import com.nepxion.discovery.plugin.framework.config.PluginConfigParser;
import com.nepxion.discovery.plugin.framework.context.PluginContextAware;

public class ConfigInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigInitializer.class);

    @Autowired
    private PluginContextAware pluginContextAware;

    @Autowired
    private PluginAdapter pluginAdapter;

    @Autowired
    private PluginConfigParser pluginConfigParser;

    // @Autowired
    // private PluginEventWapper pluginEventWapper;

    @Autowired
    private LocalConfigLoader localConfigLoader;

    @Autowired(required = false)
    private RemoteConfigLoader remoteConfigLoader;

    @PostConstruct
    public void initialize() {
        Boolean registerControlEnabled = pluginContextAware.isRegisterControlEnabled();
        Boolean discoveryControlEnabled = pluginContextAware.isDiscoveryControlEnabled();

        if (!registerControlEnabled && !discoveryControlEnabled) {
            LOG.info("Register and Discovery controls are all disabled, ignore to initialize");

            return;
        }

        LOG.info("------------- Load Discovery Config --------------");

        String[] remoteConfigList = getRemoteConfigList();
        if (remoteConfigList != null) {
            String partialRemoteConfig = remoteConfigList[0];
            if (StringUtils.isNotEmpty(partialRemoteConfig)) {
                try {
                    RuleEntity ruleEntity = pluginConfigParser.parse(partialRemoteConfig);
                    pluginAdapter.setDynamicPartialRule(ruleEntity);
                } catch (Exception e) {
                    LOG.error("Parse partial remote config failed", e);
                }
            }

            String globalRemoteConfig = remoteConfigList[1];
            if (StringUtils.isNotEmpty(globalRemoteConfig)) {
                try {
                    RuleEntity ruleEntity = pluginConfigParser.parse(globalRemoteConfig);
                    pluginAdapter.setDynamicGlobalRule(ruleEntity);
                } catch (Exception e) {
                    LOG.error("Parse partial remote config failed", e);
                }
            }
        }

        String[] localConfigList = getLocalConfigList();
        if (localConfigList != null) {
            String localConfig = localConfigList[0];
            if (StringUtils.isNotEmpty(localConfig)) {
                try {
                    RuleEntity ruleEntity = pluginConfigParser.parse(localConfig);
                    pluginAdapter.setLocalRule(ruleEntity);
                } catch (Exception e) {
                    LOG.error("Parse local config failed", e);
                }
            }
        }

        if (remoteConfigList == null && localConfigList == null) {
            LOG.info("No configs are found");
        }

        // 初始化配置的时候，不应该触发fireParameterChanged的EventBus事件
        // pluginEventWapper.fireParameterChanged();

        LOG.info("--------------------------------------------------");
    }

    private String[] getRemoteConfigList() {
        if (remoteConfigLoader != null) {
            String[] configList = null;

            try {
                configList = remoteConfigLoader.getConfigList();
            } catch (Exception e) {
                LOG.warn("Get remote config list failed", e);
            }

            return configList;
        } else {
            LOG.info("Remote config loader isn't provided");
        }

        return null;
    }

    private String[] getLocalConfigList() {
        String[] configList = null;

        try {
            configList = localConfigLoader.getConfigList();
        } catch (Exception e) {
            LOG.warn("Get local config list failed", e);
        }

        return configList;
    }
}
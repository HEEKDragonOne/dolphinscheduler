/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.server.master.engine.task.runnable;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 *  TaskExecutionContext builder
 */

@Slf4j
public class TaskExecutionContextBuilder {

    public static TaskExecutionContextBuilder get() {
        return new TaskExecutionContextBuilder();
    }

    private final TaskExecutionContext taskExecutionContext;

    public TaskExecutionContextBuilder() {
        this.taskExecutionContext = new TaskExecutionContext();
    }

    /**
     * build taskInstance related info
     *
     * @param taskInstance taskInstance
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildTaskInstanceRelatedInfo(final TaskInstance taskInstance) {
        taskExecutionContext.setTaskInstanceId(taskInstance.getId());
        taskExecutionContext.setTaskName(taskInstance.getName());
        taskExecutionContext.setFirstSubmitTime(DateUtils.dateToTimeStamp(taskInstance.getFirstSubmitTime()));
        taskExecutionContext.setStartTime(DateUtils.dateToTimeStamp(taskInstance.getStartTime()));
        taskExecutionContext.setTaskType(taskInstance.getTaskType());
        taskExecutionContext.setLogPath(taskInstance.getLogPath());
        taskExecutionContext.setWorkerGroup(taskInstance.getWorkerGroup());
        taskExecutionContext.setHost(taskInstance.getHost());
        taskExecutionContext.setVarPool(taskInstance.getVarPool());
        taskExecutionContext.setDryRun(taskInstance.getDryRun());
        taskExecutionContext.setTestFlag(taskInstance.getTestFlag());
        taskExecutionContext.setCpuQuota(taskInstance.getCpuQuota());
        taskExecutionContext.setMemoryMax(taskInstance.getMemoryMax());
        taskExecutionContext.setAppIds(taskInstance.getAppLink());
        return this;
    }

    public TaskExecutionContextBuilder buildTaskDefinitionRelatedInfo(final TaskDefinition taskDefinition) {
        // todo: remove the timeout setting here the timeout strategy should be used at master
        taskExecutionContext.setTaskTimeout(Integer.MAX_VALUE);
        if (taskDefinition.getTimeoutFlag() == TimeoutFlag.OPEN) {
            taskExecutionContext.setTaskTimeoutStrategy(taskDefinition.getTimeoutNotifyStrategy());
            if (taskDefinition.getTimeoutNotifyStrategy() == TaskTimeoutStrategy.FAILED
                    || taskDefinition.getTimeoutNotifyStrategy() == TaskTimeoutStrategy.WARNFAILED) {
                taskExecutionContext.setTaskTimeout(
                        (int) Math.min(TimeUnit.MINUTES.toSeconds(taskDefinition.getTimeout()), Integer.MAX_VALUE));
            }
        }
        taskExecutionContext.setTaskParams(taskDefinition.getTaskParams());
        return this;
    }

    /**
     * build processInstance related info
     *
     * @param workflowInstance processInstance
     * @return TaskExecutionContextBuilder
     */
    public TaskExecutionContextBuilder buildProcessInstanceRelatedInfo(final WorkflowInstance workflowInstance) {
        taskExecutionContext.setWorkflowInstanceId(workflowInstance.getId());
        taskExecutionContext.setScheduleTime(DateUtils.dateToTimeStamp(workflowInstance.getScheduleTime()));
        taskExecutionContext.setGlobalParams(workflowInstance.getGlobalParams());
        taskExecutionContext.setExecutorId(workflowInstance.getExecutorId());
        taskExecutionContext.setCmdTypeIfComplement(workflowInstance.getCmdTypeIfComplement().getCode());
        taskExecutionContext.setTenantCode(workflowInstance.getTenantCode());
        taskExecutionContext.setWorkflowDefinitionCode(workflowInstance.getWorkflowDefinitionCode());
        taskExecutionContext.setWorkflowDefinitionVersion(workflowInstance.getWorkflowDefinitionVersion());
        taskExecutionContext.setProjectCode(workflowInstance.getProjectCode());
        return this;
    }

    public TaskExecutionContextBuilder buildResourceParameters(final ResourceParametersHelper parametersHelper) {
        taskExecutionContext.setResourceParametersHelper(parametersHelper);
        return this;
    }

    /**
     * build k8sTask related info
     *
     * @param k8sTaskExecutionContext sqoopTaskExecutionContext
     * @return TaskExecutionContextBuilder
     */

    public TaskExecutionContextBuilder buildK8sTaskRelatedInfo(final K8sTaskExecutionContext k8sTaskExecutionContext) {
        taskExecutionContext.setK8sTaskExecutionContext(k8sTaskExecutionContext);
        return this;
    }

    /**
     * build global and local params
     *
     * @param propertyMap
     * @return
     */
    public TaskExecutionContextBuilder buildPrepareParams(final Map<String, Property> propertyMap) {
        taskExecutionContext.setPrepareParamsMap(propertyMap);
        return this;
    }

    /**
     * build business params
     *
     * @param businessParamsMap
     * @return
     */
    public TaskExecutionContextBuilder buildBusinessParams(final Map<String, Property> businessParamsMap) {
        taskExecutionContext.setParamsMap(businessParamsMap);
        return this;
    }

    public TaskExecutionContextBuilder buildWorkflowInstanceHost(final String masterHost) {
        taskExecutionContext.setWorkflowInstanceHost(masterHost);
        return this;
    }

    public TaskExecutionContextBuilder buildEnvironmentConfig(final String environmentConfig) {
        taskExecutionContext.setEnvironmentConfig(environmentConfig);
        return this;
    }

    /**
     * create
     *
     * @return taskExecutionContext
     */

    public TaskExecutionContext create() {
        checkNotNull(taskExecutionContext.getWorkflowInstanceHost(), "The workflow instance host cannot be empty");
        return taskExecutionContext;
    }

}

/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.ecs.command.runner;

import com.epion_t3.aws.core.configuration.AwsCredentialsProviderConfiguration;
import com.epion_t3.aws.core.configuration.AwsSdkHttpClientConfiguration;
import com.epion_t3.aws.core.holder.AwsCredentialsProviderHolder;
import com.epion_t3.aws.core.holder.AwsSdkHttpClientHolder;
import com.epion_t3.aws.ecs.bean.AwsEcsTaskInfo;
import com.epion_t3.aws.ecs.command.model.AwsEcsRunTask;
import com.epion_t3.aws.ecs.command.model.AwsEcsRunTaskWaitStopped;
import com.epion_t3.aws.ecs.messages.AwsEcsMessages;
import com.epion_t3.core.command.bean.CommandResult;
import com.epion_t3.core.command.runner.impl.AbstractCommandRunner;
import com.epion_t3.core.exception.SystemException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.AssignPublicIp;
import software.amazon.awssdk.services.ecs.model.AwsVpcConfiguration;
import software.amazon.awssdk.services.ecs.model.ContainerOverride;
import software.amazon.awssdk.services.ecs.model.DescribeTasksRequest;
import software.amazon.awssdk.services.ecs.model.DesiredStatus;
import software.amazon.awssdk.services.ecs.model.KeyValuePair;
import software.amazon.awssdk.services.ecs.model.NetworkConfiguration;
import software.amazon.awssdk.services.ecs.model.RunTaskRequest;
import software.amazon.awssdk.services.ecs.model.TaskOverride;

import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class AwsEcsRunTaskWaitStoppedRunner extends AbstractCommandRunner<AwsEcsRunTaskWaitStopped> {

    /**
     * オブジェクトマッパー.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.findAndRegisterModules();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public CommandResult execute(AwsEcsRunTaskWaitStopped command, Logger logger) throws Exception {

        var awsCredentialsProviderConfiguration = (AwsCredentialsProviderConfiguration) referConfiguration(
                command.getCredentialsConfigRef());

        var credentialsProvider = AwsCredentialsProviderHolder.getInstance()
                .getCredentialsProvider(awsCredentialsProviderConfiguration);

        var ecs = (EcsClient) null;
        if (StringUtils.isEmpty(command.getSdkHttpClientConfigRef())) {
            ecs = EcsClient.builder().credentialsProvider(credentialsProvider).build();
        } else {
            var sdkHttpClientConfiguration = (AwsSdkHttpClientConfiguration) referConfiguration(
                    command.getSdkHttpClientConfigRef());
            var sdkHttpClient = AwsSdkHttpClientHolder.getInstance().getSdkHttpClient(sdkHttpClientConfiguration);
            ecs = EcsClient.builder().credentialsProvider(credentialsProvider).httpClient(sdkHttpClient).build();
        }

        // 実行
        var taskInfos = runTask(ecs, command, logger);

        // 終了待ち
        waitTask(ecs, command, taskInfos, logger);

        // 最終参照
        describeTask(ecs, command, taskInfos, logger);

        return CommandResult.getSuccess();
    }

    /**
     * ECSTaskを実行します.
     *
     * @param ecs
     * @param command
     * @param logger
     * @return
     */
    private List<AwsEcsTaskInfo> runTask(EcsClient ecs, AwsEcsRunTaskWaitStopped command, Logger logger) {

        var requestBuilder = RunTaskRequest.builder();

        requestBuilder.cluster(command.getCluster());
        requestBuilder.taskDefinition(command.getTaskDefinition());

        if (StringUtils.isNotEmpty(command.getLaunchType())) {
            requestBuilder.launchType(command.getLaunchType());
        }
        if (StringUtils.isNotEmpty(command.getPlatformVersion())) {
            requestBuilder.platformVersion(command.getPlatformVersion());
        }
        if (StringUtils.isNotEmpty(command.getStartedBy())) {
            requestBuilder.startedBy(command.getStartedBy());
        }

        // タスクネットワーク設定上書き処理
        if (command.getNetworkConfiguration() != null) {
            var nc = command.getNetworkConfiguration();
            var nwcBuilder = NetworkConfiguration.builder();
            var vpcBuilder = AwsVpcConfiguration.builder();
            // サブネット設定
            if (CollectionUtils.isNotEmpty(nc.getSubnets())) {
                vpcBuilder.subnets(nc.getSubnets());
            }
            // セキュリティグループ設定
            if (CollectionUtils.isNotEmpty(nc.getSecurityGroups())) {
                vpcBuilder.securityGroups(nc.getSecurityGroups());
            }
            // 公開IPの自動付与設定
            if (StringUtils.isNotEmpty(nc.getAssignPublicIp())) {
                var assignPublicIp = AssignPublicIp.fromValue(nc.getAssignPublicIp());
                if (assignPublicIp == AssignPublicIp.UNKNOWN_TO_SDK_VERSION) {
                    throw new SystemException(AwsEcsMessages.AWS_ECS_ERR_9002, nc.getAssignPublicIp());
                }
                vpcBuilder.assignPublicIp(assignPublicIp);
            }
            nwcBuilder.awsvpcConfiguration(vpcBuilder.build());
            requestBuilder.networkConfiguration(nwcBuilder.build());
        }

        // タスク設定上書き処理
        if (command.getTaskOverride() != null) {
            var to = command.getTaskOverride();
            var taskOverride = TaskOverride.builder();
            if (to.getContainerOverride() != null) {
                var co = to.getContainerOverride();
                var containerOverrides = ContainerOverride.builder();
                // 名称の設定上書き
                if (StringUtils.isNotEmpty(co.getName())) {
                    containerOverrides.name(co.getName());
                }
                // 環境変数の設定上書き
                if (co.getEnvironment() != null) {
                    var keyValuePairs = co.getEnvironment()
                            .entrySet()
                            .stream()
                            .map(e -> KeyValuePair.builder().name(e.getKey()).value(e.getValue()).build())
                            .collect(Collectors.toList());
                    containerOverrides.environment(keyValuePairs);
                }
                taskOverride.containerOverrides(containerOverrides.build());
            }
            requestBuilder.overrides(taskOverride.build());
        }

        try {
            var request = requestBuilder.build();
            logger.info(collectLoggingMarker(), "run task request : {}", request);
            var response = ecs.runTask(requestBuilder.build());
            var taskInfos = (List<AwsEcsTaskInfo>) null;
            // 起動成功のタスク
            if (response.hasTasks()) {
                logger.info(collectLoggingMarker(), "run tasks : {}", response.tasks().toString());
                taskInfos = response.tasks()
                        .stream()
                        .map(x -> new AwsEcsTaskInfo(x.clusterArn(), x.taskArn(), x.startedBy(), x.lastStatus(),
                                x.desiredStatus(), x.healthStatusAsString()))
                        .collect(Collectors.toList());
            }
            // 起動失敗のタスク
            if (response.hasFailures() && !response.failures().isEmpty()) {
                logger.info("failure tasks : {}", response.failures().toString());
                // 起動失敗タスクがある場合、エラーとする
                throw new SystemException(AwsEcsMessages.AWS_ECS_ERR_9003);
            }
            return taskInfos;
        } catch (Exception e) {
            throw new SystemException(e, AwsEcsMessages.AWS_ECS_ERR_9001);
        }
    }

    private void waitTask(EcsClient ecs, AwsEcsRunTaskWaitStopped command, List<AwsEcsTaskInfo> tasks, Logger logger) {

        var tasksClone = new ArrayList<AwsEcsTaskInfo>();
        tasks.forEach(x -> {
            tasksClone.add(SerializationUtils.clone(x));
        });

        while (!tasksClone.isEmpty()) {

            try {
                Thread.sleep(command.getStoppedCheckInterval());
            } catch (InterruptedException e) {
                // ignore
            }
            logger.info(collectLoggingMarker(), "stopped check...");

            // リクエスト
            var requestBuilder = DescribeTasksRequest.builder();
            requestBuilder.cluster(command.getCluster());
            requestBuilder.tasks(tasksClone.stream().map(AwsEcsTaskInfo::getTaskArn).collect(Collectors.toList()));

            try {
                var request = requestBuilder.build();
                var response = ecs.describeTasks(request);
                logger.debug("describe task response : {}", response);

                if (response.hasTasks() && !response.tasks().isEmpty()) {
                    var stoppedTask = response.tasks()
                            .stream()
                            .filter(x -> DesiredStatus.STOPPED.toString().equals(x.lastStatus()))
                            .collect(Collectors.toList());
                    stoppedTask.forEach(x -> {
                        tasksClone.removeIf(y -> x.taskArn().equals(y.getTaskArn()));
                    });
                }
            } catch (Exception e) {
                throw new SystemException(e, AwsEcsMessages.AWS_ECS_ERR_9004);
            }
        }
    }

    private void describeTask(EcsClient ecs, AwsEcsRunTaskWaitStopped command, List<AwsEcsTaskInfo> tasks,
            Logger logger) {

        // リクエスト
        var requestBuilder = DescribeTasksRequest.builder();
        requestBuilder.cluster(command.getCluster());
        requestBuilder.tasks(tasks.stream().map(AwsEcsTaskInfo::getTaskArn).collect(Collectors.toList()));

        try {
            var request = requestBuilder.build();
            logger.info("describe task request : {}", request);
            var response = ecs.describeTasks(request);
            logger.info("describe task response : {}", response);

            if (response.hasTasks() && !response.tasks().isEmpty()) {
                var taskInfos = response.tasks()
                        .stream()
                        .map(x -> new AwsEcsTaskInfo(x.clusterArn(), x.taskArn(), x.startedBy(), x.lastStatus(),
                                x.desiredStatus(), x.healthStatusAsString()))
                        .collect(Collectors.toList());

                var evidencePath = getEvidencePath("runTaskWaitStopped.json");

                // ファイルエビデンス書き出し
                try (var fos = new FileOutputStream(evidencePath.toFile())) {
                    objectMapper.writeValue(fos, taskInfos);
                }

                // オブジェクトエビデンス登録
                registrationObjectEvidence(response.tasks());

                // ファイルエビデンス登録
                registrationFileEvidence(evidencePath);
            }
        } catch (Exception e) {
            throw new SystemException(e, AwsEcsMessages.AWS_ECS_ERR_9004);
        }
    }
}

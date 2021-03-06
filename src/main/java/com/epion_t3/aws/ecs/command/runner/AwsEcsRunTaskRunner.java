/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.ecs.command.runner;

import com.epion_t3.aws.core.configuration.AwsCredentialsProviderConfiguration;
import com.epion_t3.aws.core.configuration.AwsSdkHttpClientConfiguration;
import com.epion_t3.aws.core.holder.AwsCredentialsProviderHolder;
import com.epion_t3.aws.core.holder.AwsSdkHttpClientHolder;
import com.epion_t3.aws.ecs.command.model.AwsEcsRunTask;
import com.epion_t3.aws.ecs.messages.AwsEcsMessages;
import com.epion_t3.core.command.bean.CommandResult;
import com.epion_t3.core.command.runner.impl.AbstractCommandRunner;
import com.epion_t3.core.exception.SystemException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.AwsVpcConfiguration;
import software.amazon.awssdk.services.ecs.model.ContainerOverride;
import software.amazon.awssdk.services.ecs.model.KeyValuePair;
import software.amazon.awssdk.services.ecs.model.NetworkConfiguration;
import software.amazon.awssdk.services.ecs.model.RunTaskRequest;
import software.amazon.awssdk.services.ecs.model.TaskOverride;

import java.util.stream.Collectors;

public class AwsEcsRunTaskRunner extends AbstractCommandRunner<AwsEcsRunTask> {
    @Override
    public CommandResult execute(AwsEcsRunTask command, Logger logger) throws Exception {

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
            var response = ecs.runTask(requestBuilder.build());

            System.out.println(response.sdkHttpResponse().toString());

            response.tasks().forEach(x -> {
                x.taskArn();
            });

            return CommandResult.getSuccess();
        } catch (Exception e) {
            throw new SystemException(e, AwsEcsMessages.AWS_ECS_ERR_9001);
        }
    }
}

/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.ecs.command.runner;

import com.epion_t3.aws.core.configuration.AwsCredentialsProviderConfiguration;
import com.epion_t3.aws.core.configuration.AwsSdkHttpClientConfiguration;
import com.epion_t3.aws.core.holder.AwsCredentialsProviderHolder;
import com.epion_t3.aws.core.holder.AwsSdkHttpClientHolder;
import com.epion_t3.aws.ecs.bean.AwsEcsTaskInfo;
import com.epion_t3.aws.ecs.command.model.AwsEcsDescribeTask;
import com.epion_t3.aws.ecs.messages.AwsEcsMessages;
import com.epion_t3.core.command.bean.CommandResult;
import com.epion_t3.core.command.runner.impl.AbstractCommandRunner;
import com.epion_t3.core.exception.SystemException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.DescribeTasksRequest;

import java.io.FileOutputStream;
import java.util.stream.Collectors;

public class AwsEcsDescribeTaskRunner extends AbstractCommandRunner<AwsEcsDescribeTask> {

    /**
     * オブジェクトマッパー.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.findAndRegisterModules();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public CommandResult execute(AwsEcsDescribeTask command, Logger logger) throws Exception {

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

        // リクエスト
        var requestBuilder = DescribeTasksRequest.builder();
        requestBuilder.cluster(command.getCluster());
        requestBuilder.tasks(command.getTaskArn());

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

                var evidencePath = getEvidencePath("describeTask.json");

                // ファイルエビデンス書き出し
                try (var fos = new FileOutputStream(evidencePath.toFile())) {
                    objectMapper.writeValue(fos, taskInfos);
                }

                // オブジェクトエビデンス登録
                registrationObjectEvidence(response.tasks());

                // ファイルエビデンス登録
                registrationFileEvidence(evidencePath);
            }
            return CommandResult.getSuccess();
        } catch (Exception e) {
            throw new SystemException(e, AwsEcsMessages.AWS_ECS_ERR_9004);
        }
    }
}

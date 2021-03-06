/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.ecs.command.model;

import com.epion_t3.aws.ecs.command.runner.AwsEcsDescribeTaskRunner;
import com.epion_t3.core.common.annotation.CommandDefinition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@CommandDefinition(id = "AwsEcsDescribeTask", runner = AwsEcsDescribeTaskRunner.class)
public class AwsEcsDescribeTask extends AwsEcsBase {
    private String cluster;
    private String taskArn;
}

/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.ecs.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * AWSのECSタスク情報.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AwsEcsTaskInfo implements Serializable {
    private String clusterArn;
    private String taskArn;
    private String startedBy;
    private String lastStatus;
    private String desiredStatus;
    private String healthStatusAsString;
}

/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.ecs.command.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AwsEcsTaskOverride implements Serializable {

    /**
     * コンテナ設定上書き.
     */
    private AwsEcsContainerOverride containerOverride;
}

/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.ecs.command.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
public class AwsEcsContainerOverride implements Serializable {

    /**
     * 名前.
     */
    private String name;

    /**
     * 環境変数.
     */
    private Map<String, String> environment;

}

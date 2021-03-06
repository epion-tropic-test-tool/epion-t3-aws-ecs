/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.ecs.command.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * ECSのネットワーク関連の設定.
 */
@Getter
@Setter
public class AwsEcsNetworkConfiguration implements Serializable {

    /**
     * 起動サブネット.
     */
    private List<String> subnets;

    /**
     * 起動時に適用するセキュリティグループ.
     */
    private List<String> securityGroups;
}

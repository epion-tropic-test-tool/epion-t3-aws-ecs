/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.ecs.command.model;

import com.epion_t3.aws.ecs.command.runner.AwsEcsRunTaskWaitStoppedRunner;
import com.epion_t3.core.common.annotation.CommandDefinition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@CommandDefinition(id = "AwsEcsRunTaskWaitStopped", runner = AwsEcsRunTaskWaitStoppedRunner.class)
public class AwsEcsRunTaskWaitStopped extends AwsEcsBase {

    /**
     * クラスタ.
     */
    private String cluster;

    /**
     * タスク定義.
     */
    private String taskDefinition;

    /**
     * 起動タイプ.
     */
    private String launchType;

    /**
     * プラットフォームバージョン.
     */
    private String platformVersion;

    /**
     * 起動者.
     */
    private String startedBy;

    /**
     * ネットワーク設定.
     */
    private AwsEcsNetworkConfiguration networkConfiguration;

    /**
     * タスク定義上書き設定.
     */
    private AwsEcsTaskOverride taskOverride;

    /**
     * 終了確認を行うインターバル.
     */
    private Long stoppedCheckInterval;

}

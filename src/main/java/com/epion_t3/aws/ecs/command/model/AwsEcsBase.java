/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.ecs.command.model;

import com.epion_t3.core.common.bean.scenario.Command;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AwsEcsBase extends Command {

    /**
     * 資格情報設定の参照名.
     */
    private String credentialsConfigRef;

    /**
     * HTTPクライアント設定の参照名.
     */
    private String sdkHttpClientConfigRef;
}

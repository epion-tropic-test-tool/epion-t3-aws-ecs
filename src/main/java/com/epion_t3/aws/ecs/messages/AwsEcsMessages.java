/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.ecs.messages;

import com.epion_t3.core.message.Messages;

/**
 * aws-ecs用メッセージ定義Enum.<br>
 *
 * @author epion-t3-devtools
 */
public enum AwsEcsMessages implements Messages {

    /** ECSTaskの一部または全ての起動に失敗しました.ログを確認してください。 */
    AWS_ECS_ERR_9003("com.epion_t3.aws.ecs.err.9003"),

    /**
     * ECSTaskの起動時のパブリックIPの自動付与設定（assignPublicIp）の値が異なります.「DISABLED」or「ENABLED」を指定してください.
     * assignPublicIp: {0}
     */
    AWS_ECS_ERR_9002("com.epion_t3.aws.ecs.err.9002"),

    /** ECSTaskの参照に失敗しました. */
    AWS_ECS_ERR_9004("com.epion_t3.aws.ecs.err.9004"),

    /** ECSTaskの起動に失敗しました. */
    AWS_ECS_ERR_9001("com.epion_t3.aws.ecs.err.9001");

    /** メッセージコード */
    private String messageCode;

    /**
     * プライベートコンストラクタ<br>
     *
     * @param messageCode メッセージコード
     */
    private AwsEcsMessages(final String messageCode) {
        this.messageCode = messageCode;
    }

    /**
     * messageCodeを取得します.<br>
     *
     * @return messageCode
     */
    public String getMessageCode() {
        return this.messageCode;
    }
}

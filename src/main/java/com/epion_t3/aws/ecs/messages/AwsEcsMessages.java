/* Copyright (c) 2017-2021 Nozomu Takashima. */
package com.epion_t3.aws.ecs.messages;

import com.epion_t3.core.message.Messages;

/**
 * aws-ecs用メッセージ定義Enum.<br>
 *
 * @author epion-t3-devtools
 */
public enum AwsEcsMessages implements Messages {

    /** ECSTaskの起動に失敗しました。 */
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

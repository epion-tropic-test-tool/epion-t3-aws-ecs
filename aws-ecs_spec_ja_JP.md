#  カスタム機能ドキュメント

このドキュメントは、 のカスタム機能が提供する、
Flow、コマンド、設定定義についての説明及び出力するメッセージの定義について説明する。

- Contents
  - [Information](#Information)
  - [Description](#Description)
  - [Flow List](#Flow-List)
  - [Command List](#Command-List)
  - [Configuration List](#Configuration-List)
  - [Message List](#Message-List)

## Information

本カスタム機能の基本情報は以下の通り。

AWSのECSへのアクセスを行う機能を提供します。

- Name : `aws-ecs`
- Custom Package : `com.epion_t3.aws.ecs`

## Description
AWSのECSへのアクセスを行う機能を提供します。

## Flow List

本カスタム機能が提供するFlowの一覧及び詳細。

|Name|Summary|
|:---|:---|


## Command List

本カスタム機能が提供するコマンドの一覧及び詳細。

|Name|Summary|Assert|Evidence|
|:---|:---|:---|:---|
|[AwsEcsRunTask](#AwsEcsRunTask)|ECSTaskを実行します。  ||X|
|[AwsEcsDescribeTask](#AwsEcsDescribeTask)|ECSTaskを参照します。  ||X|

------

### AwsEcsRunTask
ECSTaskを実行します。
#### Command Type
- Assert : No
- Evidence : __Yes__

#### Functions
- ECSTaskを実行します。
- ECSTaskの実行結果をエビデンスとして保存します。（非同期実行結果となります）

#### Structure
```yaml
commands : 
  id : コマンドのID
  command : 「AwsEcsRunTask」固定
  summary : コマンドの概要（任意）
  description : コマンドの詳細（任意）
  cluster : 対象のECSクラスタを指定します。
  taskDefinition : 対象のタスク定義を指定します。
  launchType : 起動タイプを指定します。「EC2」もしくは「FARGATE」を指定してください。
  platformVersion : プラットフォームバージョンを指定します。
  startedBy : 起動元を指定します。起動元を特定したい場合に有効です。
  networkConfiguration : 起動時のネットワーク設定を行います。
    subnets : 起動するSubnetを指定します。複数指定可能です。
    securityGroups : 起動時に適用するセキュリティグループを指定します。複数指定可能です。
    assignPublicIp : 公開IPを付与するかを指定します。「DISABLED」or「ENABLED」を指定してください。
  taskOverride : タスク定義の上書き設定を行います。
    containerOverride : 起動するSubnetを指定します。複数指定可能です。
      name : 名称を指定します。
      environment : 環境変数を設定します。

```

------

### AwsEcsDescribeTask
ECSTaskを参照します。
#### Command Type
- Assert : No
- Evidence : __Yes__

#### Functions
- ECSTaskを参照します。
- ECSTaskの参照結果をエビデンスとして保存します。

#### Structure
```yaml
commands : 
  id : コマンドのID
  command : 「AwsEcsDescribeTask」固定
  summary : コマンドの概要（任意）
  description : コマンドの詳細（任意）
  cluster : 対象のECSクラスタを指定します。
  taskArn : 参照する対象のタスクのARNを指定します。

```


## Configuration List

本カスタム機能が提供する設定定義の一覧及び詳細。

|Name|Summary|
|:---|:---|


## Message List

本カスタム機能が出力するメッセージの一覧及び内容。

|MessageID|MessageContents|
|:---|:---|
|com.epion_t3.aws.ecs.err.9003|ECSTaskの一部または全ての起動に失敗しました.ログを確認してください。|
|com.epion_t3.aws.ecs.err.9002|ECSTaskの起動時のパブリックIPの自動付与設定（assignPublicIp）の値が異なります.「DISABLED」or「ENABLED」を指定してください. assignPublicIp: {0}|
|com.epion_t3.aws.ecs.err.9004|ECSTaskの参照に失敗しました.|
|com.epion_t3.aws.ecs.err.9001|ECSTaskの起動に失敗しました.|

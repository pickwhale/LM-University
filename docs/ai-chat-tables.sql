-- AI 聊天功能手动数据库更新脚本。
-- 使用 AI 聊天功能前，请在现有 `university` 数据库中执行本脚本。
-- 当前项目未启用自动数据库迁移，本脚本不会自动执行。
-- AI 大模型配置独立存储在 `ai_model_config` 表中，不再写入旧的 `config` 表。

DELETE FROM `config` WHERE `name` LIKE 'ai.%';

CREATE TABLE IF NOT EXISTS `ai_model_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `enabled` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否启用',
  `provider_name` varchar(100) DEFAULT NULL COMMENT '服务商名称',
  `endpoint_url` varchar(500) DEFAULT NULL COMMENT '接口地址',
  `http_method` varchar(20) NOT NULL DEFAULT 'POST' COMMENT '请求方法',
  `api_key` longtext COMMENT '接口密钥',
  `headers_template` longtext COMMENT '请求头模板JSON',
  `body_template` longtext COMMENT '请求体模板JSON',
  `model` varchar(200) DEFAULT NULL COMMENT '模型名称',
  `temperature` double DEFAULT '0.7' COMMENT '温度参数',
  `max_tokens` int(11) DEFAULT '1024' COMMENT '最大输出长度',
  `system_prompt` longtext COMMENT '系统提示词',
  `stream_protocol` varchar(20) DEFAULT 'AUTO' COMMENT '流式协议',
  `response_text_path` varchar(300) DEFAULT 'choices.0.delta.content' COMMENT '响应文本路径',
  `done_marker` varchar(100) DEFAULT '[DONE]' COMMENT '结束标记',
  `timeout_seconds` int(11) DEFAULT '60' COMMENT '超时秒数',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='AI大模型配置';

CREATE TABLE IF NOT EXISTS `ai_chat_conversation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_id` bigint(20) NOT NULL COMMENT '学生id',
  `title` varchar(200) NOT NULL COMMENT '会话标题',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_chat_conversation_student` (`student_id`),
  KEY `idx_ai_chat_conversation_updated` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='AI聊天会话';

CREATE TABLE IF NOT EXISTS `ai_chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `conversation_id` bigint(20) NOT NULL COMMENT 'AI聊天会话id',
  `role` varchar(32) NOT NULL COMMENT '消息角色',
  `content` longtext NOT NULL COMMENT '消息内容',
  `sources_json` longtext COMMENT '引用来源JSON',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_ai_chat_message_conversation` (`conversation_id`),
  KEY `idx_ai_chat_message_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='AI聊天消息';

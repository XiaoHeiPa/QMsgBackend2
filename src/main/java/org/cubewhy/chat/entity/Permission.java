package org.cubewhy.chat.entity;

public enum Permission {
    // admin permissions
    DASHBOARD, // 访问后端的仪表盘
    MANAGE_USER, // 管理所有用户
    MANAGE_CHANNEL, // 管理频道
    MANAGE_GROUP, // 管理所有群组
    REGISTER_INVITE, // 生成注册邀请

    // user permissions
    SEND_MESSAGE, // 发送消息
    CREATE_CHANNEL, // 创建频道
    JOIN_CHANNEL, // 加入频道
    VIEW_CHANNEL,
    SEND_CHANNEL_INVITE // 发送加频道邀请
}

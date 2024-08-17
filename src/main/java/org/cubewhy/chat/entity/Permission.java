package org.cubewhy.chat.entity;

public enum Permission {
    // servlet admin rights
    DASHBOARD, // 访问后端的仪表盘
    MANAGE_USER, // 管理所有用户
    // servlet admin & channel admin rights
    MANAGE_CHANNEL, // 管理频道
    REGISTER_INVITE, // 生成注册邀请
    DISBAND_CHANNEL, // 解散频道
    KICK_USERS, // 频道内为踢出成员,服务器内为注销账户

    // user permissions
    SEND_MESSAGE, // 发送消息
    CREATE_CHANNEL, // 创建频道
    JOIN_CHANNEL, // 加入频道
    VIEW_CHANNEL,
    SEND_CHANNEL_INVITE // 发送加频道邀请
}

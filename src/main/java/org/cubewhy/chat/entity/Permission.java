package org.cubewhy.chat.entity;

import lombok.Getter;

@Getter
public enum Permission {
    // servlet admin rights
    DASHBOARD(Type.SERVLET), // 访问后端的仪表盘
    MANAGE_USER(Type.SERVLET), // 管理所有用户
    MANAGE_ROLES(Type.CHANNEL_AND_SERVLET), // 管理身份组
    MANAGE_FILES(Type.CHANNEL_AND_SERVLET), // 管理用户上传的文件
    REGISTER_INVITE(Type.SERVLET), // 生成注册邀请
    // servlet admin & channel admin rights
    MANAGE_CHANNEL(Type.CHANNEL_AND_SERVLET), // 管理频道
    DISBAND_CHANNEL(Type.CHANNEL_AND_SERVLET), // 解散频道
    KICK_USERS(Type.CHANNEL_AND_SERVLET), // 频道内为踢出成员,服务器内为注销账户
    // channel admin rights
    PROCESS_JOIN_REQUEST(Type.CHANNEL), // 处理加频道请求

    // user permissions
    SEND_MESSAGE(Type.CHANNEL_AND_SERVLET), // 发送消息
    CREATE_CHANNEL(Type.SERVLET), // 创建频道
    JOIN_CHANNEL(Type.SERVLET), // 加入频道
    VIEW_CHANNEL(Type.CHANNEL), // 查看消息
    SEND_CHANNEL_INVITE(Type.CHANNEL), // 发送加频道邀请
    UPLOAD_FILES(Type.CHANNEL_AND_SERVLET), // 上传文件
    DOWNLOAD_FILES(Type.CHANNEL_AND_SERVLET); // 下载文件

    private final Type type;

    Permission(Type type) {
        this.type = type;
    }

    public enum Type {
        CHANNEL, // 群组权限
        SERVLET, // (全局) 服务器权限
        CHANNEL_AND_SERVLET // 重合
    }
}

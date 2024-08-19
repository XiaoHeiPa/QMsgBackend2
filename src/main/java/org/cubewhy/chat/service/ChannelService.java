package org.cubewhy.chat.service;

import jakarta.transaction.Transactional;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Channel;
import org.cubewhy.chat.entity.ChannelUser;
import org.cubewhy.chat.entity.Permission;
import org.cubewhy.chat.entity.dto.ChannelDTO;

import java.util.List;
import java.util.Optional;

public interface ChannelService {
    Channel createChannel(ChannelDTO channelDTO);

    Channel createChannel(Channel channel);

    Channel updateChannel(Long channelId, ChannelDTO channelDTO);

    void deleteChannel(Long channelId);

    Optional<Channel> getChannelById(Long channelId);

    List<Channel> getAllChannels();

    @Transactional
    void addUserToChannel(Long channelId, Long userId, Permission... permissions);

    @Transactional
    void addUserToChannel(Channel channel, Account user, Permission... permissions);

    void removeUserFromChannel(Long channelId, Long userId);

    List<Account> getUsersInChannel(Long channelId);

    boolean hasViewPermission(Account account, long channelId);

    List<ChannelUser> findChannelUsers(Account account);

    boolean disbandChannel(Long channelId);

    boolean approveJoinRequest(Long requestId);

    boolean rejectJoinRequest(Long requestId);

    Channel approveFriendRequest(Long requestId);

    boolean rejectFriendRequest(Long requestId);
}

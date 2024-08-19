package org.cubewhy.chat.service;

import jakarta.transaction.Transactional;
import org.cubewhy.chat.entity.*;
import org.cubewhy.chat.entity.dto.ChannelDTO;

import java.util.List;
import java.util.Optional;

public interface ChannelService {
    boolean existByName(String name);

    Channel createChannel(ChannelDTO channelDTO);

    Channel createChannel(Channel channel);

    Channel updateChannel(Long channelId, ChannelDTO channelDTO);

    void deleteChannel(Long channelId);

    Channel findChannelById(Long channelId);

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

    @Transactional
    boolean approveJoinRequest(ChannelJoinRequest request);

    boolean rejectJoinRequest(Long requestId);

    boolean rejectJoinRequest(ChannelJoinRequest joinRequest);

    Channel approveFriendRequest(Long requestId);

    @Transactional
    Channel approveFriendRequest(FriendRequest request);

    boolean rejectFriendRequest(Long requestId);

    boolean checkPermissions(Account account, Channel channel, Permission... permissions);

    boolean rejectFriendRequest(FriendRequest request);

    ChannelJoinRequest findJoinRequestById(long id);
}

package org.cubewhy.chat.service;

import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Channel;
import org.cubewhy.chat.entity.ChannelUser;
import org.cubewhy.chat.entity.dto.ChannelDTO;

import java.util.List;
import java.util.Optional;

public interface ChannelService {
    Channel createChannel(ChannelDTO channelDTO);

    Channel updateChannel(Long channelId, ChannelDTO channelDTO);

    void deleteChannel(Long channelId);

    Optional<Channel> getChannelById(Long channelId);

    List<Channel> getAllChannels();

    void addUserToChannel(Long channelId, Long userId);

    void removeUserFromChannel(Long channelId, Long userId);

    List<Account> getUsersInChannel(Long channelId);

    boolean hasViewPermission(Account account, long channelId);

    List<ChannelUser> findChannelUsers(Account account);
}

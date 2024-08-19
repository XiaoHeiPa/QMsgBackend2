package org.cubewhy.chat.service.impl;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Channel;
import org.cubewhy.chat.entity.ChannelUser;
import org.cubewhy.chat.entity.Permission;
import org.cubewhy.chat.entity.dto.ChannelDTO;
import org.cubewhy.chat.repository.AccountRepository;
import org.cubewhy.chat.repository.ChannelRepository;
import org.cubewhy.chat.repository.ChannelUserRepository;
import org.cubewhy.chat.service.ChannelService;
import org.cubewhy.chat.service.ChatMessageService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChannelServiceImpl implements ChannelService {
    @Resource
    private ChannelRepository channelRepository;

    @Resource
    private AccountRepository userRepository;

    @Resource
    private ChannelUserRepository channelUserRepository;

    @Resource
    ChatMessageService chatMessageService;

    @Override
    public Channel createChannel(ChannelDTO channelDTO) {
        Optional<Channel> existChannel = channelRepository.findByName(channelDTO.getName());
        if (existChannel.isPresent()) return existChannel.get();
        Channel channel = new Channel();
        channel.setName(channelDTO.getName());
        channel.setTitle(channelDTO.getTitle());
        channel.setDescription(channelDTO.getDescription());
        channel.setIconHash(channelDTO.getIconHash());
        channel.setPublicChannel(channelDTO.isPublicChannel());
        channel.setDecentralized(channelDTO.isDecentralized());
        return channelRepository.save(channel);
    }

    @Override
    public Channel updateChannel(Long channelId, ChannelDTO channelDTO) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new EntityNotFoundException("Channel not found"));

        channel.setName(channelDTO.getName());
        channel.setTitle(channelDTO.getTitle());
        channel.setDescription(channelDTO.getDescription());
        channel.setIconHash(channelDTO.getIconHash());
        return channelRepository.save(channel);
    }

    @Override
    public void deleteChannel(Long channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new EntityNotFoundException("Channel not found"));

        // Optionally handle channel-user associations
        channelUserRepository.deleteByChannelId(channelId);

        channelRepository.delete(channel);
    }

    @Override
    public Optional<Channel> getChannelById(Long channelId) {
        return channelRepository.findById(channelId);
    }

    @Override
    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }

    @Transactional
    @Override
    public void addUserToChannel(Long channelId, Long userId, Permission... permissions) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new EntityNotFoundException("Channel not found"));
        Account user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ChannelUser channelUser = new ChannelUser();
        channelUser.setChannel(channel);
        channelUser.setUser(user);
        channelUser.setJoinedAt(LocalDateTime.now());
        if (permissions.length == 0) {
            channelUser.setPermissions(Collections.emptySet());
        } else {
            channelUser.setPermissions(Arrays.stream(permissions).collect(Collectors.toSet()));
        }
        channelUserRepository.save(channelUser);
    }

    @Override
    @Transactional
    public void removeUserFromChannel(Long channelId, Long userId) {
        ChannelUser channelUser = channelUserRepository.findByChannelIdAndUserId(channelId, userId);
        if (channelUser != null) {
            channelUserRepository.delete(channelUser);
            Channel channel = channelUser.getChannel();
            if (channelUserRepository.findByChannelId(channelId).isEmpty() && channel.isDecentralized()) {
                // 全部人都退出了,自动解散
                // 非去中心化群组在重新加入后可用
                this.disbandChannel(channelId);
            }
        }
    }

    @Override
    public List<Account> getUsersInChannel(Long channelId) {
        List<ChannelUser> channelUsers = channelUserRepository.findByChannelId(channelId);
        return channelUsers.stream().map(ChannelUser::getUser).toList();
    }

    @Override
    public boolean hasViewPermission(Account account, long channelId) {
        if (account.getPermissions().contains(Permission.MANAGE_CHANNEL)) return true; // admin
        if (!account.getPermissions().contains(Permission.VIEW_CHANNEL)) return false;
        ChannelUser channelUser = channelUserRepository.findByChannelIdAndUserId(channelId, account.getId());
        return channelUser != null;
    }

    @Override
    public List<ChannelUser> findChannelUsers(Account account) {
        return channelUserRepository.findByUserId(account.getId());
    }

    @Override
    public boolean disbandChannel(Long channelId) {
        if (!channelUserRepository.existsById(channelId)) return false;
        chatMessageService.deleteAllByChannel(channelId); // 清理数据库
        channelUserRepository.deleteByChannelId(channelId); // 清理成员
        channelRepository.deleteById(channelId); // 删除群组
        return true;
    }
}

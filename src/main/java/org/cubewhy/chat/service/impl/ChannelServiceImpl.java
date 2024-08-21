package org.cubewhy.chat.service.impl;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.cubewhy.chat.entity.*;
import org.cubewhy.chat.entity.dto.ChannelDTO;
import org.cubewhy.chat.entity.dto.ChannelJoinRequestDTO;
import org.cubewhy.chat.entity.dto.FriendRequestDTO;
import org.cubewhy.chat.repository.*;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.ChannelService;
import org.cubewhy.chat.service.ChatMessageService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChannelServiceImpl implements ChannelService {
    @Resource
    ChannelRepository channelRepository;

    @Resource
    AccountRepository userRepository;

    @Resource
    ChannelUserRepository channelUserRepository;

    @Resource
    ChannelJoinRequestRepository channelJoinRequestRepository;

    @Resource
    FriendRequestRepository friendRequestRepository;

    @Resource
    ChatMessageService chatMessageService;

    @Resource
    @Lazy
    AccountService accountService;

    @Override
    public boolean existByName(String name) {
        return channelRepository.existsByName(name);
    }

    @Override
    public Channel createChannel(ChannelDTO channelDTO) {
        Optional<Channel> existChannel = channelRepository.findByName(channelDTO.getName());
        if (existChannel.isPresent()) return existChannel.get();
        if (accountService.existByUsername(channelDTO.getName())) return null;
        Channel channel = new Channel();
        if (channelDTO.getName() != null) {
            channel.setName(channelDTO.getName());
        } else {
            channel.setName("generated_" + System.currentTimeMillis());
        }
        channel.setTitle(channelDTO.getTitle());
        channel.setDescription(channelDTO.getDescription());
        channel.setIconHash(channelDTO.getIconHash());
        channel.setPublicChannel(channelDTO.isPublicChannel());
        channel.setDecentralized(channelDTO.isDecentralized());
        return channelRepository.save(channel);
    }

    @Override
    public Channel createChannel(Channel channel) {
        if (accountService.existByUsername(channel.getName())) return null;
        Optional<Channel> existChannel = channelRepository.findByName(channel.getName());
        return existChannel.orElseGet(() -> channelRepository.save(channel));
    }

    @Override
    public Channel updateChannel(Long channelId, ChannelDTO channelDTO) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new EntityNotFoundException("Channel not found"));

        channel.setName(channelDTO.getName());
        channel.setTitle(channelDTO.getTitle());
        channel.setDescription(channelDTO.getDescription());
        channel.setIconHash(channelDTO.getIconHash());
        return channelRepository.save(channel);
    }

    @Override
    public void deleteChannel(Long channelId) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new EntityNotFoundException("Channel not found"));

        // Optionally handle channel-user associations
        channelUserRepository.deleteByChannelId(channelId);

        channelRepository.delete(channel);
    }

    @Override
    public Channel findChannelById(Long channelId) {
        return channelRepository.findById(channelId).orElse(null);
    }

    @Override
    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }

    @Transactional
    @Override
    public ChannelUser addUserToChannel(Long channelId, Long userId, Permission... permissions) {
        Channel channel = channelRepository.findById(channelId).orElseThrow(() -> new EntityNotFoundException("Channel not found"));
        Account user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        return addUserToChannel(channel, user, permissions);
    }

    @Transactional
    @Override
    public ChannelUser addUserToChannel(Channel channel, Account user, Permission... permissions) {
        ChannelUser exist = channelUserRepository.findByChannelIdAndUserId(channel.getId(), user.getId());
        if (exist != null) return exist;
        ChannelUser channelUser = new ChannelUser();
        channelUser.setChannel(channel);
        channelUser.setUser(user);
        channelUser.setJoinedAt(LocalDateTime.now());
        if (permissions.length == 0) {
            channelUser.setPermissions(new HashSet<>(Arrays.asList(Permission.SEND_MESSAGE, Permission.VIEW_CHANNEL, Permission.UPLOAD_FILES, Permission.DOWNLOAD_FILES)));
        } else {
            channelUser.setPermissions(Arrays.stream(permissions).filter(p -> (p.getType().equals(Permission.Type.CHANNEL) || p.getType().equals(Permission.Type.CHANNEL_AND_SERVLET))).collect(Collectors.toSet()));
        }
        return channelUserRepository.save(channelUser);
    }

    @Override
    @Transactional
    public void removeUserFromChannel(Long channelId, Long userId) {
        ChannelUser channelUser = channelUserRepository.findByChannelIdAndUserId(channelId, userId);
        if (channelUser != null) {
            channelUserRepository.delete(channelUser);
            Channel channel = channelUser.getChannel();
            if (channelUserRepository.findByChannelId(channelId).size() <= 1 && channel.isDecentralized()) {
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
    @Transactional
    public boolean disbandChannel(Long channelId) {
        if (!channelUserRepository.existsById(channelId)) return false;
        chatMessageService.deleteAllByChannelId(channelId); // 清理数据库
        channelUserRepository.deleteByChannelId(channelId); // 清理成员
        channelRepository.deleteById(channelId); // 删除群组
        return true;
    }

    @Override
    @Transactional
    public boolean disbandChannel(Channel channel) {
        chatMessageService.deleteAllByChannelId(channel.getId());
        channelUserRepository.deleteByChannelId(channel.getId());
        channelRepository.delete(channel); // 删除群组
        return true;
    }

    @Override
    @Transactional
    public boolean approveJoinRequest(Long requestId) {
        Optional<ChannelJoinRequest> joinRequest = channelJoinRequestRepository.findById(requestId);
        if (joinRequest.isEmpty()) return false;
        ChannelJoinRequest request = joinRequest.get();
        return approveJoinRequest(request);
    }

    @Transactional
    @Override
    public boolean approveJoinRequest(ChannelJoinRequest request) {
        Long channelId = request.getChannelId();
        Channel channel = this.findChannelById(channelId);
        if (channel == null) return false;
        this.addUserToChannel(channel, accountService.findAccountById(request.getUserId()), Permission.SEND_MESSAGE, Permission.VIEW_CHANNEL);
        channelJoinRequestRepository.delete(request);
        return true;
    }

    @Override
    public boolean rejectJoinRequest(Long requestId) {
        Optional<ChannelJoinRequest> joinRequest = channelJoinRequestRepository.findById(requestId);
        return joinRequest.filter(this::rejectJoinRequest).isPresent();
    }

    @Override
    public boolean rejectJoinRequest(ChannelJoinRequest joinRequest) {
        channelJoinRequestRepository.delete(joinRequest);
        return true;
    }

    @Override
    @Transactional
    public Channel approveFriendRequest(Long requestId) {
        Optional<FriendRequest> friendRequest = friendRequestRepository.findById(requestId);
        return friendRequest.map(this::approveFriendRequest).orElse(null);
    }

    @Transactional
    @Override
    public Channel approveFriendRequest(FriendRequest request) {
        long from = request.getFrom();
        long to = request.getTo();
        Channel channel = createChannel(ChannelDTO.builder().name("friend_" + from + "_" + to).description("Private channel between " + from + " and " + to).publicChannel(false).decentralized(true).build());
        Account fromUser = accountService.findAccountById(from);
        Account toUser = accountService.findAccountById(to);
        addUserToChannel(channel, fromUser, Permission.SEND_MESSAGE, Permission.VIEW_CHANNEL);
        addUserToChannel(channel, toUser, Permission.SEND_MESSAGE, Permission.VIEW_CHANNEL);
        return channel;
    }

    @Override
    public boolean rejectFriendRequest(Long requestId) {
        Optional<FriendRequest> friendRequest = friendRequestRepository.findById(requestId);
        return friendRequest.filter(this::rejectFriendRequest).isPresent();
    }

    @Override
    public boolean rejectFriendRequest(FriendRequest request) {
        friendRequestRepository.delete(request);
        return true;
    }

    @Override
    public ChannelJoinRequest findJoinRequestById(long id) {
        return channelJoinRequestRepository.findById(id).orElse(null);
    }

    @Override
    public List<ChannelJoinRequest> findAllJoinRequests() {
        return channelJoinRequestRepository.findAll();
    }

    @Override
    public List<ChannelJoinRequest> findJoinRequestsByAccount(Account account) {
        List<Channel> channels = accountService.findManagedChannels(account);
        List<ChannelJoinRequest> result = new ArrayList<>();
        for (Channel channel : channels) {
            result.addAll(findJoinRequestByChannel(channel));
        }
        return result;
    }

    @Override
    public ChannelJoinRequest createJoinRequest(ChannelJoinRequestDTO joinRequest, Account account) {
        ChannelJoinRequest request = new ChannelJoinRequest();
        request.setChannelId(joinRequest.getChannelId());
        request.setUserId(account.getId());
        request.setReason(joinRequest.getReason());
        return channelJoinRequestRepository.save(request);
    }

    @Override
    public FriendRequest createFriendRequest(FriendRequestDTO friendRequest, Account account) {
        FriendRequest request = new FriendRequest();
        request.setFrom(account.getId());
        request.setTo(friendRequest.getTo());
        request.setReason(friendRequest.getReason());
        return friendRequestRepository.save(request);
    }

    @Override
    public List<ChannelJoinRequest> findJoinRequestByChannel(Channel channel) {
        return channelJoinRequestRepository.findALlByChannelId(channel.getId());
    }

    @Override
    public boolean checkPermissions(Account account, Channel channel, Permission... permissions) {
        ChannelUser cu = channelUserRepository.findByChannelIdAndUserId(channel.getId(), account.getId());
        if (cu == null) return false; // not in channel
        return cu.getPermissions().containsAll(Arrays.asList(permissions));
    }

    @Override
    public FriendRequest findFriendRequestById(long requestId) {
        return friendRequestRepository.findById(requestId).orElse(null);
    }

    @Override
    public boolean hasName(String name) {
        return channelRepository.existsByName(name);
    }

    @Override
    public Channel findChannelByName(String name) {
        return channelRepository.findByName(name).orElse(null);
    }
}

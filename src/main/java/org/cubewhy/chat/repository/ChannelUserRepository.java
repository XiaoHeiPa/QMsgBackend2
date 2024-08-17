package org.cubewhy.chat.repository;

import org.cubewhy.chat.entity.ChannelUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelUserRepository extends JpaRepository<ChannelUser, Long> {
    ChannelUser findByChannelIdAndUserId(Long channelId, Long userId);

    List<ChannelUser> findByChannelId(Long channelId);

    List<ChannelUser> findByUserId(Long userId);

    void deleteByChannelId(Long channelId);
}

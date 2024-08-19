package org.cubewhy.chat.repository;

import org.cubewhy.chat.entity.ChannelJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelJoinRequestRepository extends JpaRepository<ChannelJoinRequest, Long> {
    List<ChannelJoinRequest> findALlByChannelId(Long id);
}

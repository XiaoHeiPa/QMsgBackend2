package org.cubewhy.chat.repository;

import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.UserUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserUploadRepository extends JpaRepository<UserUpload, Long> {
    Optional<UserUpload> findByName(String name);

    Optional<UserUpload> findByHash(String hash);

    List<UserUpload> findByUploadUser(Account account);
}

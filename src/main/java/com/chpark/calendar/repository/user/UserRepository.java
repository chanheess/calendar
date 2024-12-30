package com.chpark.calendar.repository.user;

import com.chpark.calendar.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u.nickname FROM UserEntity u WHERE u.id = :userId")
    Optional<String> findNicknameById(@Param("userId") long userId);

    @Query("SELECT u.id FROM UserEntity u WHERE u.nickname = :nickname")
    Optional<Long> findIdByNickname(@Param("nickname") String nickname);

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u " +
            "SET u.email = COALESCE(:#{#userInfo.email}, u.email), " +
            "u.nickname = COALESCE(:#{#userInfo.nickname}, u.nickname) " +
            "WHERE u.id = :userId")
    int updateUserInfo(@Param("userId") long userId, @Param("userInfo") UserEntity userInfo);

}

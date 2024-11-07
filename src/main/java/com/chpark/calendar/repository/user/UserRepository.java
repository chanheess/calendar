package com.chpark.calendar.repository.user;

import com.chpark.calendar.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u.nickname FROM UserEntity u WHERE u.id = :userId")
    Optional<String> findNicknameById(@Param("userId") int userId);

    @Query("SELECT u.nickname FROM UserEntity u WHERE u.id = :userId")
    Optional<String> findPasswordById(@Param("userId") int userId);

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u " +
            "SET u.email = COALESCE(:#{#userInfo.email}, u.email), " +
            "u.nickname = COALESCE(:#{#userInfo.nickname}, u.nickname) " +
            "WHERE u.id = :userId")
    int updateUserInfo(@Param("userId") int userId, @Param("userInfo") UserEntity userInfo);

}

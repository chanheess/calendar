package com.chpark.chcalendar.repository;

import com.chpark.chcalendar.entity.FirebaseTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FirebaseTokenRepository extends JpaRepository<FirebaseTokenEntity, Long> {

    List<FirebaseTokenEntity> findByUserId(long userId);
    void deleteByUserIdAndToken(long userId, String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM FirebaseTokenEntity f WHERE f.token = :token")
    int deleteByToken(@Param("token") String token);

    Optional<FirebaseTokenEntity> findByUserIdAndToken(long userId, String token);
    Optional<FirebaseTokenEntity> findByUserIdAndPlatform(long userId, String platform);

}

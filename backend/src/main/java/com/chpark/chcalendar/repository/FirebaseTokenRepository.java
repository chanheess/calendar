package com.chpark.chcalendar.repository;

import com.chpark.chcalendar.entity.FirebaseTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FirebaseTokenRepository extends JpaRepository<FirebaseTokenEntity, Long> {

    List<FirebaseTokenEntity> findByUserId(long userId);
    void deleteByUserIdAndToken(long userId, String token);

}

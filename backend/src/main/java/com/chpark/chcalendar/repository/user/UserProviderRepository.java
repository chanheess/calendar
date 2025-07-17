package com.chpark.chcalendar.repository.user;

import com.chpark.chcalendar.entity.UserProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProviderRepository extends JpaRepository<UserProviderEntity, Long> {

    void deleteByUserId(long userId);

    List<UserProviderEntity> findByUserId(long userId);
}

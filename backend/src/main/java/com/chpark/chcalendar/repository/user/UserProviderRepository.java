package com.chpark.chcalendar.repository.user;

import com.chpark.chcalendar.entity.UserProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProviderRepository extends JpaRepository<UserProviderEntity, Long> {

    void deleteByUserId(long userId);
}

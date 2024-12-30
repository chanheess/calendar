package com.chpark.chcalendar.repository;

import com.chpark.chcalendar.entity.GroupUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupUserEntity, Long> {

    List<GroupUserEntity> findByUserId(long userId);

    Optional<GroupUserEntity> findByGroupIdAndUserId(long groupId, long userId);

}

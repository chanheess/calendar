package com.chpark.calendar.repository.group;

import com.chpark.calendar.entity.group.GroupUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupUserEntity, Long> {

    List<GroupUserEntity> findByUserId(long userId);

    Optional<GroupUserEntity> findByGroupIdAndUserId(long groupId, long userId);

}

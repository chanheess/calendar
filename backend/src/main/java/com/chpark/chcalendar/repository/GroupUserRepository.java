package com.chpark.chcalendar.repository;

import com.chpark.chcalendar.entity.GroupUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupUserRepository extends JpaRepository<GroupUserEntity, Long> {

    List<GroupUserEntity> findByUserId(long userId);

    @Query("SELECT g.groupId FROM GroupUserEntity g WHERE g.userId = :userId")
    List<Long> findGroupIdByUserId(@Param("userId") Long userId);

    @Query("SELECT g.userId FROM GroupUserEntity g WHERE g.groupId = :groupId")
    List<Long> findUserIdByGroupId(long groupId);

    Optional<GroupUserEntity> findByUserIdAndGroupId(long userId, long groupId);
    
    List<GroupUserEntity> findByGroupId(long groupId);


}

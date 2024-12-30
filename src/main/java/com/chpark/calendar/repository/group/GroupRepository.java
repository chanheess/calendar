package com.chpark.calendar.repository.group;

import com.chpark.calendar.entity.group.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {

    @Query("SELECT COUNT(s) FROM GroupEntity s WHERE s.adminId = :userId")
    Integer countAdminGroups(@Param("userId")Long userId);

    List<GroupEntity> findByTitle(String title);


}

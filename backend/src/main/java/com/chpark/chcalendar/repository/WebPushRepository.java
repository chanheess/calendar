package com.chpark.chcalendar.repository;

import com.chpark.chcalendar.entity.WebPushEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebPushRepository extends JpaRepository<WebPushEntity, Long> {

    Optional<WebPushEntity> findByEndpoint(String endpoint);

}

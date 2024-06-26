package com.chpark.calendar.repository;

import com.chpark.calendar.domain.Schedule;
import jakarta.persistence.EntityManager;

import java.util.List;

public class RegistrationScheduleRepository implements ScheduleRepository{

    private final EntityManager em;

    public RegistrationScheduleRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Schedule createSchedule(Schedule schedule) {
        em.persist(schedule);
        return schedule;
    }

    @Override
    public List<Schedule> findAll() {
        return em.createQuery("select m from Schedule m", Schedule.class)
                .getResultList();
    }
}

package com.ambulancia.plinio.repository;

import com.ambulancia.plinio.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    @Query("SELECT h FROM Hospital h JOIN FETCH h.address WHERE h.Id = :id")
    Optional<Hospital> findByIdFetchingAddress(@Param("id") long id);
}

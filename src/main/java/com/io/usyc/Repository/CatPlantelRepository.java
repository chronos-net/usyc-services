package com.io.usyc.Repository;

import com.io.usyc.Domain.CatPlantel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatPlantelRepository extends JpaRepository<CatPlantel, Integer> {
    boolean existsByCodeIgnoreCase(String code);
    List<CatPlantel> findAllByActiveTrueOrderByNameAsc();
    List<CatPlantel> findAllByOrderByNameAsc();
}

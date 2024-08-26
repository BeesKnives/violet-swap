package com.launchcode.violetSwap.models.data;

import com.launchcode.violetSwap.models.Variety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VarietyRepository extends JpaRepository<Variety, Integer> {
    Variety findByName(String name);
}

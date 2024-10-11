package com.launchcode.violetSwap.models.data;

import com.launchcode.violetSwap.models.User;
import com.launchcode.violetSwap.models.Variety;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VarietyRepository extends JpaRepository<Variety, Integer> {
    Variety findByName(String name);

    //select all from Variety table where the name contains the searchterm/queryFragment

//    @Query(value = "SELECT * FROM variety WHERE name LIKE '%ROB%'", nativeQuery = true)
//    public List<Variety> searchVarietyByName(String queryFragment);

//    @Query(value = "SELECT * FROM variety WHERE name LIKE ?1", nativeQuery = true)
//    public List<Variety> searchVarietyByName(String queryFragment);

    @Query(value = "SELECT * FROM variety WHERE name LIKE :search", nativeQuery = true)
    public List<Variety> searchVarietyByName(@Param("search") String queryFragment);

    //todo: try this:
    //    @Query(value = "SELECT * FROM variety WHERE name LIKE :search", nativeQuery = true)
    //    public List<Variety> searchVarietyByName(@Param("search") String queryFragment);

    List<Variety> findByNameIgnoreCaseContaining(String name);

//    @Query(value = "SELECT * FROM variety WHERE name LIKE :search", nativeQuery = true)
//    public List<Variety> searchVarietyByName();


}

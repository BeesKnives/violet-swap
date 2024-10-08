package com.launchcode.violetSwap.models.data;

import com.launchcode.violetSwap.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {


    User findByUsername(String username);

//    @Query
//    User findBySearch(List<String> list);
    //todo: find tutorial showing how this works, examples needed
    //todo: findBySearch gets the query(?) ias a string param, uses it to get users from repository
    //todo: change from CRUD Repository to JPA repository?

    //todo:
    //https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods-details.html#repositories.query-methods.query-creation
    //https://www.baeldung.com/the-persistence-layer-with-spring-data-jpa

}

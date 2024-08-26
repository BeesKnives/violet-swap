package com.launchcode.violetSwap.models.data;

import com.launchcode.violetSwap.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    User findByUsername(String username);

    //todo:
    //https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods-details.html#repositories.query-methods.query-creation
    //https://www.baeldung.com/the-persistence-layer-with-spring-data-jpa

}

package dev.luishidalgoa.springsection7.repository;

import dev.luishidalgoa.springsection7.model.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {

    Optional<dev.luishidalgoa.springsection7.model.Customer> findByEmail(String email);
}

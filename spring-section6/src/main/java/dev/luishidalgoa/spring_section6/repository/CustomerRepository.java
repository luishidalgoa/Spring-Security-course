package dev.luishidalgoa.spring_section6.repository;

import dev.luishidalgoa.spring_section6.model.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {

    Optional<dev.luishidalgoa.spring_section6.model.Customer> findByEmail(String email);
}

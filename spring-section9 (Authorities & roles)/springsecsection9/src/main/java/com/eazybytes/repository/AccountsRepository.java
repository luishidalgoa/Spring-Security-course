package com.eazybytes.repository;

import com.eazybytes.model.Accounts;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountsRepository extends CrudRepository<Accounts, Long> {

    Accounts findByCustomerId(long customerId);
    @Query("select a from Accounts a join Customer c on a.customerId = c.id where c.email = ?1")
    Accounts findByMail(String mail);

}

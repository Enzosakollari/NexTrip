package com.example.demo.User;

import com.example.demo.User.BusinessUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BusinessUserRepository extends JpaRepository<BusinessUser, Long> {
    Optional<BusinessUser> findByUsername(String username);
    BusinessUser findByEmail(String email);
}

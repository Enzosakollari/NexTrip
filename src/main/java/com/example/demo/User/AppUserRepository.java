package com.example.demo.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
/*Declaring AppUser as the entity type and Long as the primary key type
This repository is responsible for CRUD operations on AppUser entity
The JpaRepository interface provides methods for basic CRUD operations
This makes possible to retrive data and save data in the database
we can get methods like findById,findAll or save methods we just need to declare them here */
public interface AppUserRepository extends JpaRepository<AppUser,Long> {

    Optional<AppUser> findByUsername(String username);
    AppUser findByEmail(String email);

}
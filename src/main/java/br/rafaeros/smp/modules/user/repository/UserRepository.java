package br.rafaeros.smp.modules.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.rafaeros.smp.modules.user.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE " +
            "(:firstName IS NULL OR LOWER(CAST(u.firstName AS string)) LIKE LOWER(CAST(:firstName AS string))) AND " +
            "(:lastName IS NULL OR LOWER(CAST(u.lastName AS string)) LIKE LOWER(CAST(:lastName AS string))) AND " +
            "(:username IS NULL OR LOWER(CAST(u.username AS string)) LIKE LOWER(CAST(:username AS string))) AND " +
            "(:email IS NULL OR LOWER(CAST(u.email AS string)) LIKE LOWER(CAST(:email AS string)))")
    Page<User> findByFilters(Pageable pageable, @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("username") String username, @Param("email") String email);

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.sector s LEFT JOIN FETCH s.authorities WHERE u.username = :username")
    Optional<User> findByUsernameWithSector(@Param("username") String username);

    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);
}

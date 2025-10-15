package br.com.garage_management.repository;

import br.com.garage_management.domain.model.GarageSector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarageSectorRepository extends JpaRepository<GarageSector, String> {

}
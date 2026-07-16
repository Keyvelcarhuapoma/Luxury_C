package com.example.luxury.dominios.sede.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.luxury.dominios.sede.model.Sede;

public interface SedeRepository extends JpaRepository<Sede, Long> {

	@Override
	@Query("SELECT s FROM Sede s ORDER BY s.id DESC")
	List<Sede> findAll();
}

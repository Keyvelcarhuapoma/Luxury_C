package com.example.luxury.dominios.consumo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.luxury.dominios.consumo.model.Consumo;

public interface ConsumoRepository extends JpaRepository<Consumo, Long> {

	@Query("SELECT c FROM Consumo c WHERE c.sede.id = :sedeId ORDER BY c.id DESC")
	List<Consumo> findBySedeId(@Param("sedeId") Long sedeId);

	@Query("SELECT c FROM Consumo c WHERE c.periodo = :periodo ORDER BY c.id DESC")
	List<Consumo> findByPeriodo(@Param("periodo") String periodo);

	@Override
	@Query("SELECT c FROM Consumo c ORDER BY c.id DESC")
	List<Consumo> findAll();

	Page<Consumo> findAll(Pageable pageable);
}

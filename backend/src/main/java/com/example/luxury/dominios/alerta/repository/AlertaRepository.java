package com.example.luxury.dominios.alerta.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.luxury.dominios.alerta.model.Alerta;
import com.example.luxury.dominios.common.enums.NivelAlerta;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

	@Query("SELECT a FROM Alerta a WHERE a.consumo.sede.id = :sedeId ORDER BY a.id DESC")
	List<Alerta> findByConsumoSedeId(@Param("sedeId") Long sedeId);

	@Override
	@Query("SELECT a FROM Alerta a ORDER BY a.id DESC")
	List<Alerta> findAll();

	long countByNivel(NivelAlerta nivel);
}

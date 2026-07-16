package com.example.luxury.dominios.auditoria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.luxury.dominios.auditoria.model.Auditoria;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

	@Query("SELECT a FROM Auditoria a WHERE a.usuario.id = :usuarioId ORDER BY a.id DESC")
	List<Auditoria> findByUsuarioId(@Param("usuarioId") Long usuarioId);

	@Query("SELECT a FROM Auditoria a WHERE LOWER(a.modulo) = LOWER(:modulo) ORDER BY a.id DESC")
	List<Auditoria> findByModuloIgnoreCase(@Param("modulo") String modulo);

	@Override
	@Query("SELECT a FROM Auditoria a ORDER BY a.id DESC")
	List<Auditoria> findAll();
}

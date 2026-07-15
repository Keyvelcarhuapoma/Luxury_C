package com.example.luxury.dominios.auditoria.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.luxury.dominios.auditoria.dto.AuditoriaResponse;
import com.example.luxury.dominios.auditoria.model.Auditoria;
import com.example.luxury.dominios.auditoria.repository.AuditoriaRepository;
import com.example.luxury.dominios.seguridad.models.Usuario;

@Service
public class AuditoriaService {

	@Autowired
	private AuditoriaRepository auditoriaRepository;

	public void registrar(Usuario usuario, String modulo, String accion, String tabla, Long registroId, String descripcion) {
		Auditoria auditoria = new Auditoria();
		auditoria.setUsuario(usuario);
		auditoria.setModulo(modulo);
		auditoria.setAccion(accion);
		auditoria.setTablaAfectada(tabla);
		auditoria.setIdRegistroAfectado(registroId);
		auditoria.setDescripcion(descripcion);
		auditoriaRepository.save(auditoria);
	}

	public List<AuditoriaResponse> listar() {
		List<AuditoriaResponse> lista = new ArrayList<>();
		for (Auditoria a : auditoriaRepository.findAll()) {
			lista.add(AuditoriaResponse.from(a));
		}
		return lista;
	}

	public List<AuditoriaResponse> listarPorUsuario(Long usuarioId) {
		List<AuditoriaResponse> lista = new ArrayList<>();
		for (Auditoria a : auditoriaRepository.findByUsuarioId(usuarioId)) {
			lista.add(AuditoriaResponse.from(a));
		}
		return lista;
	}

	public List<AuditoriaResponse> listarPorModulo(String modulo) {
		List<AuditoriaResponse> lista = new ArrayList<>();
		for (Auditoria a : auditoriaRepository.findByModuloIgnoreCase(modulo)) {
			lista.add(AuditoriaResponse.from(a));
		}
		return lista;
	}
}

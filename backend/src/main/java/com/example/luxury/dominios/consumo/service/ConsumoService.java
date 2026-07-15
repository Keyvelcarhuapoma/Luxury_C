package com.example.luxury.dominios.consumo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.luxury.dominios.alerta.service.ReglasAlertasService;
import com.example.luxury.dominios.auditoria.service.AuditoriaService;
import com.example.luxury.dominios.common.exception.ResourceNotFoundException;
import com.example.luxury.dominios.consumo.dto.ConsumoRequest;
import com.example.luxury.dominios.consumo.dto.ConsumoResponse;
import com.example.luxury.dominios.consumo.model.Consumo;
import com.example.luxury.dominios.consumo.repository.ConsumoRepository;
import com.example.luxury.dominios.finanzas.model.ConsumoCosto;
import com.example.luxury.dominios.finanzas.service.ConversionFinancieraService;
import com.example.luxury.dominios.recurso.service.TipoRecursoService;
import com.example.luxury.dominios.seguridad.enums.NombreRol;
import com.example.luxury.dominios.seguridad.models.Rol;
import com.example.luxury.dominios.seguridad.services.AuthenticatedUserService;
import com.example.luxury.dominios.sede.service.SedeService;
import com.example.luxury.dominios.tarifa.model.TarifaRecurso;
import com.example.luxury.dominios.tarifa.service.TarifaService;
import com.example.luxury.dominios.seguridad.models.Usuario;

@Service
public class ConsumoService {

	@Autowired
	private ConsumoRepository consumoRepository;

	@Autowired
	private SedeService sedeService;

	@Autowired
	private TipoRecursoService tipoRecursoService;

	@Autowired
	private TarifaService tarifaService;

	@Autowired
	private ConversionFinancieraService conversionFinancieraService;

	@Autowired
	private ReglasAlertasService reglasAlertasService;

	@Autowired
	private AuditoriaService auditoriaService;

	@Autowired
	private AuthenticatedUserService authenticatedUserService;

	public ConsumoResponse registrar(ConsumoRequest request) {
		Usuario usuario = authenticatedUserService.actual();
		validarSedePermitida(usuario, request.getSedeId());
		TarifaRecurso tarifa = tarifaService.buscarVigente(request.getSedeId(), request.getTipoRecursoId(), request.getFechaConsumo());
		Consumo consumo = new Consumo();
		consumo.setSede(sedeService.buscar(request.getSedeId()));
		consumo.setTipoRecurso(tipoRecursoService.buscar(request.getTipoRecursoId()));
		consumo.setTarifa(tarifa);
		consumo.setUsuarioRegistro(usuario);
		consumo.setCantidadConsumida(request.getCantidadConsumida());
		consumo.setFechaConsumo(request.getFechaConsumo());
		consumo.setPeriodo(request.getPeriodo());
		Consumo saved = consumoRepository.save(consumo);
		BigDecimal costoPen = request.getCantidadConsumida()
				.multiply(tarifa.getPrecioUnitarioPen())
				.setScale(4, RoundingMode.HALF_UP);
		List<ConsumoCosto> costos = conversionFinancieraService.calcularYGuardarCostos(saved, costoPen);
		reglasAlertasService.evaluarYGenerar(saved, costoPen);
		auditoriaService.registrar(usuario, "CONSUMOS", "CREAR", "consumos", saved.getId(),
				"Registro de consumo para sede " + saved.getSede().getNombre());
		return ConsumoResponse.from(saved, costos);
	}

	public List<ConsumoResponse> listar() {
		Usuario usuario = authenticatedUserService.actual();
		List<ConsumoResponse> lista = new ArrayList<>();
		if (puedeGestionarTodasLasSedes(usuario)) {
			for (Consumo c : consumoRepository.findAll()) {
				lista.add(toResponse(c));
			}
			return lista;
		}
		if (usuario.getSede() == null) {
			return List.of();
		}
		for (Consumo c : consumoRepository.findBySedeId(usuario.getSede().getId())) {
			lista.add(toResponse(c));
		}
		return lista;
	}

	public Page<ConsumoResponse> listarPaginado(Pageable pageable) {
		Page<Consumo> page = consumoRepository.findAll(pageable);
		List<ConsumoResponse> lista = new ArrayList<>();
		for (Consumo c : page.getContent()) {
			lista.add(toResponse(c));
		}
		return new PageImpl<>(lista, pageable, page.getTotalElements());
	}

	public ConsumoResponse obtener(Long id) {
		return toResponse(buscar(id));
	}

	public List<ConsumoResponse> listarPorSede(Long sedeId) {
		validarSedePermitida(authenticatedUserService.actual(), sedeId);
		List<ConsumoResponse> lista = new ArrayList<>();
		for (Consumo c : consumoRepository.findBySedeId(sedeId)) {
			lista.add(toResponse(c));
		}
		return lista;
	}

	public List<ConsumoResponse> listarPorPeriodo(String periodo) {
		Usuario usuario = authenticatedUserService.actual();
		List<ConsumoResponse> lista = new ArrayList<>();
		for (Consumo consumo : consumoRepository.findByPeriodo(periodo)) {
			if (puedeGestionarTodasLasSedes(usuario)
					|| (usuario.getSede() != null && consumo.getSede().getId().equals(usuario.getSede().getId()))) {
				lista.add(toResponse(consumo));
			}
		}
		return lista;
	}

	private Consumo buscar(Long id) {
		Optional<Consumo> consumoOptional = consumoRepository.findById(id);
		if (consumoOptional.isPresent()) {
			return consumoOptional.get();
		}
		throw new ResourceNotFoundException("Consumo no encontrado");
	}

	private ConsumoResponse toResponse(Consumo consumo) {
		return ConsumoResponse.from(consumo, conversionFinancieraService.listarCostos(consumo.getId()));
	}

	private void validarSedePermitida(Usuario usuario, Long sedeId) {
		if (puedeGestionarTodasLasSedes(usuario)) {
			return;
		}
		if (usuario.getSede() == null) {
			throw new IllegalArgumentException("Tu usuario no tiene una sede asignada.");
		}
		if (!usuario.getSede().getId().equals(sedeId)) {
			throw new IllegalArgumentException("No puedes registrar ni consultar consumos de otra sede.");
		}
	}

	private boolean puedeGestionarTodasLasSedes(Usuario usuario) {
		for (Rol rol : usuario.getRoles()) {
			if (rol.getNombre() == NombreRol.ADMIN) {
				return true;
			}
		}
		return false;
	}
}

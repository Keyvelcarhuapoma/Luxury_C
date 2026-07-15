package com.example.luxury.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import com.example.luxury.api.dto.ConsumoApiResponse;
import com.example.luxury.dominios.common.enums.EstadoRegistro;
import com.example.luxury.dominios.consumo.dto.ConsumoRequest;
import com.example.luxury.dominios.consumo.dto.ConsumoResponse;
import com.example.luxury.dominios.consumo.service.ConsumoService;
import com.example.luxury.dominios.recurso.dto.TipoRecursoRequest;
import com.example.luxury.dominios.recurso.dto.TipoRecursoResponse;
import com.example.luxury.dominios.recurso.service.TipoRecursoService;
import com.example.luxury.dominios.seguridad.enums.NombreRol;
import com.example.luxury.dominios.seguridad.models.Rol;
import com.example.luxury.dominios.seguridad.models.Usuario;
import com.example.luxury.dominios.seguridad.services.AuthenticatedUserService;
import com.example.luxury.dominios.sede.dto.SedeRequest;
import com.example.luxury.dominios.sede.dto.SedeResponse;
import com.example.luxury.dominios.sede.service.SedeService;

@RestController
@RequestMapping("/api")
public class ApiResourcesController {

    private final SedeService sedeService;
    private final TipoRecursoService tipoRecursoService;
    private final ConsumoService consumoService;
    private final AuthenticatedUserService authenticatedUserService;

    public ApiResourcesController(SedeService sedeService, TipoRecursoService tipoRecursoService,
            ConsumoService consumoService, AuthenticatedUserService authenticatedUserService) {
        this.sedeService = sedeService;
        this.tipoRecursoService = tipoRecursoService;
        this.consumoService = consumoService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping("/sedes")
    public List<Map<String, Object>> listarSedes() {
        Usuario usuario = authenticatedUserService.actual();
        List<SedeResponse> sedes = sedeService.listar();
        List<Map<String, Object>> lista = new ArrayList<>();
        if (puedeGestionarTodasLasSedes(usuario)) {
            for (SedeResponse s : sedes) {
                lista.add(ApiMapper.sede(s));
            }
            return lista;
        }
        if (usuario.getSede() == null) {
            return List.of();
        }
        for (SedeResponse s : sedes) {
            if (s.getId().equals(usuario.getSede().getId())) {
                lista.add(ApiMapper.sede(s));
            }
        }
        return lista;
    }

    @PostMapping("/sedes")
    public Map<String, Object> crearSede(@Valid @RequestBody SedeApiRequest request) {
        SedeRequest sedeRequest = new SedeRequest(request.nombre(), request.ciudad(), request.direccion(), EstadoRegistro.ACTIVO);
        return ApiMapper.sede(sedeService.crear(sedeRequest));
    }

    @DeleteMapping("/sedes/{id}")
    public void eliminarSede(@PathVariable Long id) {
        sedeService.eliminar(id);
    }

    @GetMapping("/tipos-recurso")
    public List<Map<String, Object>> listarTiposRecurso() {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (TipoRecursoResponse t : tipoRecursoService.listar()) {
            lista.add(ApiMapper.tipoRecurso(t));
        }
        return lista;
    }

    @PostMapping("/tipos-recurso")
    public Map<String, Object> crearTipoRecurso(@Valid @RequestBody TipoRecursoApiRequest request) {
        TipoRecursoRequest tipoRequest = new TipoRecursoRequest(request.nombre(), request.unidad());
        return ApiMapper.tipoRecurso(tipoRecursoService.crear(tipoRequest));
    }

    @GetMapping("/consumos")
    public List<ConsumoApiResponse> listarConsumos() {
        List<ConsumoApiResponse> lista = new ArrayList<>();
        for (ConsumoResponse c : consumoService.listar()) {
            lista.add(ApiMapper.consumoDto(c));
        }
        return lista;
    }

    @GetMapping("/consumos/paginado")
    public Map<String, Object> listarConsumosPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        Page<?> paginado = consumoService.listarPaginado(PageRequest.of(safePage, safeSize, Sort.by(dir, sort)));
        List<ConsumoApiResponse> contentList = new ArrayList<>();
        for (Object c : paginado.getContent()) {
            contentList.add(ApiMapper.consumoDto((ConsumoResponse) c));
        }
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("content", contentList);
        data.put("page", paginado.getNumber());
        data.put("size", paginado.getSize());
        data.put("totalElements", paginado.getTotalElements());
        data.put("totalPages", paginado.getTotalPages());
        data.put("first", paginado.isFirst());
        data.put("last", paginado.isLast());
        return data;
    }

    @PostMapping("/consumos")
    public ConsumoApiResponse crearConsumo(@Valid @RequestBody ConsumoApiRequest request) {
        ConsumoRequest consumoRequest = new ConsumoRequest(
                request.sedeId(),
                request.tipoRecursoId(),
                request.cantidad(),
                fechaDesdePeriodo(request.periodo()),
                request.periodo());
        return ApiMapper.consumoDto(consumoService.registrar(consumoRequest));
    }

    @GetMapping("/consumos/{id}")
    public ConsumoApiResponse obtenerConsumo(@PathVariable Long id) {
        return ApiMapper.consumoDto(consumoService.obtener(id));
    }

    @GetMapping("/consumos/sede/{idSede}")
    public List<ConsumoApiResponse> listarConsumosPorSede(@PathVariable Long idSede) {
        List<ConsumoApiResponse> lista = new ArrayList<>();
        for (ConsumoResponse c : consumoService.listarPorSede(idSede)) {
            lista.add(ApiMapper.consumoDto(c));
        }
        return lista;
    }

    @GetMapping("/consumos/periodo/{periodo}")
    public List<ConsumoApiResponse> listarConsumosPorPeriodo(@PathVariable String periodo) {
        List<ConsumoApiResponse> lista = new ArrayList<>();
        for (ConsumoResponse c : consumoService.listarPorPeriodo(periodo)) {
            lista.add(ApiMapper.consumoDto(c));
        }
        return lista;
    }

    private LocalDate fechaDesdePeriodo(String periodo) {
        if (periodo == null || !periodo.matches("\\d{4}-\\d{2}")) {
            throw new IllegalArgumentException("Periodo con formato YYYY-MM.");
        }
        YearMonth periodoRegistro = YearMonth.parse(periodo);
        if (periodoRegistro.isAfter(YearMonth.now())) {
            throw new IllegalArgumentException("No se puede registrar un periodo futuro.");
        }
        return periodoRegistro.atDay(1);
    }

    private boolean puedeGestionarTodasLasSedes(Usuario usuario) {
        for (Rol rol : usuario.getRoles()) {
            if (rol.getNombre() == NombreRol.ADMIN) {
                return true;
            }
        }
        return false;
    }

    public record SedeApiRequest(
            @NotBlank(message = "El nombre de sede es obligatorio.") String nombre,
            String codigo,
            @NotBlank(message = "La direccion es obligatoria.") String direccion,
            @NotBlank(message = "La ciudad es obligatoria.") String ciudad,
            String responsable) {
    }

    public record ConsumoApiRequest(
            @NotNull(message = "sedeId es obligatorio.") Long sedeId,
            @NotNull(message = "tipoRecursoId es obligatorio.") Long tipoRecursoId,
            @NotBlank(message = "El periodo es obligatorio.")
            @Pattern(regexp = "\\d{4}-\\d{2}", message = "Periodo con formato YYYY-MM.") String periodo,
            @NotNull(message = "La cantidad es obligatoria.")
            @Positive(message = "La cantidad debe ser mayor a cero.") BigDecimal cantidad,
            BigDecimal costo,
            String observacion) {
    }

    public record TipoRecursoApiRequest(
            @NotBlank(message = "El nombre del tipo de recurso es obligatorio.") String nombre,
            @NotBlank(message = "La unidad es obligatoria.") String unidad) {
    }
}

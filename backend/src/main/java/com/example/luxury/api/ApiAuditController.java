package com.example.luxury.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.luxury.dominios.auditoria.dto.AuditoriaResponse;
import com.example.luxury.dominios.auditoria.service.AuditoriaService;
import com.example.luxury.dominios.eventoacceso.dto.EventoAccesoResponse;
import com.example.luxury.dominios.eventoacceso.service.EventoAccesoService;

@RestController
@RequestMapping("/api")
public class ApiAuditController {

    private final AuditoriaService auditoriaService;
    private final EventoAccesoService eventoAccesoService;

    public ApiAuditController(AuditoriaService auditoriaService, EventoAccesoService eventoAccesoService) {
        this.auditoriaService = auditoriaService;
        this.eventoAccesoService = eventoAccesoService;
    }

    @GetMapping("/auditorias")
    public List<Map<String, Object>> listarAuditorias() {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (AuditoriaResponse a : auditoriaService.listar()) {
            lista.add(ApiMapper.auditoria(a));
        }
        return lista;
    }

    @GetMapping("/auditorias/paginado")
    public Map<String, Object> listarAuditoriasPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        List<Map<String, Object>> todos = new ArrayList<>();
        for (AuditoriaResponse a : auditoriaService.listar()) {
            todos.add(ApiMapper.auditoria(a));
        }
        int from = Math.min(safePage * safeSize, todos.size());
        int to = Math.min(from + safeSize, todos.size());
        int totalPages = (int) Math.ceil((double) todos.size() / safeSize);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("content", todos.subList(from, to));
        data.put("page", safePage);
        data.put("size", safeSize);
        data.put("totalElements", todos.size());
        data.put("totalPages", totalPages);
        data.put("first", safePage == 0);
        data.put("last", safePage >= totalPages - 1);
        return data;
    }

    @GetMapping("/auditorias/usuario/{id}")
    public List<Map<String, Object>> listarAuditoriasPorUsuario(@PathVariable Long id) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (AuditoriaResponse a : auditoriaService.listarPorUsuario(id)) {
            lista.add(ApiMapper.auditoria(a));
        }
        return lista;
    }

    @GetMapping("/auditorias/modulo/{modulo}")
    public List<Map<String, Object>> listarAuditoriasPorModulo(@PathVariable String modulo) {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (AuditoriaResponse a : auditoriaService.listarPorModulo(modulo)) {
            lista.add(ApiMapper.auditoria(a));
        }
        return lista;
    }

    @GetMapping("/eventos-acceso")
    public List<Map<String, Object>> listarEventosAcceso() {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (EventoAccesoResponse e : eventoAccesoService.listar()) {
            lista.add(ApiMapper.eventoAcceso(e));
        }
        return lista;
    }
}

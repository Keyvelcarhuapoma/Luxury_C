package com.example.luxury.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.luxury.api.dto.DashboardResumenApiResponse;
import com.example.luxury.dominios.dashboard.dto.ConsumoPorSedeResponse;
import com.example.luxury.dominios.dashboard.dto.CostoPorMesSeparadoResponse;
import com.example.luxury.dominios.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class ApiDashboardController {

    private final DashboardService dashboardService;

    public ApiDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumen")
    public DashboardResumenApiResponse resumen() {
        var resumen = dashboardService.resumenGeneral();
        String periodo = dashboardService.periodoMasReciente();
        BigDecimal energiaKwh = dashboardService.consumoKwhPorPeriodo("Luz", periodo);
        BigDecimal aguaM3 = dashboardService.consumoKwhPorPeriodo("Agua", periodo);
        BigDecimal variacion = dashboardService.variacionCostoPorcentaje(periodo);

        return new DashboardResumenApiResponse(
                periodo,
                "PEN",
                resumen.getCostoTotalPen(),
                variacion,
                energiaKwh,
                aguaM3,
                resumen.getTotalSedes(),
                resumen.getTotalAlertas(),
                dashboardService.cumplimientoUmbralesPorcentaje(),
                LocalDateTime.now());
    }

    @GetMapping("/consumo-por-sede")
    public List<Map<String, Object>> consumoPorSede() {
        var costosPorSede = dashboardService.costoPenPorSede();
        var alertasPorSede = dashboardService.alertasPorSede();
        List<Map<String, Object>> lista = new ArrayList<>();
        for (ConsumoPorSedeResponse item : dashboardService.consumoPorSede()) {
            BigDecimal costoTotal = costosPorSede.getOrDefault(item.getSedeId(), BigDecimal.ZERO);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("sedeId", item.getSedeId());
            data.put("sede", item.getSede());
            data.put("energiaKwh", item.getEnergiaKwh());
            data.put("aguaM3", item.getAguaM3());
            data.put("costoTotal", costoTotal);
            data.put("alertas", alertasPorSede.getOrDefault(item.getSedeId(), 0L));
            lista.add(data);
        }
        return lista;
    }

    @GetMapping("/costos-por-mes")
    public List<Map<String, Object>> costosPorMes() {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (CostoPorMesSeparadoResponse item : dashboardService.costosPorMesSeparado()) {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("periodo", item.getPeriodo());
            data.put("etiqueta", item.getPeriodo());
            data.put("costoEnergia", item.getCostoEnergia());
            data.put("costoAgua", item.getCostoAgua());
            data.put("costoTotal", item.getCostoTotal());
            lista.add(data);
        }
        return lista;
    }
}

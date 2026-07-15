package com.example.luxury.api;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import com.example.luxury.dominios.common.enums.EstadoRegistro;
import com.example.luxury.dominios.finanzas.dto.MonedaRequest;
import com.example.luxury.dominios.finanzas.dto.MonedaResponse;
import com.example.luxury.dominios.finanzas.dto.TipoCambioRequest;
import com.example.luxury.dominios.finanzas.dto.TipoCambioResponse;
import com.example.luxury.dominios.finanzas.service.MonedaService;
import com.example.luxury.dominios.finanzas.service.TipoCambioService;

@RestController
@RequestMapping("/api")
public class ApiFinancialController {

    private final MonedaService monedaService;
    private final TipoCambioService tipoCambioService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ApiFinancialController(MonedaService monedaService, TipoCambioService tipoCambioService) {
        this.monedaService = monedaService;
        this.tipoCambioService = tipoCambioService;
    }

    @GetMapping("/monedas")
    public List<Map<String, Object>> listarMonedas() {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (MonedaResponse m : monedaService.listar()) {
            lista.add(ApiMapper.moneda(m));
        }
        return lista;
    }

    @PostMapping("/monedas")
    public Map<String, Object> crearMoneda(@Valid @RequestBody MonedaApiRequest request) {
        return ApiMapper.moneda(monedaService.crear(new MonedaRequest(request.codigo(), request.nombre())));
    }

    @GetMapping("/tipos-cambio")
    public List<Map<String, Object>> listarTiposCambio() {
        List<MonedaResponse> monedas = monedaService.listar();
        List<Map<String, Object>> lista = new ArrayList<>();
        for (TipoCambioResponse cambio : tipoCambioService.listar()) {
            if (!cambio.getMonedaOrigen().equalsIgnoreCase(cambio.getMonedaDestino())) {
                lista.add(ApiMapper.tipoCambio(cambio, monedas));
            }
        }
        return lista;
    }

    @GetMapping("/tipos-cambio/externo")
    public TipoCambioExternoResponse consultarTipoCambioExterno(
            @RequestParam String origen,
            @RequestParam String destino) {
        String codigoOrigen = normalizarCodigo(origen);
        String codigoDestino = normalizarCodigo(destino);

        if (codigoOrigen.equals(codigoDestino)) {
            throw new IllegalArgumentException("Las monedas de origen y destino deben ser distintas.");
        }

        try {
            URI uri = URI.create("https://api.frankfurter.dev/v2/rate/" + codigoOrigen + "/" + codigoDestino);
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalArgumentException("No se encontro tipo de cambio para " + codigoOrigen + "/" + codigoDestino + ".");
            }

            JsonNode json = objectMapper.readTree(response.body());
            return new TipoCambioExternoResponse(
                    json.path("base").asText(codigoOrigen),
                    json.path("quote").asText(codigoDestino),
                    new BigDecimal(json.path("rate").asText()),
                    json.path("date").asText(LocalDate.now().toString()),
                    "Frankfurter");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("No se pudo consultar el tipo de cambio externo.", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo consultar el tipo de cambio externo.", ex);
        }
    }

    @PostMapping("/tipos-cambio")
    public Map<String, Object> crearTipoCambio(@Valid @RequestBody TipoCambioApiRequest request) {
        List<MonedaResponse> monedas = monedaService.listar();
        return ApiMapper.tipoCambio(tipoCambioService.crear(toRequest(request, monedas)), monedas);
    }

    @PutMapping("/tipos-cambio")
    public Map<String, Object> actualizarTipoCambio(@Valid @RequestBody TipoCambioApiRequest request) {
        if (request.id() == null) {
            throw new IllegalArgumentException("El id del tipo de cambio es obligatorio.");
        }
        List<MonedaResponse> monedas = monedaService.listar();
        return ApiMapper.tipoCambio(tipoCambioService.actualizar(request.id(), toRequest(request, monedas)), monedas);
    }

    private TipoCambioRequest toRequest(TipoCambioApiRequest request, List<MonedaResponse> monedas) {
        String monedaOrigen = codigoMoneda(request.monedaOrigenId(), monedas);
        String monedaDestino = codigoMoneda(request.monedaDestinoId(), monedas);

        if (monedaOrigen.equalsIgnoreCase(monedaDestino)) {
            throw new IllegalArgumentException("Las monedas de origen y destino deben ser distintas.");
        }

        return new TipoCambioRequest(
                monedaOrigen,
                monedaDestino,
                request.tasa(),
                LocalDate.parse(request.fechaVigencia()),
                request.activo() == null || request.activo() ? EstadoRegistro.ACTIVO : EstadoRegistro.INACTIVO);
    }

    private String codigoMoneda(Long id, List<MonedaResponse> monedas) {
        for (MonedaResponse moneda : monedas) {
            if (moneda.getId().equals(id)) {
                return moneda.getCodigo();
            }
        }
        throw new IllegalArgumentException("Moneda no encontrada.");
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null || !codigo.trim().toUpperCase().matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Codigo de moneda invalido.");
        }
        return codigo.trim().toUpperCase();
    }

    public record MonedaApiRequest(
            @NotBlank(message = "El codigo de moneda es obligatorio.")
            @Size(min = 3, max = 3, message = "El codigo debe tener 3 letras.")
            @Pattern(regexp = "[A-Z]{3}", message = "El codigo debe estar en mayusculas.") String codigo,
            @NotBlank(message = "El nombre de moneda es obligatorio.") String nombre,
            String simbolo) {
    }

    public record TipoCambioApiRequest(
            Long id,
            @NotNull(message = "monedaOrigenId es obligatorio.") Long monedaOrigenId,
            @NotNull(message = "monedaDestinoId es obligatorio.") Long monedaDestinoId,
            @NotNull(message = "tasa es obligatoria.")
            @Positive(message = "La tasa debe ser mayor a cero.") BigDecimal tasa,
            @NotBlank(message = "fechaVigencia es obligatoria.")
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Formato YYYY-MM-DD.") String fechaVigencia,
            String fuente,
            Boolean activo) {
    }

    public record TipoCambioExternoResponse(
            String monedaOrigenCodigo,
            String monedaDestinoCodigo,
            BigDecimal tasa,
            String fechaVigencia,
            String fuente) {
    }
}

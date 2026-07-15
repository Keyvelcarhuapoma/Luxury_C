import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, delay, forkJoin, map, of } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  AUDITORIAS_MOCK,
  AUDIT_RESUMEN_MOCK,
  EVENTOS_ACCESO_MOCK,
} from '../mocks/audit.mock';
import { AuditModulo, AuditResumen, Auditoria, EventoAcceso } from '../models/audit.model';
import { AccessScopeService } from './access-scope.service';

@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly http = inject(HttpClient);
  private readonly accessScope = inject(AccessScopeService);
  private readonly apiBaseUrl = environment.apiBaseUrl;
  private readonly mockDelayMs = 450;

  obtenerAuditorias(): Observable<Auditoria[]> {
    if (environment.useMocks) {
      return of(this.filtrarAuditoriasPorAlcance(AUDITORIAS_MOCK)).pipe(delay(this.mockDelayMs));
    }
    return this.http.get<Auditoria[]>(`${this.apiBaseUrl}/auditorias`);
  }

  obtenerAuditoriasPorUsuario(id: number): Observable<Auditoria[]> {
    if (environment.useMocks) {
      return this.obtenerAuditorias().pipe(
        map(function(auditorias) { return auditorias.filter(function(auditoria) { return auditoria.usuarioId === id; }); }),
      );
    }
    return this.http.get<Auditoria[]>(`${this.apiBaseUrl}/auditorias/usuario/${id}`);
  }

  obtenerAuditoriasPorModulo(modulo: AuditModulo): Observable<Auditoria[]> {
    if (environment.useMocks) {
      return this.obtenerAuditorias().pipe(
        map(function(auditorias) { return auditorias.filter(function(auditoria) { return auditoria.modulo === modulo; }); }),
      );
    }
    return this.http.get<Auditoria[]>(`${this.apiBaseUrl}/auditorias/modulo/${modulo}`);
  }

  obtenerEventosAcceso(): Observable<EventoAcceso[]> {
    if (environment.useMocks) {
      return of(this.filtrarAccesosPorAlcance(EVENTOS_ACCESO_MOCK)).pipe(delay(this.mockDelayMs));
    }
    return this.http.get<EventoAcceso[]>(`${this.apiBaseUrl}/eventos-acceso`);
  }

  obtenerResumen(): Observable<AuditResumen> {
    if (environment.useMocks) {
      return of(AUDIT_RESUMEN_MOCK).pipe(delay(this.mockDelayMs));
    }
    return forkJoin({
      auditorias: this.obtenerAuditorias(),
      accesos: this.obtenerEventosAcceso(),
    }).pipe(
      map(function({ auditorias, accesos }) {
        return {
          totalAuditorias: auditorias.length,
          totalEventosAcceso: accesos.length,
          eventosObservados:
            auditorias.filter(function(auditoria) { return auditoria.resultado !== 'EXITOSO'; }).length +
            accesos.filter(function(acceso) { return !acceso.exitoso; }).length,
          modulosAuditados: new Set(auditorias.map(function(auditoria) { return auditoria.modulo; })).size,
        };
      }),
    );
  }

  private filtrarAuditoriasPorAlcance(auditorias: Auditoria[]): Auditoria[] {
    if (this.accessScope.esAdmin()) {
      return auditorias;
    }

    const sedeId = this.accessScope.obtenerSedeId();
    return auditorias.filter(function(auditoria) { return auditoria.sedeId === sedeId; });
  }

  private filtrarAccesosPorAlcance(accesos: EventoAcceso[]): EventoAcceso[] {
    if (this.accessScope.esAdmin()) {
      return accesos;
    }

    const sedeId = this.accessScope.obtenerSedeId();
    return accesos.filter(function(acceso) { return acceso.sedeId === sedeId; });
  }
}

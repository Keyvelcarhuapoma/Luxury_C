import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal, type Signal } from '@angular/core';
import { Observable, catchError, map, of, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { NombreRol } from '../models/role.model';

export type AlertCenterType = 'Usuario' | 'Alerta' | 'Consumo' | 'Sistema';
export type NotificationEventType =
  | 'USER_REGISTERED'
  | 'RESOURCE_REGISTERED'
  | 'BUDGET_EXCEEDED'
  | 'RATE_CHANGED';

export interface AlertCenterItem {
  id: number | string;
  tipo: AlertCenterType;
  titulo: string;
  detalle: string;
  fecha: string;
  leida: boolean;
  roles: NombreRol[];
  sedeId: number | null;
}

const STORAGE_KEY = 'luxury_alert_center';
const NOTIFICATION_RULES: Record<NotificationEventType, NombreRol[]> = {
  USER_REGISTERED: ['ADMIN'],
  RESOURCE_REGISTERED: ['ADMIN', 'GERENTE'],
  BUDGET_EXCEEDED: ['ADMIN', 'GERENTE'],
  RATE_CHANGED: ['ADMIN'],
};

@Injectable({ providedIn: 'root' })
export class AlertCenterService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = environment.apiBaseUrl;
  private readonly itemsSignal = signal<AlertCenterItem[]>(this.leer());

  readonly items = this.itemsSignal.asReadonly();
  readonly pendientesAdmin: Signal<number>;

  constructor() {
    const _this = this;
    this.pendientesAdmin = computed(
      function() { return _this.items().filter(function(item) { return item.roles.includes('ADMIN') && !item.leida; }).length; },
    );
  }

  crearParaAdmin(tipo: AlertCenterType, titulo: string, detalle: string): void {
    this.crearPorEvento('USER_REGISTERED', tipo, titulo, detalle, null);
  }

  crearPorEvento(
    evento: NotificationEventType,
    tipo: AlertCenterType,
    titulo: string,
    detalle: string,
    sedeId: number | null,
  ): void {
    const item: AlertCenterItem = {
      id: Date.now(),
      tipo,
      titulo,
      detalle,
      fecha: new Date().toISOString(),
      leida: false,
      roles: NOTIFICATION_RULES[evento],
      sedeId,
    };
    this.guardar([item, ...this.itemsSignal()].slice(0, 30));
  }

  obtenerPorPerfil(roles: NombreRol[], sedeId: number | null): AlertCenterItem[] {
    const esAdmin = roles.includes('ADMIN');
    const resultado: AlertCenterItem[] = [];

    for (const item of this.items()) {
      const coincideRol = item.roles.some(function(rol) { return roles.includes(rol); });
      const coincideSede = esAdmin || item.sedeId === null || item.sedeId === sedeId;

      if (coincideRol && coincideSede) {
        resultado.push(item);
      }
    }

    return resultado;
  }

  obtenerPorRoles(roles: NombreRol[]): AlertCenterItem[] {
    return this.obtenerPorPerfil(roles, null);
  }

  sincronizarAlertasBackend(roles: NombreRol[], sedeId: number | null): Observable<AlertCenterItem[]> {
    if (!roles.includes('ADMIN') && !roles.includes('GERENTE')) {
      return of(this.obtenerPorPerfil(roles, sedeId));
    }

    const url = roles.includes('ADMIN') || sedeId == null
      ? `${this.apiBaseUrl}/alertas`
      : `${this.apiBaseUrl}/alertas/sede/${sedeId}`;

    const _this = this;
    return this.http.get<ApiAlerta[]>(url).pipe(
      map(function(alertas) { return _this.actualizarAlertasDesdeBackend(alertas); }),
      tap(function(items) { return _this.guardar(items); }),
      map(function() { return _this.obtenerPorPerfil(roles, sedeId); }),
      catchError(function() { return of(_this.obtenerPorPerfil(roles, sedeId)); }),
    );
  }

  marcarLeida(id: number | string): void {
    this.guardar(
      this.itemsSignal().map(function(item) { return item.id === id ? { ...item, leida: true } : item; }),
    );
  }

  marcarLeidasPorRoles(roles: NombreRol[]): void {
    this.guardar(
      this.itemsSignal().map(function(item) {
        return item.roles.some(function(rol) { return roles.includes(rol); }) ? { ...item, leida: true } : item;
      }),
    );
  }

  private leer(): AlertCenterItem[] {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return [];
    }

    try {
      const items = JSON.parse(raw) as AlertCenterItem[];
      const filtrados = items.filter(function(item) { return item.titulo !== 'Evento de sesion detectado'; });

      if (filtrados.length !== items.length) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(filtrados));
      }

      return filtrados;
    } catch {
      localStorage.removeItem(STORAGE_KEY);
      return [];
    }
  }

  private guardar(items: AlertCenterItem[]): void {
    this.itemsSignal.set(items);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  }

  private actualizarAlertasDesdeBackend(alertas: ApiAlerta[]): AlertCenterItem[] {
    const actuales = this.itemsSignal();
    const _this = this;
    const leidas = new Map(actuales.map(function(item) { return [_this.clave(item), item.leida]; }));
    const locales = actuales.filter(function(item) { return !String(item.id).startsWith('alerta-'); });
    const backendItems = alertas.map(function(alerta) {
      const id = `alerta-${alerta.id}`;
      const sedeId = alerta.sedeId && alerta.sedeId > 0 ? alerta.sedeId : null;
      const severidad = alerta.severidad ?? 'MEDIA';
      const titulo = severidad === 'CRITICA' || severidad === 'ALTA'
        ? 'Alerta critica'
        : 'Alerta operativa';

      return {
        id,
        tipo: 'Alerta' as AlertCenterType,
        titulo,
        detalle: alerta.sedeNombre ? `${alerta.sedeNombre}: ${alerta.mensaje}` : alerta.mensaje,
        fecha: alerta.fechaGeneracion,
        leida: Boolean(alerta.atendida) || leidas.get(id) === true,
        roles: ['ADMIN', 'GERENTE'] as NombreRol[],
        sedeId,
      };
    });

    return [...backendItems, ...locales]
      .sort(function(a, b) { return new Date(b.fecha).getTime() - new Date(a.fecha).getTime(); })
      .slice(0, 30);
  }

  private clave(item: AlertCenterItem): string | number {
    return item.id;
  }
}

interface ApiAlerta {
  id: number;
  sedeId: number | null;
  sedeNombre: string | null;
  severidad: 'CRITICA' | 'ALTA' | 'MEDIA' | 'BAJA' | null;
  mensaje: string;
  fechaGeneracion: string;
  atendida?: boolean;
}

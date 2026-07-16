import { Routes } from '@angular/router';

import { roleGuard } from '../core/guards/role.guard';
import { AppLayout } from './app-layout/app-layout';

export const USER_APP_ROUTES: Routes = [
  {
    path: '',
    component: AppLayout,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'panel',
      },
      {
        path: 'panel',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'GERENTE', 'OPERADOR'] },
        loadComponent: function() { return import('./pages/role-panel/role-panel').then(function(m) { return m.RolePanel; }); },
        title: 'Luxury - Panel',
      },
      {
        path: 'dashboard',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'GERENTE'] },
        loadComponent: function() {
          return import('./domains/dashboard/executive-dashboard/executive-dashboard').then(
            function(m) { return m.ExecutiveDashboard; },
          );
        },
        title: 'Luxury - Dashboard',
      },
      {
        path: 'resources',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN', 'GERENTE', 'OPERADOR'],
        },
        loadComponent: function() {
          return import('./domains/resources/pages/resources-overview/resources-overview').then(
            function(m) { return m.ResourcesOverview; },
          );
        },
        title: 'Luxury - Recursos',
      },
      {
        path: 'resources/energy',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN', 'GERENTE', 'OPERADOR'],
        },
        loadComponent: function() {
          return import('./domains/resources/pages/energy-consumption/energy-consumption').then(
            function(m) { return m.EnergyConsumption; },
          );
        },
        title: 'Luxury - Energia',
      },
      {
        path: 'resources/water',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN', 'GERENTE', 'OPERADOR'],
        },
        loadComponent: function() {
          return import('./domains/resources/pages/water-consumption/water-consumption').then(
            function(m) { return m.WaterConsumption; },
          );
        },
        title: 'Luxury - Agua',
      },
      {
        path: 'resources/gas',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN', 'GERENTE', 'OPERADOR'],
        },
        loadComponent: function() {
          return import('./domains/resources/pages/gas-consumption/gas-consumption').then(
            function(m) { return m.GasConsumption; },
          );
        },
        title: 'Luxury - Gas',
      },
      {
        path: 'resources/transactions',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN', 'GERENTE', 'OPERADOR'],
        },
        loadComponent: function() {
          return import('./domains/resources/pages/transactions/transactions').then(function(m) { return m.Transactions; });
        },
        title: 'Luxury - Transacciones',
      },
      {
        path: 'financial-exchange',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN'],
        },
        loadComponent: function() {
          return import('./domains/financial-exchange/financial-exchange/financial-exchange').then(
            function(m) { return m.FinancialExchange; },
          );
        },
        title: 'Luxury - Cambio financiero',
      },
      {
        path: 'business-rules',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN', 'GERENTE'],
        },
        loadComponent: function() {
          return import('./domains/business-rules/business-rules/business-rules').then(
            function(m) { return m.BusinessRules; },
          );
        },
        title: 'Luxury - Reglas',
      },
      {
        path: 'audit',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN', 'AUDITOR'],
        },
        loadComponent: function() {
          return import('./domains/audit/audit/audit').then(function(m) { return m.Audit; });
        },
        title: 'Luxury - Auditoria',
      },
      {
        path: 'reports',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN', 'GERENTE'],
        },
        loadComponent: function() {
          return import('./domains/reports/reports/reports').then(function(m) { return m.Reports; });
        },
        title: 'Luxury - Reportes',
      },
      {
        path: 'session-monitoring',
        canActivate: [roleGuard],
        data: {
          roles: ['ADMIN'],
        },
        loadComponent: function() {
          return import('./domains/session-monitoring/session-monitoring/session-monitoring').then(
            function(m) { return m.SessionMonitoring; },
          );
        },
        title: 'Luxury - Sesiones',
      },
    ],
  },
];

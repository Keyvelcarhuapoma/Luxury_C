import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login',
  },
  {
    path: 'login',
    loadComponent: function() {
      return import('./public/public-layout/public-layout').then(function(m) { return m.PublicLayout; });
    },
    children: [
      {
        path: '',
        loadComponent: function() { return import('./public/auth/login/login').then(function(m) { return m.Login; }); },
        title: 'Luxury - Iniciar sesion',
      },
    ],
  },
  {
    path: 'registro',
    loadComponent: function() {
      return import('./public/public-layout/public-layout').then(function(m) { return m.PublicLayout; });
    },
    children: [
      {
        path: '',
        loadComponent: function() { return import('./public/auth/register/register').then(function(m) { return m.Register; }); },
        title: 'Luxury - Registro',
      },
    ],
  },
  {
    path: 'layout',
    canActivate: [authGuard],
    loadChildren: function() { return import('./user-app/user-app.routes').then(function(m) { return m.USER_APP_ROUTES; }); },
  },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    loadChildren: function() {
      return import('./admin-panel/admin-panel.routes').then(function(m) { return m.ADMIN_PANEL_ROUTES; });
    },
  },
  {
    path: '',
    canActivate: [authGuard],
    loadChildren: function() { return import('./user-app/user-app.routes').then(function(m) { return m.USER_APP_ROUTES; }); },
  },
  {
    path: '**',
    redirectTo: 'panel',
  },
];

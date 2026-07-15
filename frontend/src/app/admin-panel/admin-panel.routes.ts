import { Routes } from '@angular/router';

import { AdminLayout } from './admin-layout/admin-layout';

export const ADMIN_PANEL_ROUTES: Routes = [
  {
    path: '',
    component: AdminLayout,
    children: [
      {
        path: '',
        redirectTo: 'users',
        pathMatch: 'full',
      },
      {
        path: 'users',
        loadComponent: function() { return import('./users/users').then(function(m) { return m.Users; }); },
        title: 'Luxury - Usuarios',
      },
      {
        path: 'roles',
        loadComponent: function() { return import('./roles/roles').then(function(m) { return m.Roles; }); },
        title: 'Luxury - Roles',
      },
      {
        path: 'permissions',
        loadComponent: function() { return import('./permissions/permissions').then(function(m) { return m.Permissions; }); },
        title: 'Luxury - Permisos',
      },
    ],
  },
];

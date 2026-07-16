import { Component } from '@angular/core';

import { ResourceConsumption } from '../resource-consumption/resource-consumption';

@Component({
  selector: 'app-gas-consumption',
  standalone: true,
  imports: [ResourceConsumption],
  template: `
    <app-resource-consumption
      tipo="GAS"
      titulo="Consumo de gas"
      descripcion="Monitoreo de consumo de gas por sede, seguimiento de costos y registros observados."
    />
  `,
})
export class GasConsumption {}

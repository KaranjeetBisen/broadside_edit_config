import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { CampaignComponent } from './components/campaign/campaign.component';
import { TemplateComponent } from './components/template/template.component';

export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'campaign', component: CampaignComponent },
  { path: 'template', component: TemplateComponent },
  { path: '**', redirectTo: '' }
];

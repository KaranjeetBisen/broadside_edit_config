import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ConfigService, ConfigView } from '../../services/config.service';

@Component({
  selector: 'app-campaign',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './campaign.component.html',
  styleUrl: './campaign.component.css'
})
export class CampaignComponent implements OnInit {
  configs: ConfigView[] = [];
  selectedConfig: ConfigView | null = null;
  editMode = false;
  editedConfig: any = {};
  campaignId = '';
  loading = false;
  error: string | null = null;
  successMessage: string | null = null;

  constructor(
    private configService: ConfigService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadConfigs();
  }

  loadConfigs() {
    this.loading = true;
    this.configService.getAllConfigs().subscribe({
      next: (data) => {
        this.configs = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load configurations';
        this.loading = false;
        console.error(err);
      }
    });
  }

  viewConfig(config: ConfigView) {
    this.selectedConfig = config;
    this.editMode = false;
  }

  startEdit() {
    if (this.selectedConfig) {
      this.editedConfig = JSON.parse(JSON.stringify(this.selectedConfig.configSettings));
      this.editMode = true;
    }
  }

  cancelEdit() {
    this.editMode = false;
    this.editedConfig = {};
  }

  saveConfig() {
    if (!this.selectedConfig) return;

    this.loading = true;
    this.configService.updateConfig(this.selectedConfig.campId, this.editedConfig).subscribe({
      next: () => {
        this.successMessage = 'Configuration updated successfully!';
        this.editMode = false;
        this.loadConfigs();
        setTimeout(() => {
          this.successMessage = null;
        }, 3000);
      },
      error: (err) => {
        this.error = 'Failed to update configuration';
        console.error(err);
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  deleteConfig(campId: string) {
    if (confirm('Are you sure you want to delete this configuration?')) {
      this.configService.deleteConfig(campId).subscribe({
        next: () => {
          this.successMessage = 'Configuration deleted successfully!';
          this.selectedConfig = null;
          this.loadConfigs();
        },
        error: (err) => {
          this.error = 'Failed to delete configuration';
          console.error(err);
        }
      });
    }
  }

  goBack() {
    this.router.navigate(['/']);
  }
}

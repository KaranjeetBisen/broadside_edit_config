import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { TemplateService, TemplateView } from '../../services/template.service';

@Component({
  selector: 'app-template',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './template.component.html',
  styleUrl: './template.component.css'
})
export class TemplateComponent implements OnInit {
  templates: TemplateView[] = [];
  selectedTemplate: TemplateView | null = null;
  campaignId = '';
  loading = false;
  error: string | null = null;
  successMessage: string | null = null;
  previewMode = false;
  editMode = false;
  editedContent = '';
  selectedFile: File | null = null;
  createBackup = true;

  constructor(
    private templateService: TemplateService,
    private router: Router
  ) {}

  ngOnInit() {
    // Initialize with empty state
  }

  loadTemplate() {
    if (!this.campaignId.trim()) {
      this.error = 'Please enter a campaign ID';
      return;
    }

    this.loading = true;
    this.error = null;
    this.templateService.getTemplateInfo(this.campaignId).subscribe({
      next: (template) => {
        this.selectedTemplate = template;
        if (template.htmlContent) {
          this.editedContent = template.htmlContent;
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = `Failed to load template: ${err.error?.message || err.statusText}`;
        this.selectedTemplate = null;
        this.loading = false;
      }
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file && file.name.endsWith('.html')) {
      this.selectedFile = file;
    } else {
      this.error = 'Please select a valid HTML file';
      this.selectedFile = null;
    }
  }

  uploadTemplate() {
    if (!this.selectedFile) {
      this.error = 'Please select a file to upload';
      return;
    }

    this.loading = true;
    this.templateService.uploadTemplate(this.campaignId, this.selectedFile, this.createBackup).subscribe({
      next: () => {
        this.successMessage = 'Template uploaded successfully!';
        this.selectedFile = null;
        this.loadTemplate();
        setTimeout(() => {
          this.successMessage = null;
        }, 3000);
      },
      error: (err) => {
        this.error = `Failed to upload template: ${err.error?.message || err.statusText}`;
        this.loading = false;
      }
    });
  }

  downloadTemplate() {
    if (!this.selectedTemplate) return;

    this.templateService.downloadTemplate(this.campaignId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        const fileName = this.selectedTemplate?.filePath.split('/').pop() || `${this.campaignId}.html`;
        link.download = fileName;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        this.error = 'Failed to download template';
        console.error(err);
      }
    });
  }

  startEdit() {
    if (this.selectedTemplate?.htmlContent) {
      this.editedContent = this.selectedTemplate.htmlContent;
      this.editMode = true;
    }
  }

  saveEdit() {
    if (!this.selectedFile && this.editedContent) {
      // Create a blob from edited content
      const blob = new Blob([this.editedContent], { type: 'text/html' });
      const file = new File([blob], `${this.campaignId}.html`, { type: 'text/html' });
      this.selectedFile = file;
    }

    this.uploadTemplate();
    this.editMode = false;
  }

  cancelEdit() {
    this.editMode = false;
    this.editedContent = '';
  }

  togglePreview() {
    this.previewMode = !this.previewMode;
  }

  deleteTemplate() {
    if (confirm('Are you sure you want to delete this template?')) {
      this.loading = true;
      this.templateService.deleteTemplate(this.campaignId).subscribe({
        next: () => {
          this.successMessage = 'Template deleted successfully!';
          this.selectedTemplate = null;
          this.editedContent = '';
          setTimeout(() => {
            this.successMessage = null;
          }, 3000);
        },
        error: (err) => {
          this.error = 'Failed to delete template';
          console.error(err);
        },
        complete: () => {
          this.loading = false;
        }
      });
    }
  }

  goBack() {
    this.router.navigate(['/']);
  }
}

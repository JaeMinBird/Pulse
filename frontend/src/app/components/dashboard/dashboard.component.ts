import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { interval } from 'rxjs';
import { startWith, switchMap } from 'rxjs/operators';
import { ApiService } from '../../services/api.service';
import { RepositoryWithLatestBuild } from '../../models/repository.model';
import { RepositoryCardComponent } from '../repository-card/repository-card.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RepositoryCardComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  private apiService = inject(ApiService);

  repositories: RepositoryWithLatestBuild[] = [];
  loading = true;
  error: string | null = null;
  lastUpdated: Date = new Date();
  autoRefresh = true;
  refreshInterval = 30000; // 30 seconds

  ngOnInit(): void {
    this.loadData();
    this.setupAutoRefresh();
  }

  loadData(): void {
    this.loading = true;
    this.error = null;

    this.apiService.getRepositoriesWithLatestBuilds().subscribe({
      next: (data) => {
        this.repositories = data;
        this.loading = false;
        this.lastUpdated = new Date();
      },
      error: (error) => {
        console.error('Error loading data:', error);
        this.error = 'Failed to load repository data. Please check if the backend is running.';
        this.loading = false;
      }
    });
  }

  setupAutoRefresh(): void {
    interval(this.refreshInterval)
      .pipe(
        startWith(0),
        switchMap(() => this.apiService.getRepositoriesWithLatestBuilds())
      )
      .subscribe({
        next: (data) => {
          if (this.autoRefresh) {
            this.repositories = data;
            this.lastUpdated = new Date();
          }
        },
        error: (error) => {
          console.error('Auto-refresh error:', error);
        }
      });
  }

  toggleAutoRefresh(): void {
    this.autoRefresh = !this.autoRefresh;
  }

  manualRefresh(): void {
    this.loadData();
  }

  triggerSync(): void {
    this.apiService.triggerScheduledSync().subscribe({
      next: (response) => {
        console.log('Sync triggered:', response);
        // Reload data after a short delay to allow sync to complete
        setTimeout(() => this.loadData(), 2000);
      },
      error: (error) => {
        console.error('Sync error:', error);
        this.error = 'Failed to trigger sync. Please try again.';
      }
    });
  }

  getStatusCounts() {
    const counts = {
      success: 0,
      failed: 0,
      inProgress: 0,
      pending: 0,
      total: this.repositories.length
    };

    this.repositories.forEach(repo => {
      const status = repo.latestBuild?.status;
      if (status === 'SUCCESS') counts.success++;
      else if (status === 'FAILED') counts.failed++;
      else if (status === 'IN_PROGRESS') counts.inProgress++;
      else if (status === 'PENDING') counts.pending++;
    });

    return counts;
  }

  formatTime(date: Date): string {
    return date.toLocaleTimeString();
  }
}

import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RepositoryWithLatestBuild, BuildStatus } from '../../models/repository.model';

@Component({
  selector: 'app-repository-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './repository-card.component.html',
  styleUrls: ['./repository-card.component.css']
})
export class RepositoryCardComponent {
  @Input() data!: RepositoryWithLatestBuild;

  getStatusColor(status?: BuildStatus): string {
    if (!status) return 'bg-gray-400';

    const colors: Record<BuildStatus, string> = {
      'SUCCESS': 'bg-build-success',
      'FAILED': 'bg-build-failure',
      'PENDING': 'bg-build-pending',
      'IN_PROGRESS': 'bg-build-progress',
      'CANCELLED': 'bg-build-cancelled'
    };

    return colors[status] || 'bg-gray-400';
  }

  getStatusText(status?: BuildStatus): string {
    if (!status) return 'No builds';
    return status.replace('_', ' ');
  }

  getStatusIcon(status?: BuildStatus): string {
    if (!status) return '○';

    const icons: Record<BuildStatus, string> = {
      'SUCCESS': '✓',
      'FAILED': '✕',
      'PENDING': '○',
      'IN_PROGRESS': '↻',
      'CANCELLED': '⊘'
    };

    return icons[status] || '?';
  }

  shouldAnimate(status?: BuildStatus): boolean {
    return status === 'IN_PROGRESS';
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';

    const date = new Date(dateString);
    const now = new Date();
    const diff = now.getTime() - date.getTime();

    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7) return `${days}d ago`;

    return date.toLocaleDateString();
  }

  getShortCommitSha(sha?: string): string {
    if (!sha) return 'N/A';
    return sha.substring(0, 7);
  }

  openGitHub(): void {
    if (this.data.repository.githubUrl) {
      window.open(this.data.repository.githubUrl, '_blank');
    }
  }
}

export interface Repository {
  id: number;
  name: string;
  githubUrl: string;
  createdAt: string;
}

export interface Build {
  id: number;
  repositoryId: number;
  repositoryName: string;
  status: BuildStatus;
  commitSha: string;
  startedAt: string;
  completedAt?: string;
}

export type BuildStatus = 'PENDING' | 'IN_PROGRESS' | 'SUCCESS' | 'FAILED' | 'CANCELLED';

export interface RepositoryWithLatestBuild {
  repository: Repository;
  latestBuild?: Build;
}

export interface SyncResponse {
  success: boolean;
  syncedCount: number;
  message: string;
  repositoryName?: string;
}

public class AddRepoMeta
{
	private String commitId;

	private String currentBranch;

	private boolean localChanges;

	private boolean syncInProgress;

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public String getCurrentBranch() {
		return currentBranch;
	}

	public void setCurrentBranch(String currentBranch) {
		this.currentBranch = currentBranch;
	}

	public boolean isLocalChanges() {
		return localChanges;
	}

	public void setLocalChanges(boolean localChanges) {
		this.localChanges = localChanges;
	}

	public boolean isSyncInProgress() {
		return syncInProgress;
	}

	public void setSyncInProgress(boolean syncInProgress) {
		this.syncInProgress = syncInProgress;
	}
}
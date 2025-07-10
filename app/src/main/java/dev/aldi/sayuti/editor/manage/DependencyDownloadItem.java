package dev.aldi.sayuti.editor.manage;

import androidx.annotation.NonNull;
import org.cosmic.ide.dependency.resolver.api.Artifact;

public class DependencyDownloadItem {

    public enum DownloadState {
        PENDING,
        RESOLVING,
        DOWNLOADING,
        UNZIPPING,
        DEXING,
        COMPLETED,
        ERROR
    }

    private final String name;
    private final String displayName;
    private DownloadState state;
    private String statusMessage;
    private long bytesDownloaded;
    private long totalBytes;
    private int progress; // 0-100
    private String errorMessage;
    private final Artifact artifact;

    public DependencyDownloadItem(@NonNull Artifact artifact) {
        this.artifact = artifact;
        this.name = artifact.toString();
        this.displayName = artifact.getArtifactId() + "-" + artifact.getVersion();
        this.state = DownloadState.PENDING;
        this.statusMessage = "Pending...";
        this.bytesDownloaded = 0;
        this.totalBytes = 0;
        this.progress = 0;
        this.errorMessage = null;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public DownloadState getState() {
        return state;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public long getBytesDownloaded() {
        return bytesDownloaded;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public int getProgress() {
        return progress;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setState(DownloadState state) {
        this.state = state;
        updateStatusMessage();
    }

    public void setDownloadProgress(long bytesDownloaded, long totalBytes) {
        this.bytesDownloaded = bytesDownloaded;
        this.totalBytes = totalBytes;
        if (totalBytes > 0) {
            this.progress = (int) ((bytesDownloaded * 100) / totalBytes);
        }
        updateStatusMessage();
    }

    public void setError(String errorMessage) {
        this.state = DownloadState.ERROR;
        this.errorMessage = errorMessage;
        this.statusMessage = "Error: " + errorMessage;
    }

    private void updateStatusMessage() {
        switch (state) {
            case PENDING:
                statusMessage = "Pending...";
                break;
            case RESOLVING:
                statusMessage = "Resolving dependency...";
                break;
            case DOWNLOADING:
                if (totalBytes > 0) {
                    statusMessage = formatBytes(bytesDownloaded) + " / " + formatBytes(totalBytes);
                } else {
                    statusMessage = "Downloading...";
                }
                break;
            case UNZIPPING:
                statusMessage = "Unzipping...";
                break;
            case DEXING:
                statusMessage = "Processing (DEX)...";
                break;
            case COMPLETED:
                statusMessage = "Completed";
                break;
            case ERROR:
                statusMessage = "Error: " + (errorMessage != null ? errorMessage : "Unknown error");
                break;
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    public boolean isCompleted() {
        return state == DownloadState.COMPLETED;
    }

    public boolean isError() {
        return state == DownloadState.ERROR;
    }

    public boolean isInProgress() {
        return state == DownloadState.DOWNLOADING ||
                state == DownloadState.UNZIPPING ||
                state == DownloadState.DEXING ||
                state == DownloadState.RESOLVING;
    }

    public boolean isPending() {
        return state == DownloadState.PENDING;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DependencyDownloadItem that = (DependencyDownloadItem) obj;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "DependencyDownloadItem{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", state=" + state +
                ", statusMessage='" + statusMessage + '\'' +
                ", bytesDownloaded=" + bytesDownloaded +
                ", totalBytes=" + totalBytes +
                ", progress=" + progress +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

}

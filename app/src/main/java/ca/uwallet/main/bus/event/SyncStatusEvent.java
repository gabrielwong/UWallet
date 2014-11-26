package ca.uwallet.main.bus.event;

/**
 * Created by gabriel on 6/14/14.
 */
public class SyncStatusEvent {

    private final Status status;

    public enum Status {
        STARTED, FINISHED
    }

    public SyncStatusEvent(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isInProgress() {
        return status == Status.STARTED;
    }
}

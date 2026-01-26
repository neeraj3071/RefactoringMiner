package TestFeatures;

public class EventDeclarations {
    private java.util.List<OnStartedListener> onStartedListeners = new java.util.ArrayList<>();
    public void addOnStartedListener(OnStartedListener listener) {
        onStartedListeners.add(listener);
    }
    public void removeOnStartedListener(OnStartedListener listener) {
        onStartedListeners.remove(listener);
    }
    protected void fireStarted() {
        for (OnStartedListener listener : onStartedListeners) {
            listener.onStarted();
        }
    }
    public interface OnStartedListener {
        void onStarted();
    }
    private java.util.List<OnMessageReceivedListener> onMessageReceivedListeners = new java.util.ArrayList<>();
    public void addOnMessageReceivedListener(OnMessageReceivedListener listener) {
        onMessageReceivedListeners.add(listener);
    }
    public void removeOnMessageReceivedListener(OnMessageReceivedListener listener) {
        onMessageReceivedListeners.remove(listener);
    }
    protected void fireMessageReceived() {
        for (OnMessageReceivedListener listener : onMessageReceivedListeners) {
            listener.onMessageReceived();
        }
    }
    public interface OnMessageReceivedListener {
        void onMessageReceived();
    }
    private java.util.List<OnDataChangedListener> onDataChangedListeners = new java.util.ArrayList<>();
    public void addOnDataChangedListener(OnDataChangedListener listener) {
        onDataChangedListeners.add(listener);
    }
    public void removeOnDataChangedListener(OnDataChangedListener listener) {
        onDataChangedListeners.remove(listener);
    }
    protected void fireDataChanged() {
        for (OnDataChangedListener listener : onDataChangedListeners) {
            listener.onDataChanged();
        }
    }
    public interface OnDataChangedListener {
        void onDataChanged();
    }
    private java.util.List<OnCompletedListener> onCompletedListeners = new java.util.ArrayList<>();
    public void addOnCompletedListener(OnCompletedListener listener) {
        onCompletedListeners.add(listener);
    }
    public void removeOnCompletedListener(OnCompletedListener listener) {
        onCompletedListeners.remove(listener);
    }
    protected void fireCompleted() {
        for (OnCompletedListener listener : onCompletedListeners) {
            listener.onCompleted();
        }
    }
    public interface OnCompletedListener {
        void onCompleted();
    }
    private java.util.List<OnInternalEventListener> onInternalEventListeners = new java.util.ArrayList<>();
    public void addOnInternalEventListener(OnInternalEventListener listener) {
        onInternalEventListeners.add(listener);
    }
    public void removeOnInternalEventListener(OnInternalEventListener listener) {
        onInternalEventListeners.remove(listener);
    }
    protected void fireInternalEvent() {
        for (OnInternalEventListener listener : onInternalEventListeners) {
            listener.onInternalEvent();
        }
    }
    public interface OnInternalEventListener {
        void onInternalEvent();
    }
    public void TriggerStarted() {
        fireStarted();
    }
    public void TriggerMessage(String message) {
        fireMessageReceived();
    }
    public void TriggerDataChanged(int id, String data) {
        fireDataChanged();
    }
    public void TriggerCompleted() {
        fireCompleted();
    }
}

package ndds.com.trakidhome;

public abstract class StateObject {
    private boolean a, b;
    private Object data;
    private boolean alreadyPerformed = false;

    public void setB(boolean b) {
        this.b = b;
        checkCondition();
    }

    public void setA(boolean a) {
        this.a = a;
        checkCondition();
    }

    public void reset() {
        alreadyPerformed = false;
    }

    private void checkCondition() {
        if (a && b && !alreadyPerformed) {
            onBothConditionStatisfied();
            alreadyPerformed = true;
        }
    }

    abstract void onBothConditionStatisfied();

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }
}

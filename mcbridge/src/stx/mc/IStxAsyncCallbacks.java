package stx.mc;

public interface IStxAsyncCallbacks {

    public abstract void jobSuccess();
    public abstract void jobFailed(int errorCode);
}

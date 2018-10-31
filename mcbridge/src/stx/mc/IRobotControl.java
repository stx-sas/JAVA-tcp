package stx.mc;

public interface IRobotControl {

    void moveEndPoint(Position position) throws StxCommunicationException, StxControllerErrorException;
    void moveEndPointAsync(Position position, IStxAsyncCallbacks ack);


    void setAngles(double[] angles) throws StxCommunicationException, StxControllerErrorException;
    void setAnglesAsync(double[] angles, IStxAsyncCallbacks ack);

    void connect(String ip);

    void disconnect();

    void enable() throws StxCommunicationException, StxControllerErrorException;

    void disable() throws StxCommunicationException, StxControllerErrorException;
}

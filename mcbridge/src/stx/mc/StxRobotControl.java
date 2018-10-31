package stx.mc;

public class StxRobotControl implements IRobotControl {


    private StxTelnetClient _stxTelnetClient;

    public void connect(String ip){
        _stxTelnetClient = new StxTelnetClient();
        _stxTelnetClient.connect(ip);
    }

    @Override
    public void disconnect() {
        _stxTelnetClient.disconnect();
    }

    @Override
    public void enable() throws StxCommunicationException, StxControllerErrorException {
        _stxTelnetClient.enable();
    }

    @Override
    public void disable() throws StxCommunicationException, StxControllerErrorException {
        _stxTelnetClient.disable();
    }

    @Override
    public void moveEndPoint(Position position) throws StxCommunicationException, StxControllerErrorException {
//        _stxTelnetClient.moveEndPointToXYZ(position.xyz, 100);
        _stxTelnetClient.moveEndPointToPosition(position.xyz, position.ypr, true, 100);

    }

    @Override
    public void moveEndPointAsync(Position position, IStxAsyncCallbacks ack) {

    }

    @Override
    public void setAngles(double[] angles) throws StxCommunicationException, StxControllerErrorException {
        _stxTelnetClient.setAngles(angles, false, 100);

    }

    @Override
    public void setAnglesAsync(double[] angles, IStxAsyncCallbacks ack) {

    }
}

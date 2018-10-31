package stx.mc;

public class ResponseWaitObject {

    private final Object _waitObject = new Object();
    private boolean _isResolved = false;
    private Integer _errorCode = 0;

    private String[] _response;

    public String[] waitForResponse(long timeoutMs) throws StxCommunicationException, StxControllerErrorException {
        long startWaitTime = System.currentTimeMillis();
        synchronized (_waitObject){
            while (!_isResolved){
                try {
                    _waitObject.wait(100);
                    long elapsed = System.currentTimeMillis() - startWaitTime;
                    if (elapsed > timeoutMs){
                        System.err.println("STX Controller not responding for " + elapsed + "ms");
                        throw new StxCommunicationException();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (_errorCode != null && _errorCode != 0){
                throw new StxControllerErrorException(_errorCode);
            }
        }
        return _response;

    }

    public void resolve(Integer errorCode, String[] response){
        synchronized (_waitObject){
            _isResolved = true;
            _errorCode = errorCode;
            _response = response;
            _waitObject.notifyAll();
        }
    }

}

package stx.mc;

public class StxControllerErrorException extends Exception {

    @Override
    public String getMessage() {
        return "STX_CONTROLLER_ERROR " + _errorCode;
    }

    public StxControllerErrorException(int errorCode) {
        this._errorCode = errorCode;
    }

    private int _errorCode;
}

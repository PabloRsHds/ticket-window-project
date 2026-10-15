package br.com.queue.infra.unit;

public class UnitInactiveException extends RuntimeException {
    public UnitInactiveException(String message) {
        super(message);
    }
}

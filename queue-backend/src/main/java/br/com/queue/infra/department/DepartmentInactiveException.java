package br.com.queue.infra.department;

public class DepartmentInactiveException extends RuntimeException {
    public DepartmentInactiveException(String message) {
        super(message);
    }
}

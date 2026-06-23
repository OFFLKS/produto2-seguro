package com.locadora.dto;

/**
 * DTO para resposta de login — nunca retorna a senha ou stack trace.
 */
public class LoginResponse {
    private boolean success;
    private String message;
    private Long usuarioId;
    private String nome;
    private String role;

    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LoginResponse(boolean success, String message, Long usuarioId, String nome, String role) {
        this.success = success;
        this.message = message;
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.role = role;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Long getUsuarioId() { return usuarioId; }
    public String getNome() { return nome; }
    public String getRole() { return role; }
}

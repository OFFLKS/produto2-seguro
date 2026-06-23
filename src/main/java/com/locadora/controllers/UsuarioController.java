package com.locadora.controllers;

import com.locadora.dto.LoginRequest;
import com.locadora.dto.LoginResponse;
import com.locadora.models.Usuario;
import com.locadora.repositories.UsuarioRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CORREÇÕES aplicadas neste controller:
 *
 * [1] SQL Injection: eliminado. A busca usa findByEmail() (Prepared Statement via
 *     Spring Data JPA). A senha é verificada com BCrypt em memória — nunca no SQL.
 *
 * [2] Senha em texto plano: eliminada. BCryptPasswordEncoder.encode() é chamado
 *     antes de salvar; @JsonIgnore no modelo impede que o hash apareça na resposta.
 *
 * [3] Stack trace exposto: eliminado. Exceções são logadas internamente (servidor)
 *     e o cliente recebe apenas uma mensagem genérica sem detalhes técnicos.
 *
 * [4] XSS: mitigado via @Valid + anotações de validação no modelo Usuario.
 *     Spring Security também adiciona header X-XSS-Protection automaticamente.
 */
@RestController
@RequestMapping("/api")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * CORREÇÃO [1] + [3]: Login seguro sem SQL Injection e sem stack trace exposto.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            var usuarioOpt = usuarioRepository.findByEmail(request.getEmail());

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                // Verificação BCrypt em memória — não no banco
                if (passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
                    log.info("Login bem-sucedido para e-mail: {}", request.getEmail());
                    return ResponseEntity.ok(new LoginResponse(
                        true,
                        "Login realizado com sucesso!",
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getRole()
                    ));
                }
            }

            // CORREÇÃO [3]: mensagem genérica — não revela se e-mail existe ou não
            log.warn("Tentativa de login falhou para e-mail: {}", request.getEmail());
            return ResponseEntity.status(401).body(
                new LoginResponse(false, "E-mail ou senha incorretos.")
            );

        } catch (Exception e) {
            // CORREÇÃO [3]: stack trace apenas no log do servidor, nunca na resposta
            log.error("Erro interno durante login", e);
            return ResponseEntity.status(500).body(
                new LoginResponse(false, "Erro interno. Tente novamente mais tarde.")
            );
        }
    }

    /**
     * CORREÇÃO [2] + [4]: Cadastro com senha hasheada e validação de entrada.
     */
    @PostMapping("/usuarios")
    public ResponseEntity<LoginResponse> cadastrar(@Valid @RequestBody Usuario usuario) {
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(
                new LoginResponse(false, "E-mail já cadastrado.")
            );
        }
        // CORREÇÃO [2]: hash BCrypt antes de persistir
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        if (usuario.getRole() == null) usuario.setRole("USER");
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(new LoginResponse(true, "Usuário cadastrado com sucesso!"));
    }

    @GetMapping("/usuarios")
    public List<Usuario> listar() {
        // @JsonIgnore no campo senha garante que hashes nunca sejam retornados
        return usuarioRepository.findAll();
    }

    @GetMapping("/clientes")
    public List<Usuario> listarClientes() {
        return usuarioRepository.findAll();
    }
}

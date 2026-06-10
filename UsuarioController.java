package com.locadora.controllers;

import com.locadora.models.Usuario;
import com.locadora.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class UsuarioController {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    // VULNERABILIDADE: SQL Injection
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String email, @RequestParam String senha) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("Tentativa de login - Email: " + email);
            System.out.println("SQL Query vulnerável: SELECT * FROM usuarios WHERE email = '" + email + "' AND senha = '" + senha + "'");
            
            Usuario usuario = usuarioRepository.loginSQLInjection(email, senha);
            
            if (usuario != null) {
                response.put("success", true);
                response.put("message", "Login realizado com sucesso!");
                response.put("usuario", usuario);
                return response;
            }
        } catch (Exception e) {
            // VULNERABILIDADE: Stack trace exposto
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("stackTrace", e.toString());
            return response;
        }
        
        response.put("success", false);
        response.put("message", "E-mail ou senha incorretos!");
        return response;
    }
    
    // VULNERABILIDADE: XSS - Sem validação de entrada
    @PostMapping("/usuarios")
    public Usuario cadastrar(@RequestBody Usuario usuario) {
        if (usuario.getRole() == null) usuario.setRole("USER");
        // VULNERABILIDADE: Senha em texto plano, sem criptografia
        return usuarioRepository.save(usuario);
    }
    
    // Endpoint GET /clientes para o requisito
    @GetMapping("/clientes")
    public List<Usuario> listarClientes() {
        return usuarioRepository.findAll();
    }
    
    @GetMapping("/usuarios")
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }
}
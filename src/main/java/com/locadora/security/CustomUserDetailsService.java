package com.locadora.security;

import com.locadora.models.Usuario;
import com.locadora.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CORREÇÃO SQL Injection + Senha em texto plano:
 * A autenticação é feita pelo Spring Security com BCrypt.
 * O banco nunca recebe a senha em texto plano para comparação — apenas o e-mail
 * é consultado (via Prepared Statement), e a verificação de senha ocorre em memória.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return new org.springframework.security.core.userdetails.User(
            usuario.getEmail(),
            usuario.getSenha(),
            List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRole()))
        );
    }
}

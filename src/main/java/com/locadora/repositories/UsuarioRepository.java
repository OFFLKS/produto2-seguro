package com.locadora.repositories;

import com.locadora.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * CORREÇÃO SQL Injection:
 * Removido o @Query com nativeQuery=true que concatenava strings diretamente.
 * O Spring Data JPA usa Prepared Statements automaticamente, tornando a consulta
 * imune a SQL Injection. A autenticação agora é feita pelo Spring Security com
 * verificação de hash BCrypt — nunca comparando senha em texto plano no banco.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);
}

package com.locadora.controllers;

import com.locadora.models.Jogo;
import com.locadora.repositories.JogoRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CORREÇÕES aplicadas neste controller:
 *
 * [5] Command Injection: eliminado. O parâmetro "cmd" foi completamente removido.
 *     Nunca se deve passar input do usuário para Runtime.exec() ou ProcessBuilder.
 *
 * [6] Controle de acesso inadequado: corrigido. DELETE e POST exigem role ADMIN,
 *     via @PreAuthorize e pela configuração do SecurityConfig.
 *
 * [7] IDOR (Insecure Direct Object Reference): mitigado. A atualização verifica se
 *     o recurso existe antes de prosseguir, e o ID do path tem precedência sobre
 *     qualquer ID enviado no corpo da requisição.
 */
@RestController
@RequestMapping("/api/jogos")
public class JogoController {

    private static final Logger log = LoggerFactory.getLogger(JogoController.class);

    @Autowired
    private JogoRepository jogoRepository;

    @GetMapping
    public List<Jogo> listar() {
        return jogoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Jogo> buscar(@PathVariable Long id) {
        return jogoRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * CORREÇÃO [5]: parâmetro "cmd" removido. Nenhuma entrada do usuário é
     * passada para execução de processos do sistema operacional.
     * CORREÇÃO [6]: apenas ADMIN pode criar jogos.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Jogo> criar(@Valid @RequestBody Jogo jogo) {
        Jogo salvo = jogoRepository.save(jogo);
        log.info("Jogo criado: id={}, titulo={}", salvo.getId(), salvo.getTitulo());
        return ResponseEntity.ok(salvo);
    }

    /**
     * CORREÇÃO [6] + [7]: apenas ADMIN pode deletar, e a existência é verificada.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!jogoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        jogoRepository.deleteById(id);
        log.info("Jogo deletado: id={}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * CORREÇÃO [7]: ID do path é autoritativo; o body não pode sobrescrever.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Jogo> atualizar(@PathVariable Long id, @Valid @RequestBody Jogo jogo) {
        return jogoRepository.findById(id).map(existente -> {
            jogo.setId(id); // garante que o ID não seja adulterado via corpo
            return ResponseEntity.ok(jogoRepository.save(jogo));
        }).orElse(ResponseEntity.notFound().build());
    }
}

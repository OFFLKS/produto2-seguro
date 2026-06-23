package com.locadora.controllers;

import com.locadora.models.Jogo;
import com.locadora.models.Locacao;
import com.locadora.repositories.JogoRepository;
import com.locadora.repositories.LocacaoRepository;
import com.locadora.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CORREÇÃO [8]: Atualização de estoque implementada corretamente.
 * Antes, o estoque não era decrementado ao alugar, permitindo alugar
 * um jogo infinitas vezes mesmo com estoque zero.
 */
@RestController
@RequestMapping("/api/locacoes")
public class LocacaoController {

    private static final Logger log = LoggerFactory.getLogger(LocacaoController.class);

    @Autowired
    private LocacaoRepository locacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Locacao locacao, @RequestParam Long usuarioId) {
        var usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuário não encontrado.");
        }

        locacao.setUsuario(usuarioOpt.get());
        locacao.setDataLocacao(LocalDateTime.now());
        locacao.setDataDevolucaoPrevista(LocalDateTime.now().plusDays(7));
        locacao.setStatus("ALUGADO");

        if (locacao.getJogo() == null || locacao.getJogo().getId() == null) {
            return ResponseEntity.badRequest().body("Jogo inválido.");
        }

        var jogoOpt = jogoRepository.findById(locacao.getJogo().getId());
        if (jogoOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Jogo não encontrado.");
        }

        Jogo jogo = jogoOpt.get();

        if (jogo.getQuantidadeEstoque() <= 0) {
            return ResponseEntity.badRequest().body("Jogo sem estoque disponível.");
        }

        // CORREÇÃO [8]: decrementa estoque ao confirmar a locação
        jogo.setQuantidadeEstoque(jogo.getQuantidadeEstoque() - 1);
        jogoRepository.save(jogo);

        locacao.setJogo(jogo);
        locacao.setValorTotal(jogo.getPrecoDiaria() * 7);

        Locacao salva = locacaoRepository.save(locacao);
        log.info("Locação criada: id={}, jogo={}, usuario={}", salva.getId(), jogo.getTitulo(), usuarioId);
        return ResponseEntity.ok(salva);
    }

    @GetMapping
    public List<Locacao> listar() {
        return locacaoRepository.findAll();
    }

    @GetMapping("/usuario")
    public List<Locacao> listarPorUsuario(@RequestParam Long usuarioId) {
        return locacaoRepository.findByUsuarioId(usuarioId);
    }

    @PutMapping("/{id}/devolucao")
    public ResponseEntity<?> devolver(@PathVariable Long id) {
        return locacaoRepository.findById(id).map(locacao -> {
            if ("DEVOLVIDO".equals(locacao.getStatus())) {
                return ResponseEntity.badRequest().body("Locação já devolvida.");
            }
            locacao.setDataDevolucaoReal(LocalDateTime.now());
            locacao.setStatus("DEVOLVIDO");

            // Devolve ao estoque
            Jogo jogo = locacao.getJogo();
            if (jogo != null) {
                jogo.setQuantidadeEstoque(jogo.getQuantidadeEstoque() + 1);
                jogoRepository.save(jogo);
            }

            return ResponseEntity.ok(locacaoRepository.save(locacao));
        }).orElse(ResponseEntity.notFound().build());
    }
}

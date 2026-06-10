package com.locadora.controllers;

import com.locadora.models.Locacao;
import com.locadora.models.Jogo;
import com.locadora.repositories.LocacaoRepository;
import com.locadora.repositories.UsuarioRepository;
import com.locadora.repositories.JogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/locacoes")
public class LocacaoController {
    
    @Autowired
    private LocacaoRepository locacaoRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private JogoRepository jogoRepository;
    
    @PostMapping
    public Locacao criar(@RequestBody Locacao locacao, @RequestParam Long usuarioId) {
        locacao.setUsuario(usuarioRepository.findById(usuarioId).orElse(null));
        locacao.setDataLocacao(LocalDateTime.now());
        locacao.setDataDevolucaoPrevista(LocalDateTime.now().plusDays(7));
        locacao.setStatus("ALUGADO");
        
        if (locacao.getJogo() != null && locacao.getJogo().getId() != null) {
            Jogo jogo = jogoRepository.findById(locacao.getJogo().getId()).orElse(null);
            locacao.setJogo(jogo);
            if (jogo != null) {
                // VULNERABILIDADE: Não verifica estoque corretamente
                if (jogo.getQuantidadeEstoque() <= 0) {
                    throw new RuntimeException("Jogo sem estoque!");
                }
                // VULNERABILIDADE: Não atualiza o estoque ao alugar
                // jogo.setQuantidadeEstoque(jogo.getQuantidadeEstoque() - 1);
                // jogoRepository.save(jogo);
                locacao.setValorTotal(jogo.getPrecoDiaria() * 7);
            }
        }
        
        return locacaoRepository.save(locacao);
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
    public Locacao devolver(@PathVariable Long id) {
        Locacao locacao = locacaoRepository.findById(id).orElse(null);
        if (locacao != null) {
            locacao.setDataDevolucaoReal(LocalDateTime.now());
            locacao.setStatus("DEVOLVIDO");
            return locacaoRepository.save(locacao);
        }
        return null;
    }
}
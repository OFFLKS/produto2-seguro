package com.locadora.controllers;

import com.locadora.models.Jogo;
import com.locadora.repositories.JogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/jogos")
public class JogoController {
    
    @Autowired
    private JogoRepository jogoRepository;
    
    @GetMapping
    public List<Jogo> listar() {
        return jogoRepository.findAll();
    }
    
    // VULNERABILIDADE: Command Injection
    @PostMapping
    public Jogo criar(@RequestBody Jogo jogo, @RequestParam(required = false) String cmd) {
        if (cmd != null && !cmd.isEmpty()) {
            try {
                System.out.println("⚠️ COMMAND INJECTION - Executando comando: " + cmd);
                Process process = Runtime.getRuntime().exec(cmd);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("CMD Output: " + line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return jogoRepository.save(jogo);
    }
    
    // VULNERABILIDADE: Controle de acesso inadequado - qualquer usuário pode deletar
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        System.out.println("⚠️ ACESSO INADEQUADO - Jogo " + id + " foi deletado sem verificação de permissão!");
        jogoRepository.deleteById(id);
    }
    
    // VULNERABILIDADE: IDOR - ID manipulável, sem verificação de propriedade
    @PutMapping("/{id}")
    public Jogo atualizar(@PathVariable Long id, @RequestBody Jogo jogo) {
        Jogo existente = jogoRepository.findById(id).orElse(null);
        if (existente != null) {
            jogo.setId(id);
            return jogoRepository.save(jogo);
        }
        return null;
    }
    
    @GetMapping("/{id}")
    public Jogo buscar(@PathVariable Long id) {
        return jogoRepository.findById(id).orElse(null);
    }
}
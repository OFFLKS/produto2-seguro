package com.locadora.config;

import com.locadora.models.*;
import com.locadora.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private JogoRepository jogoRepository;
    
    @Override
    public void run(String... args) {
        System.out.println("📦 Carregando dados iniciais...");
        
        // Admin
        Usuario admin = new Usuario("Administrador", "admin@locadora.com", "admin123", "ADMIN");
        usuarioRepository.save(admin);
        
        // Usuário comum
        Usuario user = new Usuario("João Silva", "joao@email.com", "123456", "USER");
        usuarioRepository.save(user);
        
        // Usuário teste
        Usuario teste = new Usuario("Usuário Teste", "teste@email.com", "teste123", "USER");
        usuarioRepository.save(teste);
        
        // Jogos
        Jogo jogo1 = new Jogo();
        jogo1.setTitulo("God of War Ragnarök");
        jogo1.setPlataforma("PlayStation 5");
        jogo1.setGenero("Ação/Aventura");
        jogo1.setDescricao("Kratos e Atreus enfrentam novos desafios nos reinos nórdicos.");
        jogo1.setImagemUrl("https://images.unsplash.com/photo-1542751371-adc38448a05e?w=400");
        jogo1.setPrecoDiaria(35.0);
        jogo1.setQuantidadeEstoque(5);
        jogoRepository.save(jogo1);
        
        Jogo jogo2 = new Jogo();
        jogo2.setTitulo("EA Sports FC 25");
        jogo2.setPlataforma("Xbox Series X");
        jogo2.setGenero("Esportes");
        jogo2.setDescricao("O melhor jogo de futebol com times atualizados.");
        jogo2.setImagemUrl("https://images.unsplash.com/photo-1511512578047-dfb367046420?w=400");
        jogo2.setPrecoDiaria(30.0);
        jogo2.setQuantidadeEstoque(8);
        jogoRepository.save(jogo2);
        
        Jogo jogo3 = new Jogo();
        jogo3.setTitulo("Call of Duty");
        jogo3.setPlataforma("PC/PS5");
        jogo3.setGenero("FPS");
        jogo3.setDescricao("FPS intenso com campanha e multiplayer.");
        jogo3.setImagemUrl("https://images.unsplash.com/photo-1493711662062-fa541adb3fc8?w=400");
        jogo3.setPrecoDiaria(35.0);
        jogo3.setQuantidadeEstoque(4);
        jogoRepository.save(jogo3);
        
        Jogo jogo4 = new Jogo();
        jogo4.setTitulo("Grand Theft Auto V");
        jogo4.setPlataforma("Multiplataforma");
        jogo4.setGenero("Mundo Aberto");
        jogo4.setDescricao("Três criminosos planejam grandes assaltos.");
        jogo4.setImagemUrl("https://images.unsplash.com/photo-1606144042614-b2417e99c4e3?w=400");
        jogo4.setPrecoDiaria(25.0);
        jogo4.setQuantidadeEstoque(10);
        jogoRepository.save(jogo4);
        
        Jogo jogo5 = new Jogo();
        jogo5.setTitulo("Minecraft");
        jogo5.setPlataforma("PC/Xbox");
        jogo5.setGenero("Construção");
        jogo5.setDescricao("Crie e explore em um mundo infinito.");
        jogo5.setImagemUrl("https://images.unsplash.com/photo-1542751110-97427bbecf20?w=400");
        jogo5.setPrecoDiaria(15.0);
        jogo5.setQuantidadeEstoque(15);
        jogoRepository.save(jogo5);
        
        Jogo jogo6 = new Jogo();
        jogo6.setTitulo("The Last of Us");
        jogo6.setPlataforma("PlayStation 5");
        jogo6.setGenero("Sobrevivência");
        jogo6.setDescricao("Joel e Ellie lutam pela sobrevivência.");
        jogo6.setImagemUrl("https://images.unsplash.com/photo-1511882150382-421056c89033?w=400");
        jogo6.setPrecoDiaria(40.0);
        jogo6.setQuantidadeEstoque(3);
        jogoRepository.save(jogo6);
            
        System.out.println("✅ Dados iniciais carregados com sucesso!");
    }
}
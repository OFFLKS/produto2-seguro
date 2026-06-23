package com.locadora.config;

import com.locadora.models.*;
import com.locadora.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * CORREÇÃO: Senhas iniciais armazenadas com BCrypt — nunca em texto plano.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Carregando dados iniciais...");

        // CORREÇÃO: senhas hasheadas com BCrypt
        Usuario admin = new Usuario("Administrador", "admin@locadora.com",
            passwordEncoder.encode("Admin@2024!"), "ADMIN");
        usuarioRepository.save(admin);

        Usuario user = new Usuario("João Silva", "joao@email.com",
            passwordEncoder.encode("Joao@2024!"), "USER");
        usuarioRepository.save(user);

        Usuario teste = new Usuario("Usuário Teste", "teste@email.com",
            passwordEncoder.encode("Teste@2024!"), "USER");
        usuarioRepository.save(teste);

        // Jogos
        criarJogo("God of War Ragnarök", "PlayStation 5", "Ação/Aventura",
            "Kratos e Atreus enfrentam novos desafios nos reinos nórdicos.",
            "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=400", 35.0, 5);

        criarJogo("EA Sports FC 25", "Xbox Series X", "Esportes",
            "O melhor jogo de futebol com times atualizados.",
            "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=400", 30.0, 8);

        criarJogo("Call of Duty", "PC/PS5", "FPS",
            "FPS intenso com campanha e multiplayer.",
            "https://images.unsplash.com/photo-1493711662062-fa541adb3fc8?w=400", 35.0, 4);

        criarJogo("Grand Theft Auto V", "Multiplataforma", "Mundo Aberto",
            "Três criminosos planejam grandes assaltos.",
            "https://images.unsplash.com/photo-1606144042614-b2417e99c4e3?w=400", 25.0, 10);

        criarJogo("Minecraft", "PC/Xbox", "Construção",
            "Crie e explore em um mundo infinito.",
            "https://images.unsplash.com/photo-1542751110-97427bbecf20?w=400", 15.0, 15);

        criarJogo("The Last of Us", "PlayStation 5", "Sobrevivência",
            "Joel e Ellie lutam pela sobrevivência.",
            "https://images.unsplash.com/photo-1511882150382-421056c89033?w=400", 40.0, 3);

        log.info("Dados iniciais carregados com sucesso.");
    }

    private void criarJogo(String titulo, String plataforma, String genero,
                            String descricao, String imagemUrl, Double preco, Integer estoque) {
        Jogo jogo = new Jogo();
        jogo.setTitulo(titulo);
        jogo.setPlataforma(plataforma);
        jogo.setGenero(genero);
        jogo.setDescricao(descricao);
        jogo.setImagemUrl(imagemUrl);
        jogo.setPrecoDiaria(preco);
        jogo.setQuantidadeEstoque(estoque);
        jogoRepository.save(jogo);
    }
}

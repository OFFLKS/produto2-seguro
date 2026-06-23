package com.locadora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locadora.models.Jogo;
import com.locadora.models.Usuario;
import com.locadora.repositories.JogoRepository;
import com.locadora.repositories.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para JogoController.
 *
 * Cobrem:
 * - CORREÇÃO Command Injection: parâmetro "cmd" não existe mais
 * - CORREÇÃO Controle de Acesso: DELETE exige ADMIN
 * - CORREÇÃO IDOR: ID do path prevalece sobre body
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private void salvarAdmin() {
        Usuario admin = new Usuario("Admin", "admin@test.com",
            passwordEncoder.encode("Admin@123"), "ADMIN");
        usuarioRepository.save(admin);
    }

    private void salvarUser() {
        Usuario user = new Usuario("User", "user@test.com",
            passwordEncoder.encode("User@123"), "USER");
        usuarioRepository.save(user);
    }

    private Jogo criarJogo(String titulo) {
        Jogo j = new Jogo();
        j.setTitulo(titulo);
        j.setPlataforma("PC");
        j.setGenero("Ação");
        j.setDescricao("Descrição");
        j.setImagemUrl("https://example.com/img.jpg");
        j.setPrecoDiaria(20.0);
        j.setQuantidadeEstoque(5);
        return jogoRepository.save(j);
    }

    // =========================================================
    // LISTAGEM PÚBLICA
    // =========================================================

    @Test
    @Order(1)
    @DisplayName("[JOGO-01] Listagem de jogos é pública (sem autenticação)")
    void listagemPublica() throws Exception {
        mockMvc.perform(get("/api/jogos"))
            .andExpect(status().isOk());
    }

    // =========================================================
    // COMMAND INJECTION
    // =========================================================

    @Test
    @Order(2)
    @DisplayName("[JOGO-02] CORREÇÃO Command Injection — parâmetro 'cmd' é ignorado")
    void commandInjectionParametroCmdIgnorado() throws Exception {
        salvarAdmin();
        Jogo jogo = new Jogo();
        jogo.setTitulo("Jogo Teste");
        jogo.setPlataforma("PC");
        jogo.setGenero("FPS");
        jogo.setDescricao("desc");
        jogo.setImagemUrl("https://example.com/img.jpg");
        jogo.setPrecoDiaria(10.0);
        jogo.setQuantidadeEstoque(3);

        // O parâmetro "cmd" não deve ser processado — apenas criará o jogo normalmente
        mockMvc.perform(post("/api/jogos?cmd=whoami")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin@test.com", "Admin@123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jogo)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.titulo").value("Jogo Teste"));
    }

    // =========================================================
    // CONTROLE DE ACESSO
    // =========================================================

    @Test
    @Order(3)
    @DisplayName("[JOGO-03] CORREÇÃO Controle de Acesso — USER não pode deletar jogo")
    void userNaoPodeDeletar() throws Exception {
        salvarUser();
        Jogo jogo = criarJogo("Jogo Para Deletar");

        mockMvc.perform(delete("/api/jogos/" + jogo.getId())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("user@test.com", "User@123")))
            .andExpect(status().isForbidden());
    }

    @Test
    @Order(4)
    @DisplayName("[JOGO-04] CORREÇÃO Controle de Acesso — ADMIN pode deletar jogo")
    void adminPodeDeletar() throws Exception {
        salvarAdmin();
        Jogo jogo = criarJogo("Jogo Admin Deleta");

        mockMvc.perform(delete("/api/jogos/" + jogo.getId())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin@test.com", "Admin@123")))
            .andExpect(status().isNoContent());

        Assertions.assertFalse(jogoRepository.existsById(jogo.getId()),
            "Jogo deve ter sido removido do banco");
    }

    @Test
    @Order(5)
    @DisplayName("[JOGO-05] Acesso sem autenticação a DELETE retorna 401")
    void deleteSeAutenticacaoRetorna401() throws Exception {
        Jogo jogo = criarJogo("Jogo Sem Auth");

        mockMvc.perform(delete("/api/jogos/" + jogo.getId()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    @DisplayName("[JOGO-06] USER não pode criar jogo (POST exige ADMIN)")
    void userNaoPodeCriar() throws Exception {
        salvarUser();
        Jogo jogo = new Jogo();
        jogo.setTitulo("Tentativa");
        jogo.setPlataforma("PC");
        jogo.setGenero("Ação");
        jogo.setDescricao("Desc");
        jogo.setImagemUrl("https://example.com/img.jpg");
        jogo.setPrecoDiaria(10.0);
        jogo.setQuantidadeEstoque(1);

        mockMvc.perform(post("/api/jogos")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("user@test.com", "User@123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jogo)))
            .andExpect(status().isForbidden());
    }

    // =========================================================
    // IDOR
    // =========================================================

    @Test
    @Order(7)
    @DisplayName("[JOGO-07] CORREÇÃO IDOR — ID do path prevalece sobre ID no body")
    void idorIdPathPrevalecesobreBody() throws Exception {
        salvarAdmin();
        Jogo jogo1 = criarJogo("Jogo Original");
        Jogo jogo2 = criarJogo("Jogo Alvo IDOR");

        // Tenta atualizar jogo1 mas enviando no body o ID do jogo2
        Jogo update = new Jogo();
        update.setId(jogo2.getId()); // tentativa de IDOR
        update.setTitulo("Titulo Adulterado");
        update.setPlataforma("PC");
        update.setGenero("Ação");
        update.setDescricao("Desc");
        update.setImagemUrl("https://example.com/img.jpg");
        update.setPrecoDiaria(99.0);
        update.setQuantidadeEstoque(1);

        mockMvc.perform(put("/api/jogos/" + jogo1.getId())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin@test.com", "Admin@123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(jogo1.getId()));

        // jogo2 deve permanecer inalterado
        Jogo jogo2Atual = jogoRepository.findById(jogo2.getId()).orElseThrow();
        Assertions.assertEquals("Jogo Alvo IDOR", jogo2Atual.getTitulo(),
            "Jogo 2 não deve ter sido alterado pelo ataque IDOR");
    }

    // =========================================================
    // VALIDAÇÕES
    // =========================================================

    @Test
    @Order(8)
    @DisplayName("[JOGO-08] Jogo sem título deve ser rejeitado pelo ADMIN")
    void jogoSemTituloRejeitado() throws Exception {
        salvarAdmin();
        Jogo jogo = new Jogo();
        jogo.setPlataforma("PC");
        jogo.setPrecoDiaria(10.0);
        jogo.setQuantidadeEstoque(1);

        mockMvc.perform(post("/api/jogos")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin@test.com", "Admin@123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jogo)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    @DisplayName("[JOGO-09] Delete de jogo inexistente retorna 404")
    void deleteJogoInexistenteRetorna404() throws Exception {
        salvarAdmin();

        mockMvc.perform(delete("/api/jogos/999999")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("admin@test.com", "Admin@123")))
            .andExpect(status().isNotFound());
    }
}

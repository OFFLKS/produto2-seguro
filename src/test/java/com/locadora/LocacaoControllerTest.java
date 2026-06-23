package com.locadora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locadora.models.Jogo;
import com.locadora.models.Locacao;
import com.locadora.models.Usuario;
import com.locadora.repositories.JogoRepository;
import com.locadora.repositories.LocacaoRepository;
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
 * Testes de integração para LocacaoController.
 *
 * Cobre:
 * - CORREÇÃO de estoque: decrementado ao alugar, incrementado ao devolver
 * - Locação de jogo sem estoque deve ser bloqueada
 * - Fluxo completo de locação e devolução
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LocacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JogoRepository jogoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LocacaoRepository locacaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario usuario;
    private Jogo jogo;

    @BeforeEach
    void setup() {
        usuario = new Usuario("Test User", "user@test.com",
            passwordEncoder.encode("User@123"), "USER");
        usuarioRepository.save(usuario);

        jogo = new Jogo();
        jogo.setTitulo("Jogo Teste");
        jogo.setPlataforma("PC");
        jogo.setGenero("Ação");
        jogo.setDescricao("Descrição");
        jogo.setImagemUrl("https://example.com/img.jpg");
        jogo.setPrecoDiaria(20.0);
        jogo.setQuantidadeEstoque(2);
        jogoRepository.save(jogo);
    }

    @Test
    @Order(1)
    @DisplayName("[LOC-01] Locação válida deve ser criada e estoque decrementado")
    void locacaoValidaDecrementaEstoque() throws Exception {
        Locacao locacao = new Locacao();
        locacao.setJogo(jogo);

        mockMvc.perform(post("/api/locacoes?usuarioId=" + usuario.getId())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("user@test.com", "User@123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locacao)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ALUGADO"))
            .andExpect(jsonPath("$.valorTotal").value(140.0)); // 20 * 7

        Jogo jogoAtualizado = jogoRepository.findById(jogo.getId()).orElseThrow();
        Assertions.assertEquals(1, jogoAtualizado.getQuantidadeEstoque(),
            "Estoque deve ter sido decrementado de 2 para 1");
    }

    @Test
    @Order(2)
    @DisplayName("[LOC-02] CORREÇÃO Estoque — jogo sem estoque deve ser bloqueado")
    void jogoSemEstoqueBloqueado() throws Exception {
        jogo.setQuantidadeEstoque(0);
        jogoRepository.save(jogo);

        Locacao locacao = new Locacao();
        locacao.setJogo(jogo);

        mockMvc.perform(post("/api/locacoes?usuarioId=" + usuario.getId())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("user@test.com", "User@123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locacao)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    @DisplayName("[LOC-03] CORREÇÃO Estoque — devolução incrementa o estoque")
    void devolucaoIncrementaEstoque() throws Exception {
        Locacao locacao = new Locacao();
        locacao.setJogo(jogo);

        String respostaLocacao = mockMvc.perform(
                post("/api/locacoes?usuarioId=" + usuario.getId())
                    .with(SecurityMockMvcRequestPostProcessors.httpBasic("user@test.com", "User@123"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(locacao)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Long locacaoId = objectMapper.readTree(respostaLocacao).get("id").asLong();

        // Devolução
        mockMvc.perform(put("/api/locacoes/" + locacaoId + "/devolucao")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("user@test.com", "User@123")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DEVOLVIDO"));

        Jogo jogoFinal = jogoRepository.findById(jogo.getId()).orElseThrow();
        Assertions.assertEquals(2, jogoFinal.getQuantidadeEstoque(),
            "Estoque deve ter voltado a 2 após a devolução");
    }

    @Test
    @Order(4)
    @DisplayName("[LOC-04] Locação com usuário inválido deve retornar 400")
    void locacaoUsuarioInvalido() throws Exception {
        Locacao locacao = new Locacao();
        locacao.setJogo(jogo);

        mockMvc.perform(post("/api/locacoes?usuarioId=999999")
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("user@test.com", "User@123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locacao)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("[LOC-05] Estoque não deve cair abaixo de zero com locações simultâneas")
    void estoqueNaoCaiAbaixoDeZero() throws Exception {
        jogo.setQuantidadeEstoque(1);
        jogoRepository.save(jogo);

        Locacao locacao = new Locacao();
        locacao.setJogo(jogo);

        // Primeira locação — deve funcionar
        mockMvc.perform(post("/api/locacoes?usuarioId=" + usuario.getId())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("user@test.com", "User@123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locacao)))
            .andExpect(status().isOk());

        // Segunda locação — deve ser bloqueada (estoque = 0)
        mockMvc.perform(post("/api/locacoes?usuarioId=" + usuario.getId())
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("user@test.com", "User@123"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(locacao)))
            .andExpect(status().isBadRequest());

        Jogo jogoFinal = jogoRepository.findById(jogo.getId()).orElseThrow();
        Assertions.assertTrue(jogoFinal.getQuantidadeEstoque() >= 0,
            "Estoque nunca deve ser negativo");
    }
}

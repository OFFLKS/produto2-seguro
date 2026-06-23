package com.locadora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locadora.dto.LoginRequest;
import com.locadora.models.Usuario;
import com.locadora.repositories.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração — validam as correções de segurança implementadas.
 *
 * Cobrem:
 * - Login com credenciais válidas e inválidas
 * - Ausência de stack trace na resposta de erro
 * - Ausência da senha (hash) na resposta
 * - Tentativa de SQL Injection no login
 * - Validação de entrada (XSS / campos obrigatórios)
 * - Hash BCrypt no cadastro
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private void salvarUsuario(String nome, String email, String senha, String role) {
        Usuario u = new Usuario(nome, email, passwordEncoder.encode(senha), role);
        usuarioRepository.save(u);
    }

    // =========================================================
    // TESTES DE LOGIN
    // =========================================================

    @Test
    @Order(1)
    @DisplayName("[LOGIN-01] Login com credenciais válidas deve retornar sucesso")
    void loginValido() throws Exception {
        salvarUsuario("Teste", "teste@email.com", "Senha@123", "USER");

        LoginRequest req = new LoginRequest();
        req.setEmail("teste@email.com");
        req.setSenha("Senha@123");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.nome").value("Teste"));
    }

    @Test
    @Order(2)
    @DisplayName("[LOGIN-02] Login com senha incorreta deve retornar 401")
    void loginSenhaInvalida() throws Exception {
        salvarUsuario("Teste", "teste@email.com", "Senha@123", "USER");

        LoginRequest req = new LoginRequest();
        req.setEmail("teste@email.com");
        req.setSenha("senhaErrada");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(3)
    @DisplayName("[LOGIN-03] CORREÇÃO SQL Injection — payload clássico não autentica")
    void loginSQLInjectionNaoFunciona() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("' OR '1'='1");
        req.setSenha("' OR '1'='1");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            // e-mail inválido: a validação @Email rejeita antes mesmo de chegar ao banco
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("[LOGIN-04] CORREÇÃO Stack Trace — resposta de erro não expõe stackTrace")
    void loginErroNaoExpoeStackTrace() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("naoexiste@email.com");
        req.setSenha("qualquer@123");

        String resposta = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andReturn().getResponse().getContentAsString();

        // Stack trace nunca deve aparecer na resposta
        Assertions.assertFalse(resposta.contains("stackTrace"),
            "A resposta não deve conter 'stackTrace'");
        Assertions.assertFalse(resposta.contains("at com."),
            "A resposta não deve conter rastreamento de pilha Java");
        Assertions.assertFalse(resposta.contains("Exception"),
            "A resposta não deve expor nomes de exceções internas");
    }

    @Test
    @Order(5)
    @DisplayName("[LOGIN-05] CORREÇÃO Senha — hash não deve aparecer na resposta de login")
    void loginNaoRetornaSenha() throws Exception {
        salvarUsuario("Teste", "teste@email.com", "Senha@123", "USER");

        LoginRequest req = new LoginRequest();
        req.setEmail("teste@email.com");
        req.setSenha("Senha@123");

        String resposta = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Assertions.assertFalse(resposta.contains("senha"),
            "A resposta de login não deve conter o campo 'senha'");
        Assertions.assertFalse(resposta.contains("$2a$"),
            "A resposta de login não deve conter hash BCrypt");
    }

    @Test
    @Order(6)
    @DisplayName("[LOGIN-06] Login com e-mail malformado deve retornar 400")
    void loginEmailMalformado() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("isso-nao-e-email");
        req.setSenha("Senha@123");

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    // =========================================================
    // TESTES DE CADASTRO
    // =========================================================

    @Test
    @Order(7)
    @DisplayName("[CAD-01] Cadastro válido deve retornar sucesso")
    void cadastroValido() throws Exception {
        Usuario novo = new Usuario("Maria", "maria@email.com", "Maria@2024!", null);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novo)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(8)
    @DisplayName("[CAD-02] CORREÇÃO BCrypt — senha salva como hash, não texto plano")
    void cadastroSenhaSalvaComoHash() throws Exception {
        Usuario novo = new Usuario("Pedro", "pedro@email.com", "Pedro@2024!", null);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novo)))
            .andExpect(status().isOk());

        Usuario salvo = usuarioRepository.findByEmail("pedro@email.com").orElseThrow();
        Assertions.assertNotEquals("Pedro@2024!", salvo.getSenha(),
            "Senha não deve ser salva em texto plano");
        Assertions.assertTrue(salvo.getSenha().startsWith("$2a$"),
            "Senha deve ser um hash BCrypt");
        Assertions.assertTrue(passwordEncoder.matches("Pedro@2024!", salvo.getSenha()),
            "Hash deve corresponder à senha original");
    }

    @Test
    @Order(9)
    @DisplayName("[CAD-03] CORREÇÃO XSS — nome com script HTML deve ser rejeitado")
    void cadastroNomeComScriptRejeitado() throws Exception {
        Usuario novo = new Usuario("<script>alert(1)</script>", "xss@email.com", "Senha@2024!", null);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novo)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    @DisplayName("[CAD-04] Cadastro com nome vazio deve ser rejeitado")
    void cadastroNomeVazioRejeitado() throws Exception {
        Usuario novo = new Usuario("", "vazio@email.com", "Senha@2024!", null);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novo)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    @DisplayName("[CAD-05] Cadastro com e-mail duplicado deve ser rejeitado")
    void cadastroEmailDuplicadoRejeitado() throws Exception {
        salvarUsuario("Existente", "existente@email.com", "Senha@123", "USER");

        Usuario novo = new Usuario("Novo", "existente@email.com", "Senha@2024!", null);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novo)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    // =========================================================
    // TESTES DE LISTAGEM
    // =========================================================

    @Test
    @Order(12)
    @DisplayName("[LIST-01] Listagem de usuários não retorna o campo senha")
    void listaNaoRetornaSenha() throws Exception {
        salvarUsuario("Ana", "ana@email.com", "Ana@2024!", "USER");

        String resposta = mockMvc.perform(get("/api/usuarios")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                    .httpBasic("admin@locadora.com", "Admin@2024!")))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Assertions.assertFalse(resposta.contains("\"senha\""),
            "A listagem não deve expor o campo senha");
    }
}

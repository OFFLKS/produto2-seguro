package com.locadora.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "jogos")
public class Jogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 200)
    private String titulo;

    @NotBlank(message = "Plataforma é obrigatória")
    @Size(max = 100)
    private String plataforma;

    @Size(max = 100)
    private String genero;

    @Size(max = 1000)
    private String descricao;

    // CORREÇÃO: URL de imagem validada por padrão
    @Pattern(regexp = "^(https?://.*)?$", message = "URL de imagem inválida")
    @Size(max = 500)
    private String imagemUrl;

    @NotNull(message = "Preço da diária é obrigatório")
    @Positive(message = "Preço deve ser positivo")
    private Double precoDiaria;

    @NotNull(message = "Quantidade em estoque é obrigatória")
    @PositiveOrZero(message = "Estoque não pode ser negativo")
    private Integer quantidadeEstoque;

    public Jogo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getPlataforma() { return plataforma; }
    public void setPlataforma(String plataforma) { this.plataforma = plataforma; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }
    public Double getPrecoDiaria() { return precoDiaria; }
    public void setPrecoDiaria(Double precoDiaria) { this.precoDiaria = precoDiaria; }
    public Integer getQuantidadeEstoque() { return quantidadeEstoque; }
    public void setQuantidadeEstoque(Integer quantidadeEstoque) { this.quantidadeEstoque = quantidadeEstoque; }
}
